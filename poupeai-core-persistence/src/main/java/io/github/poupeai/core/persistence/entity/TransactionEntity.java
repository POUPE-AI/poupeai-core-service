package io.github.poupeai.core.persistence.entity;

import io.github.poupeai.core.domain.model.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", referencedColumnName = "user_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_transaction_profile")
    )
    private ProfileEntity profile;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TransactionType type;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "fk_transaction_bank_account")
    )
    private BankAccountEntity bankAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_card_id", referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "fk_transaction_credit_card")
    )
    private CreditCardEntity creditCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "fk_transaction_category")
    )
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "fk_transaction_invoice")
    )
    private InvoiceEntity invoice;

    @Column(name = "attachment_key", length = 255)
    private String attachmentKey;

    @Column(name = "is_installment")
    @Builder.Default
    private Boolean isInstallment = false;

    @Column(name = "installment_number")
    private Integer installmentNumber;

    @Column(name = "total_installments")
    private Integer totalInstallments;

    @Column(name = "purchase_group_uuid")
    private UUID purchaseGroupUuid;

    @Column(name = "original_statement_id", length = 100)
    private String originalStatementId;

    @Column(name = "original_statement_description", columnDefinition = "TEXT")
    private String originalStatementDescription;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
