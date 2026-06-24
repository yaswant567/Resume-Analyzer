import { useMutation } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { setToken, setUser } from "@/lib/auth";
import type { ApiResponse, AuthResponse, LoginRequest, RegisterRequest } from "@/types";

function persistAuth(auth: AuthResponse) {
  setToken(auth.token);
  setUser({ userId: auth.userId, name: auth.name, email: auth.email });
}

export function useLogin() {
  const router = useRouter();

  return useMutation({
    mutationFn: async (payload: LoginRequest) => {
      const { data } = await api.post<ApiResponse<AuthResponse>>("/auth/login", payload);
      return data.data as AuthResponse;
    },
    onSuccess: (auth) => {
      persistAuth(auth);
      router.push("/dashboard");
    },
  });
}

export function useRegister() {
  const router = useRouter();

  return useMutation({
    mutationFn: async (payload: RegisterRequest) => {
      const { data } = await api.post<ApiResponse<AuthResponse>>("/auth/register", payload);
      return data.data as AuthResponse;
    },
    onSuccess: (auth) => {
      persistAuth(auth);
      router.push("/dashboard");
    },
  });
}
