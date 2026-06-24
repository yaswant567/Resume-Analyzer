package com.resumeanalyzer.controller;

import com.resumeanalyzer.model.dto.ApiResponse;
import com.resumeanalyzer.model.dto.AnalysisResponse;
import com.resumeanalyzer.model.dto.AnalysisStatusResponse;
import com.resumeanalyzer.security.UserPrincipal;
import com.resumeanalyzer.service.AnalysisService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/analysis")
@Validated
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping(value = "/submit", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<AnalysisResponse>> submit(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam("jobDescription")
            @NotBlank(message = "Job description is required")
            @Size(min = 50, max = 10000, message = "Job description must be between 50 and 10000 characters")
            String jobDescription
    ) {
        AnalysisResponse response = analysisService.submit(principal.id(), file, jobDescription);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(response, "Analysis submitted and queued for processing"));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AnalysisResponse>>> getAll(@AuthenticationPrincipal UserPrincipal principal) {
        List<AnalysisResponse> analyses = analysisService.getAllForUser(principal.id());
        return ResponseEntity.ok(ApiResponse.success(analyses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AnalysisResponse>> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        AnalysisResponse response = analysisService.getById(principal.id(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AnalysisStatusResponse>> getStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        AnalysisStatusResponse response = analysisService.getStatus(principal.id(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
