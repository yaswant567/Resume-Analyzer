export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  message: string | null;
  timestamp: string;
}

export interface AuthResponse {
  userId: string;
  name: string;
  email: string;
  token: string;
  tokenType: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export type AnalysisStatus = "PENDING" | "PROCESSING" | "COMPLETED" | "FAILED";

export interface AnalysisResponse {
  id: string;
  matchScore: number | null;
  matchedKeywords: string[];
  missingKeywords: string[];
  strengths: string[];
  improvements: string[];
  summary: string | null;
  status: AnalysisStatus;
  errorMessage: string | null;
  createdAt: string;
}

export interface AnalysisStatusResponse {
  id: string;
  status: AnalysisStatus;
  errorMessage: string | null;
}

export interface ApiErrorResponse {
  success: false;
  data: Record<string, string> | null;
  message: string;
  timestamp: string;
}
