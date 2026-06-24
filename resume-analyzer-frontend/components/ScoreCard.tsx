import clsx from "clsx";

interface ScoreCardProps {
  score: number | null;
}

function getScoreColor(score: number): string {
  if (score >= 75) return "text-green-600 border-green-200 bg-green-50";
  if (score >= 50) return "text-amber-600 border-amber-200 bg-amber-50";
  return "text-red-600 border-red-200 bg-red-50";
}

function getScoreLabel(score: number): string {
  if (score >= 75) return "Strong Match";
  if (score >= 50) return "Moderate Match";
  return "Weak Match";
}

export function ScoreCard({ score }: ScoreCardProps) {
  if (score === null) {
    return (
      <div className="flex flex-col items-center justify-center rounded-xl border border-slate-200 bg-slate-50 p-6">
        <span className="text-sm text-slate-500">Score not available</span>
      </div>
    );
  }

  return (
    <div
      className={clsx(
        "flex flex-col items-center justify-center rounded-xl border p-6 text-center",
        getScoreColor(score)
      )}
    >
      <span className="text-5xl font-bold">{score}</span>
      <span className="mt-1 text-sm font-medium opacity-80">/ 100</span>
      <span className="mt-2 text-sm font-semibold">{getScoreLabel(score)}</span>
    </div>
  );
}
