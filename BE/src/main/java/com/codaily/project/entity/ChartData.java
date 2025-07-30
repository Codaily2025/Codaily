package com.codaily.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "chart_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChartData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chart_id")
    private Long chartId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "retro_id")
    private Long retroId;

    @Column(name = "type", nullable = false, length = 50)
    private String type; // "productivity" 등

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> dataJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "granularity", length = 10)
    private String granularity; // 'daily', 'weekly', 'monthly'

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "project_id")
    private Long projectId;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}