"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import clsx from "clsx";
import { Button } from "@/components/ui/Button";
import { clearAuth } from "@/lib/auth";

const links = [
  { href: "/dashboard", label: "Dashboard" },
  { href: "/analyze", label: "New Analysis" },
];

export function Navbar() {
  const pathname = usePathname();
  const router = useRouter();

  function handleLogout() {
    clearAuth();
    router.push("/login");
  }

  return (
    <header className="border-b border-slate-200 bg-white">
      <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-4">
        <Link href="/dashboard" className="text-lg font-bold text-slate-900">
          AI Resume Analyzer
        </Link>

        <nav className="flex items-center gap-4">
          {links.map((link) => (
            <Link
              key={link.href}
              href={link.href}
              className={clsx(
                "text-sm font-medium",
                pathname === link.href
                  ? "text-primary-600"
                  : "text-slate-600 hover:text-slate-900"
              )}
            >
              {link.label}
            </Link>
          ))}
          <Button variant="secondary" onClick={handleLogout} className="text-xs">
            Log Out
          </Button>
        </nav>
      </div>
    </header>
  );
}
