"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { Navbar } from "@/components/Navbar";
import { AnalysisResult } from "@/components/AnalysisResult";
import { Card } from "@/components/ui/Card";
import { useAnalysis } from "@/hooks/useAnalysis";
import { useRequireAuth } from "@/hooks/useRequireAuth";

export default function AnalysisDetailPage() {
  const { checked } = useRequireAuth();
  const params = useParams<{ id: string }>();
  const { data: analysis, isLoading, isError } = useAnalysis(params.id);

  if (!checked) return null;

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <main className="mx-auto max-w-3xl px-4 py-10">
        <div className="mb-6 flex items-center justify-between">
          <h1 className="text-2xl font-bold text-slate-900">Analysis Result</h1>
          <Link href="/dashboard" className="text-sm font-medium text-primary-600 hover:underline">
            ← Back to dashboard
          </Link>
        </div>

        {isLoading && (
          <Card className="py-12 text-center text-sm text-slate-500">Loading analysis…</Card>
        )}

        {isError && (
          <Card className="border-red-200 bg-red-50 text-sm text-red-700">
            Failed to load this analysis.
          </Card>
        )}

        {analysis && <AnalysisResult analysis={analysis} />}
      </main>
    </div>
  );
}
