package io.github.poupeai.core.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "goals")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GoalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", referencedColumnName = "user_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_goal_profile")
    )
    private ProfileEntity profile;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "goal_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal goalAmount;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "completed_at")
    private LocalDate completedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
