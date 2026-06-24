package com.resumeanalyzer.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lightweight Redis-list-backed job queue for async resume analysis.
 * Producers push analysis IDs; a small worker pool pops and processes them.
 */
@Service
@Slf4j
public class QueueService {

    private static final String QUEUE_KEY = "analysis:queue";
    private static final int WORKER_COUNT = 2;
    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(5);

    private final RedisTemplate<String, String> redisTemplate;
    private final AnalysisService analysisService;

    private final ExecutorService workerPool = Executors.newFixedThreadPool(WORKER_COUNT);
    private final AtomicBoolean running = new AtomicBoolean(true);

    public QueueService(RedisTemplate<String, String> redisTemplate, @Lazy AnalysisService analysisService) {
        this.redisTemplate = redisTemplate;
        this.analysisService = analysisService;
    }

    @PostConstruct
    public void startWorkers() {
        for (int i = 0; i < WORKER_COUNT; i++) {
            workerPool.submit(this::workerLoop);
        }
        log.info("Started {} analysis queue worker(s)", WORKER_COUNT);
    }

    @PreDestroy
    public void shutdown() {
        running.set(false);
        workerPool.shutdownNow();
    }

    /**
     * Enqueues an analysis job for async processing.
     */
    public void enqueue(UUID analysisId) {
        redisTemplate.opsForList().leftPush(QUEUE_KEY, analysisId.toString());
    }

    private void workerLoop() {
        while (running.get()) {
            try {
                String analysisId = redisTemplate.opsForList().rightPop(QUEUE_KEY, POLL_TIMEOUT);
                if (analysisId != null) {
                    analysisService.processAnalysis(UUID.fromString(analysisId));
                }
            } catch (Exception ex) {
                if (running.get()) {
                    log.error("Error while processing analysis queue job", ex);
                }
            }
        }
    }
}
