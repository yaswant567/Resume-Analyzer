import clsx from "clsx";

interface KeywordBadgeProps {
  label: string;
  variant: "matched" | "missing";
}

export function KeywordBadge({ label, variant }: KeywordBadgeProps) {
  return (
    <span
      className={clsx(
        "inline-flex items-center rounded-full px-3 py-1 text-xs font-medium",
        variant === "matched"
          ? "bg-green-100 text-green-800"
          : "bg-red-100 text-red-800"
      )}
    >
      {label}
    </span>
  );
}
