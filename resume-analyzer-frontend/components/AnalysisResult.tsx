import { Card } from "@/components/ui/Card";
import { KeywordBadge } from "@/components/KeywordBadge";
import { ScoreCard } from "@/components/ScoreCard";
import type { AnalysisResponse } from "@/types";

interface AnalysisResultProps {
  analysis: AnalysisResponse;
}

export function AnalysisResult({ analysis }: AnalysisResultProps) {
  if (analysis.status === "PENDING" || analysis.status === "PROCESSING") {
    return (
      <Card className="flex flex-col items-center gap-3 py-12 text-center">
        <span className="h-8 w-8 animate-spin rounded-full border-2 border-primary-600 border-t-transparent" />
        <p className="text-sm font-medium text-slate-700">
          {analysis.status === "PENDING" ? "Queued for analysis…" : "Analyzing your resume…"}
        </p>
        <p className="text-xs text-slate-500">This usually takes a few seconds.</p>
      </Card>
    );
  }

  if (analysis.status === "FAILED") {
    return (
      <Card className="border-red-200 bg-red-50">
        <h3 className="text-sm font-semibold text-red-800">Analysis failed</h3>
        <p className="mt-1 text-sm text-red-700">
          {analysis.errorMessage ?? "Something went wrong while analyzing your resume."}
        </p>
      </Card>
    );
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-3">
        <ScoreCard score={analysis.matchScore} />
        <Card className="sm:col-span-2">
          <h3 className="text-sm font-semibold text-slate-900">Summary</h3>
          <p className="mt-2 text-sm leading-relaxed text-slate-600">{analysis.summary}</p>
        </Card>
      </div>

      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
        <Card>
          <h3 className="text-sm font-semibold text-slate-900">Matched Keywords</h3>
          <div className="mt-3 flex flex-wrap gap-2">
            {analysis.matchedKeywords.length === 0 && (
              <p className="text-sm text-slate-500">No matched keywords found.</p>
            )}
            {analysis.matchedKeywords.map((keyword) => (
              <KeywordBadge key={keyword} label={keyword} variant="matched" />
            ))}
          </div>
        </Card>

        <Card>
          <h3 className="text-sm font-semibold text-slate-900">Missing Keywords</h3>
          <div className="mt-3 flex flex-wrap gap-2">
            {analysis.missingKeywords.length === 0 && (
              <p className="text-sm text-slate-500">No missing keywords found.</p>
            )}
            {analysis.missingKeywords.map((keyword) => (
              <KeywordBadge key={keyword} label={keyword} variant="missing" />
            ))}
          </div>
        </Card>
      </div>

      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
        <Card>
          <h3 className="text-sm font-semibold text-slate-900">Strengths</h3>
          <ul className="mt-3 list-inside list-disc space-y-1.5 text-sm text-slate-600">
            {analysis.strengths.map((item, i) => (
              <li key={i}>{item}</li>
            ))}
          </ul>
        </Card>

        <Card>
          <h3 className="text-sm font-semibold text-slate-900">Improvements</h3>
          <ul className="mt-3 list-inside list-disc space-y-1.5 text-sm text-slate-600">
            {analysis.improvements.map((item, i) => (
              <li key={i}>{item}</li>
            ))}
          </ul>
        </Card>
      </div>
    </div>
  );
}
