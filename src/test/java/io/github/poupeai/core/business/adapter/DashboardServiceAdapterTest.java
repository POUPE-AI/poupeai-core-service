package io.github.poupeai.core.business.adapter;

import io.github.poupeai.core.domain.model.BankAccount;
import io.github.poupeai.core.domain.model.CreditCard;
import io.github.poupeai.core.domain.model.DashboardData;
import io.github.poupeai.core.domain.model.Invoice;
import io.github.poupeai.core.domain.model.InvoiceStatus;
import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.domain.model.TransactionType;
import io.github.poupeai.core.domain.port.persistence.BankAccountRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.CreditCardRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.InvoiceRepositoryPort;
import io.github.poupeai.core.domain.port.persistence.TransactionRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceAdapterTest {

        @Mock
        private BankAccountRepositoryPort bankAccountRepository;

        @Mock
        private TransactionRepositoryPort transactionRepository;

        @Mock
        private InvoiceRepositoryPort invoiceRepository;

        @Mock
        private CreditCardRepositoryPort creditCardRepository;

        @InjectMocks
        private DashboardServiceAdapter dashboardService;

        private final UUID profileId = UUID.randomUUID();

        @Test
        @DisplayName("Should calculate dashboard data for specified period")
        void shouldCalculateDashboardDataForPeriod() {
                YearMonth period = YearMonth.of(2026, 1);
                UUID bankAccountId = UUID.randomUUID();

                BankAccount bankAccount = BankAccount.builder()
                                .id(bankAccountId)
                                .profileId(profileId)
                                .initialBalance(new BigDecimal("1000.00"))
                                .build();

                when(bankAccountRepository.findAllByProfileId(profileId))
                                .thenReturn(List.of(bankAccount));

                when(transactionRepository.sumAmountByProfileIdAndTypeBeforeDate(
                                eq(profileId), eq(TransactionType.INCOME), any(LocalDate.class)))
                                .thenReturn(new BigDecimal("500.00"));
                when(transactionRepository.sumAmountByProfileIdAndTypeBeforeDate(
                                eq(profileId), eq(TransactionType.EXPENSE), any(LocalDate.class)))
                                .thenReturn(new BigDecimal("300.00"));

                when(transactionRepository.findByProfileIdAndBankAccountNotNullAndDateRange(
                                eq(profileId), any(LocalDate.class), any(LocalDate.class)))
                                .thenReturn(List.of());

                when(transactionRepository.sumAmountByProfileIdAndTypeAndDateRange(
                                eq(profileId), eq(TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class)))
                                .thenReturn(new BigDecimal("2000.00"));
                when(transactionRepository.sumAmountByProfileIdAndTypeAndDateRange(
                                eq(profileId), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                                .thenReturn(new BigDecimal("1500.00"));

                when(invoiceRepository.findByProfileIdAndMonthAndYear(profileId, 1, 2026))
                                .thenReturn(List.of());
                when(invoiceRepository.findByProfileIdAndMonthAndYear(profileId, 12, 2025))
                                .thenReturn(List.of());
                when(creditCardRepository.findAllByProfileId(profileId))
                                .thenReturn(List.of());

                DashboardData result = dashboardService.getDashboardData(profileId, period);

                assertNotNull(result);
                assertEquals(LocalDate.of(2026, 1, 1), result.getStartDate());
                assertEquals(LocalDate.of(2026, 1, 31), result.getEndDate());
                assertNotNull(result.getBalance());
                assertNotNull(result.getIncomes());
                assertNotNull(result.getExpenses());
                assertNotNull(result.getInvoices());
        }

        @Test
        @DisplayName("Should calculate correct balance totals")
        void shouldCalculateCorrectBalanceTotals() {
                YearMonth period = YearMonth.of(2026, 1);
                UUID bankAccountId = UUID.randomUUID();
                LocalDate jan15 = LocalDate.of(2026, 1, 15);

                BankAccount bankAccount = BankAccount.builder()
                                .id(bankAccountId)
                                .profileId(profileId)
                                .initialBalance(new BigDecimal("1000.00"))
                                .build();

                Transaction income = Transaction.builder()
                                .id(UUID.randomUUID())
                                .profileId(profileId)
                                .bankAccountId(bankAccountId)
                                .type(TransactionType.INCOME)
                                .amount(new BigDecimal("500.00"))
                                .transactionDate(jan15)
                                .build();

                Transaction expense = Transaction.builder()
                                .id(UUID.randomUUID())
                                .profileId(profileId)
                                .bankAccountId(bankAccountId)
                                .type(TransactionType.EXPENSE)
                                .amount(new BigDecimal("200.00"))
                                .transactionDate(jan15)
                                .build();

                when(bankAccountRepository.findAllByProfileId(profileId))
                                .thenReturn(List.of(bankAccount));

                when(transactionRepository.sumAmountByProfileIdAndTypeBeforeDate(
                                eq(profileId), eq(TransactionType.INCOME), any(LocalDate.class)))
                                .thenReturn(BigDecimal.ZERO);
                when(transactionRepository.sumAmountByProfileIdAndTypeBeforeDate(
                                eq(profileId), eq(TransactionType.EXPENSE), any(LocalDate.class)))
                                .thenReturn(BigDecimal.ZERO);

                when(transactionRepository.findByProfileIdAndBankAccountNotNullAndDateRange(
                                eq(profileId), any(LocalDate.class), any(LocalDate.class)))
                                .thenReturn(List.of(income, expense));

                when(transactionRepository.sumAmountByProfileIdAndTypeAndDateRange(
                                eq(profileId), eq(TransactionType.INCOME), any(LocalDate.class), any(LocalDate.class)))
                                .thenReturn(new BigDecimal("500.00"));
                when(transactionRepository.sumAmountByProfileIdAndTypeAndDateRange(
                                eq(profileId), eq(TransactionType.EXPENSE), any(LocalDate.class), any(LocalDate.class)))
                                .thenReturn(new BigDecimal("200.00"));

                when(invoiceRepository.findByProfileIdAndMonthAndYear(eq(profileId), anyInt(), anyInt()))
                                .thenReturn(List.of());
                when(creditCardRepository.findAllByProfileId(profileId))
                                .thenReturn(List.of());

                DashboardData result = dashboardService.getDashboardData(profileId, period);

                assertEquals(new BigDecimal("1300.00"), result.getBalance().getCurrentTotal());
                assertEquals(31, result.getBalance().getChartData().size());
        }

        @Test
        @DisplayName("Should calculate invoice summary with credit card names")
        void shouldCalculateInvoiceSummaryWithCreditCardNames() {
                YearMonth period = YearMonth.of(2026, 1);
                UUID creditCardId = UUID.randomUUID();
                UUID invoiceId = UUID.randomUUID();

                CreditCard creditCard = CreditCard.builder()
                                .id(creditCardId)
                                .profileId(profileId)
                                .name("Nubank")
                                .build();

                Invoice invoice = Invoice.builder()
                                .id(invoiceId)
                                .creditCardId(creditCardId)
                                .month(1)
                                .year(2026)
                                .totalAmount(new BigDecimal("1500.00"))
                                .status(InvoiceStatus.OPEN)
                                .dueDate(LocalDate.of(2026, 1, 20))
                                .build();

                when(bankAccountRepository.findAllByProfileId(profileId))
                                .thenReturn(List.of());
                when(transactionRepository.sumAmountByProfileIdAndTypeBeforeDate(
                                eq(profileId), any(TransactionType.class), any(LocalDate.class)))
                                .thenReturn(BigDecimal.ZERO);
                when(transactionRepository.findByProfileIdAndBankAccountNotNullAndDateRange(
                                eq(profileId), any(LocalDate.class), any(LocalDate.class)))
                                .thenReturn(List.of());
                when(transactionRepository.sumAmountByProfileIdAndTypeAndDateRange(
                                eq(profileId), any(TransactionType.class), any(LocalDate.class), any(LocalDate.class)))
                                .thenReturn(BigDecimal.ZERO);

                when(invoiceRepository.findByProfileIdAndMonthAndYear(profileId, 1, 2026))
                                .thenReturn(List.of(invoice));
                when(invoiceRepository.findByProfileIdAndMonthAndYear(profileId, 12, 2025))
                                .thenReturn(List.of());
                when(creditCardRepository.findAllByProfileId(profileId))
                                .thenReturn(List.of(creditCard));

                DashboardData result = dashboardService.getDashboardData(profileId, period);

                assertEquals(new BigDecimal("1500.00"), result.getInvoices().getCurrentTotal());
                assertEquals(1, result.getInvoices().getChartData().size());
                assertEquals("Nubank", result.getInvoices().getChartData().get(0).getCreditCardName());
                assertEquals("01/2026", result.getInvoices().getChartData().get(0).getMonth());
                assertFalse(result.getInvoices().getChartData().get(0).getPaid());
        }

        @Test
        @DisplayName("Should use last 30 days when no period specified")
        void shouldUseLast30DaysWhenNoPeriodSpecified() {
                when(bankAccountRepository.findAllByProfileId(profileId))
                                .thenReturn(List.of());
                when(transactionRepository.sumAmountByProfileIdAndTypeBeforeDate(
                                eq(profileId), any(TransactionType.class), any(LocalDate.class)))
                                .thenReturn(BigDecimal.ZERO);
                when(transactionRepository.findByProfileIdAndBankAccountNotNullAndDateRange(
                                eq(profileId), any(LocalDate.class), any(LocalDate.class)))
                                .thenReturn(List.of());
                when(transactionRepository.sumAmountByProfileIdAndTypeAndDateRange(
                                eq(profileId), any(TransactionType.class), any(LocalDate.class), any(LocalDate.class)))
                                .thenReturn(BigDecimal.ZERO);
                when(invoiceRepository.findByProfileIdAndMonthAndYear(eq(profileId), anyInt(), anyInt()))
                                .thenReturn(List.of());
                when(creditCardRepository.findAllByProfileId(profileId))
                                .thenReturn(List.of());

                DashboardData result = dashboardService.getDashboardData(profileId, null);

                assertNotNull(result);
                LocalDate today = LocalDate.now();
                assertEquals(today.minusDays(29), result.getStartDate());
                assertEquals(today, result.getEndDate());
        }

        @Test
        @DisplayName("Should return no savings when no transactions in last 3 months")
        void shouldReturnNoSavingsWhenNoTransactionsInLast3Months() {
                YearMonth period = YearMonth.of(2026, 1);

                when(bankAccountRepository.findAllByProfileId(profileId))
                                .thenReturn(List.of());
                when(transactionRepository.sumAmountByProfileIdAndTypeBeforeDate(
                                eq(profileId), any(TransactionType.class), any(LocalDate.class)))
                                .thenReturn(BigDecimal.ZERO);
                when(transactionRepository.findByProfileIdAndBankAccountNotNullAndDateRange(
                                eq(profileId), any(LocalDate.class), any(LocalDate.class)))
                                .thenReturn(List.of());
                when(transactionRepository.sumAmountByProfileIdAndTypeAndDateRange(
                                eq(profileId), any(TransactionType.class), any(LocalDate.class), any(LocalDate.class)))
                                .thenReturn(BigDecimal.ZERO);
                when(invoiceRepository.findByProfileIdAndMonthAndYear(eq(profileId), anyInt(), anyInt()))
                                .thenReturn(List.of());
                when(creditCardRepository.findAllByProfileId(profileId))
                                .thenReturn(List.of());

                DashboardData result = dashboardService.getDashboardData(profileId, period);

                assertNotNull(result.getEstimatedSaving());
                assertEquals(BigDecimal.ZERO, result.getEstimatedSaving().getEstimatedSavings());
                assertEquals("monthly", result.getEstimatedSaving().getComparisonPeriod());
        }
}
