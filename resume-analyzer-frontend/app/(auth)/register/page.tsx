"use client";

import Link from "next/link";
import { FormEvent, useState } from "react";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { extractErrorMessage } from "@/lib/api";
import { useRegister } from "@/hooks/useAuth";

export default function RegisterPage() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [validationError, setValidationError] = useState<string | null>(null);
  const register = useRegister();

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setValidationError(null);

    if (name.trim().length < 2) {
      setValidationError("Name must be at least 2 characters");
      return;
    }
    if (password.length < 8) {
      setValidationError("Password must be at least 8 characters");
      return;
    }

    register.mutate({ name, email, password });
  }

  return (
    <main className="flex min-h-screen items-center justify-center px-4">
      <Card className="w-full max-w-md">
        <h1 className="text-2xl font-bold text-slate-900">Create your account</h1>
        <p className="mt-1 text-sm text-slate-600">
          Sign up to start analyzing your resume against job descriptions.
        </p>

        <form onSubmit={handleSubmit} className="mt-6 flex flex-col gap-4">
          <Input
            id="name"
            type="text"
            label="Full name"
            placeholder="Jane Doe"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
          <Input
            id="email"
            type="email"
            label="Email"
            placeholder="you@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <Input
            id="password"
            type="password"
            label="Password"
            placeholder="At least 8 characters"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={8}
          />

          {(validationError || register.isError) && (
            <p className="text-sm text-red-600">
              {validationError ?? extractErrorMessage(register.error)}
            </p>
          )}

          <Button type="submit" isLoading={register.isPending} className="mt-2 w-full">
            Sign Up
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-600">
          Already have an account?{" "}
          <Link href="/login" className="font-semibold text-primary-600 hover:underline">
            Log in
          </Link>
        </p>
      </Card>
    </main>
  );
}
