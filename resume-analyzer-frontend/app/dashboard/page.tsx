"use client";

import Link from "next/link";
import clsx from "clsx";
import { Navbar } from "@/components/Navbar";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { useAnalysisList } from "@/hooks/useAnalysis";
import { useRequireAuth } from "@/hooks/useRequireAuth";
import type { AnalysisStatus } from "@/types";

const statusStyles: Record<AnalysisStatus, string> = {
  PENDING: "bg-slate-100 text-slate-700",
  PROCESSING: "bg-blue-100 text-blue-700",
  COMPLETED: "bg-green-100 text-green-700",
  FAILED: "bg-red-100 text-red-700",
};

function formatDate(value: string): string {
  return new Date(value).toLocaleString();
}

export default function DashboardPage() {
  const { user, checked } = useRequireAuth();
  const { data: analyses, isLoading, isError } = useAnalysisList();

  if (!checked) return null;

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <main className="mx-auto max-w-5xl px-4 py-10">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-slate-900">
              Welcome{user ? `, ${user.name}` : ""}
            </h1>
            <p className="mt-1 text-sm text-slate-600">
              Here&apos;s a history of your past resume analyses.
            </p>
          </div>
          <Link href="/analyze">
            <Button>New Analysis</Button>
          </Link>
        </div>

        <div className="mt-6 flex flex-col gap-4">
          {isLoading && (
            <Card className="py-12 text-center text-sm text-slate-500">Loading analyses…</Card>
          )}

          {isError && (
            <Card className="border-red-200 bg-red-50 text-sm text-red-700">
              Failed to load your analysis history.
            </Card>
          )}

          {!isLoading && !isError && analyses?.length === 0 && (
            <Card className="py-12 text-center">
              <p className="text-sm text-slate-600">You haven&apos;t analyzed any resumes yet.</p>
              <Link
                href="/analyze"
                className="mt-3 inline-block text-sm font-semibold text-primary-600 hover:underline"
              >
                Run your first analysis →
              </Link>
            </Card>
          )}

          {analyses?.map((analysis) => (
            <Link key={analysis.id} href={`/analyze/${analysis.id}`}>
              <Card className="flex items-center justify-between gap-4 transition-shadow hover:shadow-md">
                <div className="flex flex-col gap-1">
                  <span className="text-sm font-semibold text-slate-900">
                    {analysis.summary
                      ? analysis.summary.slice(0, 100) + (analysis.summary.length > 100 ? "…" : "")
                      : "Analysis pending…"}
                  </span>
                  <span className="text-xs text-slate-500">{formatDate(analysis.createdAt)}</span>
                </div>

                <div className="flex items-center gap-3">
                  {analysis.matchScore !== null && (
                    <span className="text-lg font-bold text-slate-900">
                      {analysis.matchScore}
                      <span className="text-xs font-normal text-slate-500">/100</span>
                    </span>
                  )}
                  <span
                    className={clsx(
                      "rounded-full px-3 py-1 text-xs font-medium",
                      statusStyles[analysis.status]
                    )}
                  >
                    {analysis.status}
                  </span>
                </div>
              </Card>
            </Link>
          ))}
        </div>
      </main>
    </div>
  );
}
