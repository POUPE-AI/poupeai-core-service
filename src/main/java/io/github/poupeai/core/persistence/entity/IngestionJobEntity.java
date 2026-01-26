package io.github.poupeai.core.persistence.entity;

import io.github.poupeai.core.domain.model.DestinationType;
import io.github.poupeai.core.domain.model.JobStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ingestion_jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngestionJobEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "job_status", nullable = false)
    private JobStatus status;

    @Column(name = "file_key_minio", nullable = false)
    private String fileKeyMinio;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "destination_entity_type", columnDefinition = "destination_type", nullable = false)
    private DestinationType destinationEntityType;

    @Column(name = "destination_entity_id", nullable = false)
    private UUID destinationEntityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String summary;

    @Column(name = "error_details", columnDefinition = "text")
    private String errorDetails;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
