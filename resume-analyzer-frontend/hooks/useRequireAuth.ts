"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { getUser, isAuthenticated, type StoredUser } from "@/lib/auth";

export function useRequireAuth() {
  const router = useRouter();
  const [user, setUser] = useState<StoredUser | null>(null);
  const [checked, setChecked] = useState(false);

  useEffect(() => {
    if (!isAuthenticated()) {
      router.replace("/login");
      return;
    }
    setUser(getUser());
    setChecked(true);
  }, [router]);

  return { user, checked };
}
