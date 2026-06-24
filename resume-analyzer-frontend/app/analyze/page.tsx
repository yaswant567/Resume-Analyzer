"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { Navbar } from "@/components/Navbar";
import { UploadZone } from "@/components/UploadZone";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { TextArea } from "@/components/ui/TextArea";
import { extractErrorMessage } from "@/lib/api";
import { useSubmitAnalysis } from "@/hooks/useAnalysis";
import { useRequireAuth } from "@/hooks/useRequireAuth";

const MIN_JOB_DESCRIPTION_LENGTH = 50;
const MAX_JOB_DESCRIPTION_LENGTH = 10000;

export default function AnalyzePage() {
  const { checked } = useRequireAuth();
  const router = useRouter();
  const [file, setFile] = useState<File | null>(null);
  const [jobDescription, setJobDescription] = useState("");
  const [validationError, setValidationError] = useState<string | null>(null);
  const submitAnalysis = useSubmitAnalysis();

  if (!checked) return null;

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setValidationError(null);

    if (!file) {
      setValidationError("Please upload your resume as a PDF");
      return;
    }

    if (jobDescription.trim().length < MIN_JOB_DESCRIPTION_LENGTH) {
      setValidationError(
        `Job description must be at least ${MIN_JOB_DESCRIPTION_LENGTH} characters`
      );
      return;
    }

    submitAnalysis.mutate(
      { file, jobDescription },
      {
        onSuccess: (analysis) => {
          router.push(`/analyze/${analysis.id}`);
        },
      }
    );
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <main className="mx-auto max-w-3xl px-4 py-10">
        <h1 className="text-2xl font-bold text-slate-900">Analyze Your Resume</h1>
        <p className="mt-1 text-sm text-slate-600">
          Upload your resume and paste the job description to get an AI-powered match analysis.
        </p>

        <Card className="mt-6">
          <form onSubmit={handleSubmit} className="flex flex-col gap-5">
            <UploadZone file={file} onFileSelect={setFile} />

            <TextArea
              id="jobDescription"
              label="Job Description"
              placeholder="Paste the full job description here…"
              rows={10}
              value={jobDescription}
              onChange={(e) => setJobDescription(e.target.value)}
              maxLength={MAX_JOB_DESCRIPTION_LENGTH}
              required
            />
            <p className="-mt-3 text-right text-xs text-slate-400">
              {jobDescription.length} / {MAX_JOB_DESCRIPTION_LENGTH}
            </p>

            {(validationError || submitAnalysis.isError) && (
              <p className="text-sm text-red-600">
                {validationError ?? extractErrorMessage(submitAnalysis.error)}
              </p>
            )}

            <Button type="submit" isLoading={submitAnalysis.isPending} className="w-full">
              Analyze Resume
            </Button>
          </form>
        </Card>
      </main>
    </div>
  );
}
