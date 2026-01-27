package io.github.poupeai.core.domain.model;

import io.github.poupeai.core.domain.exception.DomainException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.With;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Transaction {
    private UUID id;
    private UUID profileId;
    private String description;
    private BigDecimal amount;
    @With private TransactionType type;
    private LocalDate transactionDate;

    private UUID bankAccountId;
    private UUID creditCardId;

    private Category category;
    @With private UUID invoiceId;
    @With private String attachmentKey;

    @Builder.Default
    private Boolean isInstallment = false;
    private Integer installmentNumber;
    private Integer totalInstallments;
    private UUID purchaseGroupUuid;

    private String originalStatementId;
    private String originalStatementDescription;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public void validateCreationState() {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("O valor da transação deve ser maior que zero.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new DomainException("A descrição é obrigatória.");
        }
        if (category == null || category.getId() == null) {
            throw new DomainException("A categoria é obrigatória.");
        }
        validateSourceExclusivity();

        if (Boolean.TRUE.equals(isInstallment)) {
            validateInstallmentRules();
        }
    }

    private void validateSourceExclusivity() {
        if (bankAccountId == null && creditCardId == null) {
            throw new DomainException("A transação deve estar associada a uma conta bancária ou cartão de crédito.");
        }
        if (bankAccountId != null && creditCardId != null) {
            throw new DomainException("A transação não pode ter conta e cartão simultaneamente.");
        }
        if (creditCardId != null && type == TransactionType.INCOME) {
            throw new DomainException("Transações de cartão de crédito devem ser do tipo despesa.");
        }
    }

    private void validateInstallmentRules() {
        if (bankAccountId != null) {
            throw new DomainException("Parcelamento só é permitido para transações de cartão de crédito.");
        }
        if (totalInstallments == null || totalInstallments < 2) {
            throw new DomainException("Uma transação parcelada deve ter pelo menos 2 parcelas.");
        }
        if (totalInstallments > 48) {
            throw new DomainException("O número máximo de parcelas é 48.");
        }
    }

    public List<Transaction> generateInstallments() {
        if (Boolean.FALSE.equals(isInstallment)) {
            return List.of(this);
        }

        List<Transaction> installments = new ArrayList<>();
        BigDecimal installmentAmount = amount.divide(BigDecimal.valueOf(totalInstallments), 2, RoundingMode.HALF_UP);
        BigDecimal remainder = amount.subtract(installmentAmount.multiply(BigDecimal.valueOf(totalInstallments)));

        UUID newPurchaseGroupUuid = UUID.randomUUID();
        LocalDate baseDate = this.transactionDate;

        for (int i = 1; i <= totalInstallments; i++) {
            BigDecimal currentAmount = (i == totalInstallments) ? installmentAmount.add(remainder) : installmentAmount;

            Transaction installment = this.toBuilder()
                    .id(null)
                    .description(String.format("%s (%d/%d)", this.description, i, totalInstallments))
                    .amount(currentAmount)
                    .transactionDate(baseDate.plusMonths(i - 1))
                    .isInstallment(true)
                    .installmentNumber(i)
                    .purchaseGroupUuid(newPurchaseGroupUuid)
                    .build();

            installments.add(installment);
        }
        return installments;
    }
}
