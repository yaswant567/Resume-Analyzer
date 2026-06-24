import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type {
  AnalysisResponse,
  AnalysisStatusResponse,
  ApiResponse,
} from "@/types";

const ACTIVE_STATUSES = new Set(["PENDING", "PROCESSING"]);

export function useAnalysisList() {
  return useQuery({
    queryKey: ["analyses"],
    queryFn: async () => {
      const { data } = await api.get<ApiResponse<AnalysisResponse[]>>("/analysis/all");
      return data.data ?? [];
    },
  });
}

export function useAnalysis(id: string | undefined) {
  return useQuery({
    queryKey: ["analysis", id],
    queryFn: async () => {
      const { data } = await api.get<ApiResponse<AnalysisResponse>>(`/analysis/${id}`);
      return data.data as AnalysisResponse;
    },
    enabled: !!id,
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      return status && ACTIVE_STATUSES.has(status) ? 3000 : false;
    },
  });
}

export function useAnalysisStatus(id: string | undefined) {
  return useQuery({
    queryKey: ["analysis-status", id],
    queryFn: async () => {
      const { data } = await api.get<ApiResponse<AnalysisStatusResponse>>(
        `/analysis/${id}/status`
      );
      return data.data as AnalysisStatusResponse;
    },
    enabled: !!id,
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      return status && ACTIVE_STATUSES.has(status) ? 3000 : false;
    },
  });
}

export function useSubmitAnalysis() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ file, jobDescription }: { file: File; jobDescription: string }) => {
      const formData = new FormData();
      formData.append("file", file);
      formData.append("jobDescription", jobDescription);

      const { data } = await api.post<ApiResponse<AnalysisResponse>>(
        "/analysis/submit",
        formData,
        { headers: { "Content-Type": "multipart/form-data" } }
      );
      return data.data as AnalysisResponse;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["analyses"] });
    },
  });
}
