"use client";

import { ChangeEvent, DragEvent, useId, useState } from "react";
import clsx from "clsx";

interface UploadZoneProps {
  file: File | null;
  onFileSelect: (file: File | null) => void;
  error?: string;
}

const MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5MB

export function UploadZone({ file, onFileSelect, error }: UploadZoneProps) {
  const [isDragging, setIsDragging] = useState(false);
  const [localError, setLocalError] = useState<string | null>(null);
  const inputId = useId();

  function validateAndSelect(selected: File | undefined | null) {
    if (!selected) return;

    if (selected.type !== "application/pdf" && !selected.name.toLowerCase().endsWith(".pdf")) {
      setLocalError("Only PDF files are supported");
      return;
    }

    if (selected.size > MAX_FILE_SIZE_BYTES) {
      setLocalError("File size must be 5MB or less");
      return;
    }

    setLocalError(null);
    onFileSelect(selected);
  }

  function handleDrop(e: DragEvent<HTMLDivElement>) {
    e.preventDefault();
    setIsDragging(false);
    validateAndSelect(e.dataTransfer.files?.[0]);
  }

  function handleChange(e: ChangeEvent<HTMLInputElement>) {
    validateAndSelect(e.target.files?.[0]);
  }

  const displayError = error ?? localError ?? undefined;

  return (
    <div className="flex flex-col gap-1.5">
      <label className="text-sm font-medium text-slate-700" htmlFor={inputId}>
        Resume (PDF)
      </label>
      <div
        onDragOver={(e) => {
          e.preventDefault();
          setIsDragging(true);
        }}
        onDragLeave={() => setIsDragging(false)}
        onDrop={handleDrop}
        className={clsx(
          "flex flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed px-6 py-10 text-center transition-colors",
          isDragging ? "border-primary-500 bg-primary-50" : "border-slate-300 bg-slate-50",
          displayError && "border-red-400"
        )}
      >
        <svg
          className="h-10 w-10 text-slate-400"
          fill="none"
          viewBox="0 0 24 24"
          strokeWidth={1.5}
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5M16.5 12L12 16.5m0 0L7.5 12m4.5 4.5V3"
          />
        </svg>

        {file ? (
          <p className="text-sm font-medium text-slate-700">{file.name}</p>
        ) : (
          <p className="text-sm text-slate-600">
            Drag and drop your resume here, or{" "}
            <label htmlFor={inputId} className="cursor-pointer font-semibold text-primary-600 hover:underline">
              browse
            </label>
          </p>
        )}

        <p className="text-xs text-slate-400">PDF up to 5MB</p>

        <input
          id={inputId}
          type="file"
          accept="application/pdf,.pdf"
          className="sr-only"
          onChange={handleChange}
        />

        {file && (
          <button
            type="button"
            onClick={() => onFileSelect(null)}
            className="text-xs font-medium text-red-600 hover:underline"
          >
            Remove file
          </button>
        )}
      </div>
      {displayError && <p className="text-sm text-red-600">{displayError}</p>}
    </div>
  );
}
