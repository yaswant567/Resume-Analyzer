import Link from "next/link";

export default function HomePage() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center px-6 text-center">
      <h1 className="text-4xl font-bold tracking-tight text-slate-900 sm:text-5xl">
        AI Resume Analyzer
      </h1>
      <p className="mt-4 max-w-xl text-lg text-slate-600">
        Upload your resume and a job description to get an instant ATS match score,
        keyword analysis, and personalized improvement suggestions.
      </p>
      <div className="mt-8 flex gap-4">
        <Link
          href="/login"
          className="rounded-lg bg-primary-600 px-6 py-3 text-sm font-semibold text-white shadow hover:bg-primary-700"
        >
          Log In
        </Link>
        <Link
          href="/register"
          className="rounded-lg border border-slate-300 bg-white px-6 py-3 text-sm font-semibold text-slate-700 shadow-sm hover:bg-slate-50"
        >
          Sign Up
        </Link>
      </div>
    </main>
  );
}
