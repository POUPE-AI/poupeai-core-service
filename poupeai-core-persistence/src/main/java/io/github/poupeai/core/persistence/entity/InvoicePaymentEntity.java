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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "invoice_payments",
    uniqueConstraints = @UniqueConstraint(
        name = "idx_invoice_payments_invoice_transaction_unique",
        columnNames = {"invoice_id", "payment_transaction_id"}
    )
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoicePaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", referencedColumnName = "id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_invoice_payment_invoice")
    )
    private InvoiceEntity invoice;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_transaction_id", referencedColumnName = "id", nullable = false, unique = true,
        foreignKey = @ForeignKey(name = "fk_invoice_payment_transaction")
    )
    private TransactionEntity paymentTransaction;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
