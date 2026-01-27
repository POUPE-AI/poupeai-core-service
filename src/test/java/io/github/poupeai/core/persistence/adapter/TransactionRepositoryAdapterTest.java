package io.github.poupeai.core.persistence.adapter;

import io.github.poupeai.core.domain.model.Category;
import io.github.poupeai.core.domain.model.PageDomain;
import io.github.poupeai.core.domain.model.Transaction;
import io.github.poupeai.core.domain.model.TransactionFilter;
import io.github.poupeai.core.domain.model.TransactionType;
import io.github.poupeai.core.persistence.entity.*;
import io.github.poupeai.core.persistence.mapper.TransactionEntityMapper;
import io.github.poupeai.core.persistence.repository.BankAccountRepository;
import io.github.poupeai.core.persistence.repository.CategoryRepository;
import io.github.poupeai.core.persistence.repository.CreditCardRepository;
import io.github.poupeai.core.persistence.repository.InvoiceRepository;
import io.github.poupeai.core.persistence.repository.ProfileRepository;
import io.github.poupeai.core.persistence.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionRepositoryAdapterTest {

    @InjectMocks
    private TransactionRepositoryAdapter adapter;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionEntityMapper transactionMapper;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private CreditCardRepository creditCardRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Test
    @DisplayName("Should create transaction with bank account")
    void shouldCreateTransactionWithBankAccount() {
        UUID profileId = UUID.randomUUID();
        UUID bankAccountId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Transaction domain = Transaction.builder()
                .profileId(profileId)
                .bankAccountId(bankAccountId)
                .category(Category.builder().id(categoryId).build())
                .description("Test")
                .amount(BigDecimal.valueOf(100))
                .build();

        TransactionEntity entity = new TransactionEntity();
        ProfileEntity profileProxy = new ProfileEntity();
        BankAccountEntity bankAccountProxy = new BankAccountEntity();
        CategoryEntity categoryProxy = new CategoryEntity();

        when(transactionMapper.toEntity(domain)).thenReturn(entity);
        when(profileRepository.getReferenceById(profileId)).thenReturn(profileProxy);
        when(bankAccountRepository.getReferenceById(bankAccountId)).thenReturn(bankAccountProxy);
        when(categoryRepository.getReferenceById(categoryId)).thenReturn(categoryProxy);
        when(transactionRepository.save(entity)).thenReturn(entity);
        when(transactionMapper.toDomain(entity)).thenReturn(domain);

        Transaction result = adapter.create(domain);

        assertNotNull(result);
        verify(transactionRepository).save(entity);
        assertEquals(profileProxy, entity.getProfile());
        assertEquals(bankAccountProxy, entity.getBankAccount());
        assertEquals(categoryProxy, entity.getCategory());
    }

    @Test
    @DisplayName("Should create transaction with credit card and invoice")
    void shouldCreateTransactionWithCreditCardAndInvoice() {
        UUID profileId = UUID.randomUUID();
        UUID creditCardId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();

        Transaction domain = Transaction.builder()
                .profileId(profileId)
                .creditCardId(creditCardId)
                .category(Category.builder().id(categoryId).build())
                .invoiceId(invoiceId)
                .description("Test")
                .amount(BigDecimal.valueOf(100))
                .build();

        TransactionEntity entity = new TransactionEntity();
        ProfileEntity profileProxy = new ProfileEntity();
        CreditCardEntity creditCardProxy = new CreditCardEntity();
        CategoryEntity categoryProxy = new CategoryEntity();
        InvoiceEntity invoiceProxy = new InvoiceEntity();

        when(transactionMapper.toEntity(domain)).thenReturn(entity);
        when(profileRepository.getReferenceById(profileId)).thenReturn(profileProxy);
        when(creditCardRepository.getReferenceById(creditCardId)).thenReturn(creditCardProxy);
        when(categoryRepository.getReferenceById(categoryId)).thenReturn(categoryProxy);
        when(invoiceRepository.getReferenceById(invoiceId)).thenReturn(invoiceProxy);
        when(transactionRepository.save(entity)).thenReturn(entity);
        when(transactionMapper.toDomain(entity)).thenReturn(domain);

        Transaction result = adapter.create(domain);

        assertNotNull(result);
        assertEquals(creditCardProxy, entity.getCreditCard());
        assertEquals(invoiceProxy, entity.getInvoice());
    }

    @Test
    @DisplayName("Should update transaction successfully (via save/upsert)")
    void shouldUpdateTransactionSuccessfully() {
        UUID transactionId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Transaction domain = Transaction.builder()
                .id(transactionId)
                .profileId(profileId)
                .category(Category.builder().id(categoryId).build())
                .description("Updated")
                .amount(BigDecimal.valueOf(200))
                .build();

        TransactionEntity entity = new TransactionEntity();
        ProfileEntity profileProxy = new ProfileEntity();
        CategoryEntity categoryProxy = new CategoryEntity();

        when(transactionMapper.toEntity(domain)).thenReturn(entity);
        when(profileRepository.getReferenceById(profileId)).thenReturn(profileProxy);
        when(categoryRepository.getReferenceById(categoryId)).thenReturn(categoryProxy);
        when(transactionRepository.save(entity)).thenReturn(entity);
        when(transactionMapper.toDomain(entity)).thenReturn(domain);

        Transaction result = adapter.update(domain);

        assertNotNull(result);
        verify(transactionRepository).save(entity);
        assertEquals(profileProxy, entity.getProfile());
        assertEquals(categoryProxy, entity.getCategory());
    }

    @Test
    @DisplayName("Should create all transactions using saveAll")
    void shouldCreateAllTransactions() {
        UUID profileId = UUID.randomUUID();
        Transaction transaction = Transaction.builder()
                .profileId(profileId)
                .description("Test")
                .build();
        List<Transaction> transactions = List.of(transaction);

        TransactionEntity entity = new TransactionEntity();
        ProfileEntity profileProxy = new ProfileEntity();

        when(transactionMapper.toEntity(transaction)).thenReturn(entity);
        when(profileRepository.getReferenceById(profileId)).thenReturn(profileProxy);
        when(transactionRepository.saveAll(anyList())).thenReturn(List.of(entity));
        when(transactionMapper.toDomainList(anyList())).thenReturn(transactions);

        List<Transaction> result = adapter.createAll(transactions);

        assertEquals(1, result.size());
        verify(transactionRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should find transaction by id and profile id")
    void shouldFindTransactionByIdAndProfileId() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        TransactionEntity entity = new TransactionEntity();
        Transaction domain = Transaction.builder().build();

        when(transactionRepository.findByIdAndProfileUserId(id, profileId)).thenReturn(Optional.of(entity));
        when(transactionMapper.toDomain(entity)).thenReturn(domain);

        Optional<Transaction> result = adapter.findByIdAndProfileId(id, profileId);

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Should find all transactions by profile id")
    void shouldFindAllTransactionsByProfileId() {
        UUID profileId = UUID.randomUUID();
        List<TransactionEntity> entities = List.of(new TransactionEntity());
        List<Transaction> domains = List.of(Transaction.builder().build());

        when(transactionRepository.findAllByProfileUserIdOrderByTransactionDateDesc(profileId)).thenReturn(entities);
        when(transactionMapper.toDomainList(entities)).thenReturn(domains);

        List<Transaction> result = adapter.findAllByProfileId(profileId);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should delete transaction by id")
    void shouldDeleteTransactionById() {
        UUID id = UUID.randomUUID();
        adapter.delete(id);
        verify(transactionRepository).deleteById(id);
    }

    @Test
    @DisplayName("Should check if transaction exists")
    void shouldCheckIfTransactionExists() {
        UUID id = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        when(transactionRepository.existsByIdAndProfileUserId(id, profileId)).thenReturn(true);

        assertTrue(adapter.existsByIdAndProfileId(id, profileId));
    }

    @Test
    @DisplayName("Should find transactions by purchase group uuid")
    void shouldFindByPurchaseGroupUuid() {
        UUID purchaseGroupUuid = UUID.randomUUID();
        List<TransactionEntity> entities = List.of(new TransactionEntity());
        List<Transaction> domains = List.of(Transaction.builder().build());

        when(transactionRepository.findByPurchaseGroupUuid(purchaseGroupUuid)).thenReturn(entities);
        when(transactionMapper.toDomainList(entities)).thenReturn(domains);

        List<Transaction> result = adapter.findByPurchaseGroupUuid(purchaseGroupUuid);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should find transactions by invoice id")
    void shouldFindByInvoiceId() {
        UUID invoiceId = UUID.randomUUID();
        List<TransactionEntity> entities = List.of(new TransactionEntity());
        List<Transaction> domains = List.of(Transaction.builder().build());

        when(transactionRepository.findByInvoiceId(invoiceId)).thenReturn(entities);
        when(transactionMapper.toDomainList(entities)).thenReturn(domains);

        List<Transaction> result = adapter.findByInvoiceId(invoiceId);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should find transactions by bank account id")
    void shouldFindByBankAccountId() {
        UUID bankAccountId = UUID.randomUUID();
        List<TransactionEntity> entities = List.of(new TransactionEntity());
        List<Transaction> domains = List.of(Transaction.builder().build());

        when(transactionRepository.findByBankAccountId(bankAccountId)).thenReturn(entities);
        when(transactionMapper.toDomainList(entities)).thenReturn(domains);

        List<Transaction> result = adapter.findByBankAccountId(bankAccountId);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should sum amount by bank account and type")
    void shouldSumAmountByBankAccountAndType() {
        UUID bankAccountId = UUID.randomUUID();
        BigDecimal expectedSum = BigDecimal.valueOf(500);

        when(transactionRepository.sumAmountByBankAccountIdAndType(bankAccountId, TransactionType.EXPENSE))
                .thenReturn(expectedSum);

        BigDecimal result = adapter.sumAmountByBankAccountIdAndType(bankAccountId, TransactionType.EXPENSE);

        assertEquals(expectedSum, result);
    }

    @Test
    @DisplayName("Should sum amount by invoice id")
    void shouldSumAmountByInvoiceId() {
        UUID invoiceId = UUID.randomUUID();
        BigDecimal expectedSum = BigDecimal.valueOf(1000);

        when(transactionRepository.sumAmountByInvoiceId(invoiceId)).thenReturn(expectedSum);

        BigDecimal result = adapter.sumAmountByInvoiceId(invoiceId);

        assertEquals(expectedSum, result);
    }

    @Test
    @DisplayName("Should delete by purchase group uuid")
    void shouldDeleteByPurchaseGroupUuid() {
        UUID purchaseGroupUuid = UUID.randomUUID();
        adapter.deleteByPurchaseGroupUuid(purchaseGroupUuid);
        verify(transactionRepository).deleteByPurchaseGroupUuid(purchaseGroupUuid);
    }

    @Test
    @DisplayName("Should delete by invoice id")
    void shouldDeleteByInvoiceId() {
        UUID invoiceId = UUID.randomUUID();
        adapter.deleteByInvoiceId(invoiceId);
        verify(transactionRepository).deleteByInvoiceId(invoiceId);
    }

    @Test
    @DisplayName("Should search transactions with filters")
    void shouldSearchTransactions() {
        UUID profileId = UUID.randomUUID();
        TransactionFilter filter = TransactionFilter.builder()
                .page(0)
                .size(10)
                .sortDirection("DESC")
                .sortBy("transactionDate")
                .build();

        TransactionEntity entity = new TransactionEntity();
        Page<TransactionEntity> page = new PageImpl<>(List.of(entity));
        List<Transaction> domains = List.of(Transaction.builder().build());

        when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(transactionMapper.toDomainList(anyList())).thenReturn(domains);

        PageDomain<Transaction> result = adapter.search(profileId, filter);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        verify(transactionRepository).findAll(any(Specification.class), any(Pageable.class));
    }
}