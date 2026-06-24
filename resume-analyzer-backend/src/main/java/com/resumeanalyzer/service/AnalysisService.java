package com.resumeanalyzer.service;

import com.resumeanalyzer.exception.CustomExceptions.RateLimitExceededException;
import com.resumeanalyzer.exception.CustomExceptions.ResourceNotFoundException;
import com.resumeanalyzer.model.dto.AIAnalysisResult;
import com.resumeanalyzer.model.dto.AnalysisResponse;
import com.resumeanalyzer.model.dto.AnalysisStatusResponse;
import com.resumeanalyzer.model.entity.Analysis;
import com.resumeanalyzer.model.entity.AnalysisStatus;
import com.resumeanalyzer.model.entity.User;
import com.resumeanalyzer.repository.AnalysisRepository;
import com.resumeanalyzer.repository.UserRepository;
import com.resumeanalyzer.service.ai.AIProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final UserRepository userRepository;
    private final PDFService pdfService;
    private final AIProvider aiProvider;
    private final QueueService queueService;

    @Value("${app.rate-limit.daily-analysis-limit}")
    private int dailyAnalysisLimit;

    /**
     * Validates the upload, enforces the per-user daily rate limit, persists a
     * PENDING analysis record, and enqueues it for async AI processing.
     */
    @Transactional
    public AnalysisResponse submit(UUID userId, MultipartFile file, String jobDescription) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        enforceRateLimit(user);

        String resumeText = pdfService.extractText(file);

        Analysis analysis = Analysis.builder()
                .user(user)
                .resumeText(resumeText)
                .jobDescription(jobDescription)
                .status(AnalysisStatus.PENDING)
                .build();

        Analysis saved = analysisRepository.save(analysis);

        recordUsage(user);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                queueService.enqueue(saved.getId());
            }
        });

        return AnalysisResponse.from(saved);
    }

    /**
     * Runs synchronously on a queue worker thread. Calls Claude, stores the
     * result, and marks the analysis COMPLETED or FAILED.
     */
    @Transactional
    public void processAnalysis(UUID analysisId) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found: " + analysisId));

        analysis.setStatus(AnalysisStatus.PROCESSING);
        analysisRepository.save(analysis);

        try {
            AIAnalysisResult result = aiProvider.analyze(analysis.getResumeText(), analysis.getJobDescription());

            analysis.setMatchScore(result.matchScore());
            analysis.setMatchedKeywords(result.matchedKeywords() != null ? result.matchedKeywords() : List.of());
            analysis.setMissingKeywords(result.missingKeywords() != null ? result.missingKeywords() : List.of());
            analysis.setStrengths(result.strengths() != null ? result.strengths() : List.of());
            analysis.setImprovements(result.improvements() != null ? result.improvements() : List.of());
            analysis.setSummary(result.summary());
            analysis.setStatus(AnalysisStatus.COMPLETED);
        } catch (Exception ex) {
            log.error("Analysis {} failed", analysisId, ex);
            analysis.setStatus(AnalysisStatus.FAILED);
            analysis.setErrorMessage("Analysis failed: " + ex.getMessage());
        }

        analysisRepository.save(analysis);
    }

    @Transactional(readOnly = true)
    public List<AnalysisResponse> getAllForUser(UUID userId) {
        return analysisRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(AnalysisResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AnalysisResponse getById(UUID userId, UUID analysisId) {
        Analysis analysis = analysisRepository.findByIdAndUserId(analysisId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found: " + analysisId));
        return AnalysisResponse.from(analysis);
    }

    @Transactional(readOnly = true)
    public AnalysisStatusResponse getStatus(UUID userId, UUID analysisId) {
        Analysis analysis = analysisRepository.findByIdAndUserId(analysisId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found: " + analysisId));
        return new AnalysisStatusResponse(analysis.getId(), analysis.getStatus(), analysis.getErrorMessage());
    }

    private void enforceRateLimit(User user) {
        LocalDate today = LocalDate.now();

        if (today.equals(user.getLastAnalysisDate()) && user.getDailyAnalysisCount() >= dailyAnalysisLimit) {
            throw new RateLimitExceededException(
                    "Daily analysis limit of " + dailyAnalysisLimit + " reached. Please try again tomorrow.");
        }
    }

    private void recordUsage(User user) {
        LocalDate today = LocalDate.now();

        if (today.equals(user.getLastAnalysisDate())) {
            user.setDailyAnalysisCount(user.getDailyAnalysisCount() + 1);
        } else {
            user.setLastAnalysisDate(today);
            user.setDailyAnalysisCount(1);
        }

        userRepository.save(user);
    }
}
