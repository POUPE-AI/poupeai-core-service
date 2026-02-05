package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.model.BalanceChartPoint;
import io.github.poupeai.core.domain.model.BalanceSummary;
import io.github.poupeai.core.domain.model.BankAccount;
import io.github.poupeai.core.domain.model.CategoryChartPoint;
import io.github.poupeai.core.domain.model.CategorySummary;
import io.github.poupeai.core.domain.model.CreditCard;
import io.github.poupeai.core.domain.model.DashboardData;
import io.github.poupeai.core.domain.model.Invoice;
import io.github.poupeai.core.domain.model.InvoiceChartData;
import io.github.poupeai.core.domain.model.InvoiceStatus;
import io.github.poupeai.core.domain.model.InvoicesSummary;
import io.github.poupeai.core.domain.model.SavingsEstimate;
import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.domain.model.TransactionType;
import io.github.poupeai.core.domain.port.business.DashboardServicePort;
import io.github.poupeai.core.domain.port.persistence.BankAccountRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.CreditCardRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.InvoiceRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceAdapter implements DashboardServicePort {
        private final BankAccountRepositoryPort bankAccountRepository;
        private final TransactionRepositoryPort transactionRepository;
        private final InvoiceRepositoryPort invoiceRepository;
        private final CreditCardRepositoryPort creditCardRepository;

        @Override
        public DashboardData getDashboardData(UUID profileId, YearMonth period) {
                LocalDate startDate;
                LocalDate endDate;

                if (period != null) {
                        startDate = period.atDay(1);
                        endDate = period.plusMonths(1).atDay(1);
                } else {
                        LocalDate today = LocalDate.now();
                        endDate = today.plusDays(1);
                        startDate = today.minusDays(29);
                }

                BalanceSummary balance = calculateBalanceSummary(profileId, startDate, endDate);
                CategorySummary incomes = calculateCategorySummary(profileId, TransactionType.INCOME, startDate,
                                endDate);
                CategorySummary expenses = calculateCategorySummary(profileId, TransactionType.EXPENSE, startDate,
                                endDate);
                InvoicesSummary invoices = calculateInvoicesSummary(profileId, period);
                SavingsEstimate estimatedSaving = calculateSavingsEstimate(profileId);

                return DashboardData.builder()
                                .startDate(startDate)
                                .endDate(endDate.minusDays(1))
                                .balance(balance)
                                .incomes(incomes)
                                .expenses(expenses)
                                .invoices(invoices)
                                .estimatedSaving(estimatedSaving)
                                .build();
        }

        private BalanceSummary calculateBalanceSummary(UUID profileId, LocalDate startDate, LocalDate endDate) {
                List<BankAccount> bankAccounts = bankAccountRepository.findAllByProfileId(profileId);
                BigDecimal initialBalanceSum = bankAccounts.stream()
                                .map(acc -> acc.getInitialBalance() != null ? acc.getInitialBalance() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal incomesBeforePeriod = transactionRepository.sumAmountByProfileIdAndTypeBeforeDate(
                                profileId, TransactionType.INCOME, startDate);
                BigDecimal expensesBeforePeriod = transactionRepository.sumAmountByProfileIdAndTypeBeforeDate(
                                profileId, TransactionType.EXPENSE, startDate);

                BigDecimal initialBalance = initialBalanceSum.add(incomesBeforePeriod).subtract(expensesBeforePeriod);

                List<Transaction> transactions = transactionRepository.findByProfileIdAndBankAccountNotNullAndDateRange(
                                profileId, startDate, endDate);

                Map<LocalDate, BigDecimal> dailyIncomes = new HashMap<>();
                Map<LocalDate, BigDecimal> dailyExpenses = new HashMap<>();

                for (Transaction t : transactions) {
                        LocalDate date = t.getTransactionDate();
                        BigDecimal amount = t.getAmount();
                        if (t.getType() == TransactionType.INCOME) {
                                dailyIncomes.merge(date, amount, BigDecimal::add);
                        } else {
                                dailyExpenses.merge(date, amount, BigDecimal::add);
                        }
                }

                List<BalanceChartPoint> chartData = new ArrayList<>();
                BigDecimal runningBalance = initialBalance;

                for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
                        BigDecimal dayIncome = dailyIncomes.getOrDefault(date, BigDecimal.ZERO);
                        BigDecimal dayExpense = dailyExpenses.getOrDefault(date, BigDecimal.ZERO);
                        runningBalance = runningBalance.add(dayIncome).subtract(dayExpense);

                        chartData.add(BalanceChartPoint.builder()
                                        .date(date)
                                        .balance(runningBalance)
                                        .build());
                }

                BigDecimal currentTotal = runningBalance;
                BigDecimal difference = calculatePercentDifference(initialBalance, currentTotal);

                return BalanceSummary.builder()
                                .currentTotal(currentTotal)
                                .difference(difference)
                                .chartData(chartData)
                                .build();
        }

        private CategorySummary calculateCategorySummary(UUID profileId, TransactionType type, LocalDate startDate,
                        LocalDate endDate) {
                BigDecimal currentTotal = transactionRepository.sumAmountByProfileIdAndTypeAndDateRange(
                                profileId, type, startDate, endDate);

                BigDecimal previousTotal = transactionRepository.sumAmountByProfileIdAndTypeBeforeDate(
                                profileId, type, startDate);

                BigDecimal difference = calculatePercentDifference(previousTotal, currentTotal);

                List<Transaction> transactions = transactionRepository.findByProfileIdAndBankAccountNotNullAndDateRange(
                                profileId, startDate, endDate);

                Map<LocalDate, BigDecimal> dailyTotals = new HashMap<>();
                for (Transaction t : transactions) {
                        if (t.getType() == type) {
                                dailyTotals.merge(t.getTransactionDate(), t.getAmount(), BigDecimal::add);
                        }
                }

                List<CategoryChartPoint> chartData = new ArrayList<>();
                for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
                        chartData.add(CategoryChartPoint.builder()
                                        .date(date)
                                        .total(dailyTotals.getOrDefault(date, BigDecimal.ZERO))
                                        .build());
                }

                return CategorySummary.builder()
                                .currentTotal(currentTotal)
                                .difference(difference)
                                .chartData(chartData)
                                .build();
        }

        private InvoicesSummary calculateInvoicesSummary(UUID profileId, YearMonth period) {
                YearMonth currentPeriod = period != null ? period : YearMonth.now();
                YearMonth previousPeriod = currentPeriod.minusMonths(1);

                List<Invoice> currentInvoices = invoiceRepository.findByProfileIdAndMonthAndYear(
                                profileId, currentPeriod.getMonthValue(), currentPeriod.getYear());
                List<Invoice> previousInvoices = invoiceRepository.findByProfileIdAndMonthAndYear(
                                profileId, previousPeriod.getMonthValue(), previousPeriod.getYear());

                Map<UUID, CreditCard> creditCards = creditCardRepository.findAllByProfileId(profileId).stream()
                                .collect(Collectors.toMap(CreditCard::getId, Function.identity()));

                BigDecimal currentTotal = currentInvoices.stream()
                                .map(inv -> inv.getTotalAmount() != null ? inv.getTotalAmount() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal previousTotal = previousInvoices.stream()
                                .map(inv -> inv.getTotalAmount() != null ? inv.getTotalAmount() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal difference = calculatePercentDifference(previousTotal, currentTotal);

                List<InvoiceChartData> chartData = currentInvoices.stream()
                                .map(invoice -> {
                                        CreditCard card = creditCards.get(invoice.getCreditCardId());
                                        String cardName = card != null ? card.getName() : "Cartão desconhecido";
                                        boolean isPaid = invoice.getStatus() == InvoiceStatus.PAID;

                                        return InvoiceChartData.builder()
                                                        .creditCardName(cardName)
                                                        .month(String.format("%02d/%d", invoice.getMonth(),
                                                                        invoice.getYear()))
                                                        .totalAmount(invoice.getTotalAmount() != null
                                                                        ? invoice.getTotalAmount()
                                                                        : BigDecimal.ZERO)
                                                        .paid(isPaid)
                                                        .dueDate(invoice.getDueDate())
                                                        .build();
                                })
                                .collect(Collectors.toList());

                return InvoicesSummary.builder()
                                .currentTotal(currentTotal)
                                .difference(difference)
                                .chartData(chartData)
                                .build();
        }

        private BigDecimal calculatePercentDifference(BigDecimal previous, BigDecimal current) {
                if (previous.compareTo(BigDecimal.ZERO) == 0) {
                        if (current.compareTo(BigDecimal.ZERO) > 0) {
                                return new BigDecimal("100.0");
                        } else if (current.compareTo(BigDecimal.ZERO) < 0) {
                                return new BigDecimal("-100.0");
                        }
                        return BigDecimal.ZERO;
                }

                BigDecimal diff = current.subtract(previous);
                return diff.divide(previous.abs(), 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"))
                                .setScale(2, RoundingMode.HALF_UP);
        }

        private SavingsEstimate calculateSavingsEstimate(UUID profileId) {
                LocalDate today = LocalDate.now();
                LocalDate startDateThreeMonthsAgo = today.withDayOfMonth(1).minusMonths(3);

                BigDecimal totalExpenses = transactionRepository.sumAmountByProfileIdAndTypeAndDateRange(
                                profileId, TransactionType.EXPENSE, startDateThreeMonthsAgo, today.plusDays(1));

                if (totalExpenses.compareTo(BigDecimal.ZERO) == 0) {
                        return SavingsEstimate.builder()
                                        .estimatedSavings(BigDecimal.ZERO)
                                        .savingsPercentage(BigDecimal.ZERO)
                                        .message("Não há transações recentes para calcular a economia.")
                                        .comparisonPeriod("monthly")
                                        .build();
                }

                LocalDate endCurrentPeriod = today.withDayOfMonth(1).minusDays(1);
                LocalDate startCurrentPeriod = endCurrentPeriod.withDayOfMonth(1);

                LocalDate endPreviousPeriod = startCurrentPeriod.minusDays(1);
                LocalDate startPreviousPeriod = endPreviousPeriod.withDayOfMonth(1);

                BigDecimal currentPeriodExpenses = transactionRepository.sumAmountByProfileIdAndTypeAndDateRange(
                                profileId, TransactionType.EXPENSE, startCurrentPeriod, endCurrentPeriod.plusDays(1));
                BigDecimal previousPeriodExpenses = transactionRepository.sumAmountByProfileIdAndTypeAndDateRange(
                                profileId, TransactionType.EXPENSE, startPreviousPeriod, endPreviousPeriod.plusDays(1));

                if (previousPeriodExpenses.compareTo(BigDecimal.ZERO) == 0) {
                        return SavingsEstimate.builder()
                                        .estimatedSavings(BigDecimal.ZERO)
                                        .savingsPercentage(BigDecimal.ZERO)
                                        .message("Não foi possível calcular a economia pois não há dados de despesas do mês retrasado para comparação.")
                                        .comparisonPeriod("monthly")
                                        .build();
                }

                BigDecimal difference = previousPeriodExpenses.subtract(currentPeriodExpenses);
                BigDecimal percentageChange = difference.divide(previousPeriodExpenses, 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"))
                                .setScale(2, RoundingMode.HALF_UP);

                String message;
                if (difference.compareTo(BigDecimal.ZERO) > 0) {
                        message = String.format("Houve uma economia de R$ %.2f em relação ao mês anterior, " +
                                        "representando uma diminuição de %.2f%% nos gastos.",
                                        difference, percentageChange);
                } else if (difference.compareTo(BigDecimal.ZERO) < 0) {
                        BigDecimal absoluteDifference = difference.abs();
                        BigDecimal absolutePercentage = percentageChange.abs();
                        message = String.format("Houve um aumento de R$ %.2f nos gastos em relação ao mês anterior, " +
                                        "representando um aumento de %.2f%%.",
                                        absoluteDifference, absolutePercentage);
                } else {
                        message = "Seus gastos permaneceram os mesmos em relação ao mês anterior.";
                }

                return SavingsEstimate.builder()
                                .estimatedSavings(difference.setScale(2, RoundingMode.HALF_UP))
                                .savingsPercentage(percentageChange)
                                .message(message)
                                .comparisonPeriod("monthly")
                                .build();
        }
}
