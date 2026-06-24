package com.resumeanalyzer.model.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "analyses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Analysis {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "resume_text", columnDefinition = "TEXT")
    private String resumeText;

    @Column(name = "job_description", nullable = false, columnDefinition = "TEXT")
    private String jobDescription;

    @Column(name = "match_score")
    private Integer matchScore;

    @Type(JsonType.class)
    @Column(name = "matched_keywords", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> matchedKeywords = List.of();

    @Type(JsonType.class)
    @Column(name = "missing_keywords", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> missingKeywords = List.of();

    @Type(JsonType.class)
    @Column(name = "strengths", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> strengths = List.of();

    @Type(JsonType.class)
    @Column(name = "improvements", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> improvements = List.of();

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AnalysisStatus status = AnalysisStatus.PENDING;

    @Column(name = "error_message")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
