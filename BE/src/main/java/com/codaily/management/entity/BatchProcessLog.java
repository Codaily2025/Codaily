package com.codaily.management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "batch_process_log")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchProcessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "process_date", nullable = false)
    private LocalDate processDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProcessStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "processing_type", length = 50)
    private String processingType; // DAILY_STATUS, OVERDUE_FEATURES, etc.

    public enum ProcessStatus {
        PENDING,     // 처리 대기 중
        PROCESSING,  // 처리 중
        COMPLETED,   // 완료
        FAILED,      // 실패
        SKIPPED      // 건너뜀 (변경사항 없음)
    }

    // 상태 변경 메서드들
    public void markAsProcessing() {
        this.status = ProcessStatus.PROCESSING;
        this.startedAt = LocalDateTime.now();
    }

    public void markAsCompleted() {
        this.status = ProcessStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = ProcessStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
        this.retryCount++;
    }

    public void markAsSkipped() {
        this.status = ProcessStatus.SKIPPED;
        this.completedAt = LocalDateTime.now();
    }

    public boolean canRetry() {
        return this.retryCount < 3 && this.status == ProcessStatus.FAILED;
    }
}