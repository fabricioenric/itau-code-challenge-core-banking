package br.com.itau.challenge.corebanking.application;

import br.com.itau.challenge.corebanking.domain.exception.InvalidTransactionEventException;
import br.com.itau.challenge.corebanking.domain.model.AccountBalance;
import br.com.itau.challenge.corebanking.domain.model.Balance;
import br.com.itau.challenge.corebanking.domain.model.TransactionEvent;
import br.com.itau.challenge.corebanking.domain.model.TransactionStatus;
import br.com.itau.challenge.corebanking.domain.model.TransactionType;
import br.com.itau.challenge.corebanking.port.output.BalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessTransactionEventServiceTest {

    @Mock
    private BalanceRepository repository;

    private ProcessTransactionEventService service;

    private static final UUID TRANSACTION_ID = UUID.randomUUID();
    private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final UUID OWNER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ProcessTransactionEventService(repository);
    }

    private TransactionEvent validEvent() {
        return new TransactionEvent(
                TRANSACTION_ID,
                TransactionType.CREDIT,
                TransactionStatus.APPROVED,
                1751641364589998L,
                ACCOUNT_ID,
                OWNER_ID,
                new Balance(new BigDecimal("183.12"), "BRL")
        );
    }

    @Test
    void deveSalvarSaldoQuandoEventoForValidoENovo() {
        when(repository.saveIfNewer(any(AccountBalance.class))).thenReturn(true);

        service.process(validEvent());

        ArgumentCaptor<AccountBalance> captor = ArgumentCaptor.forClass(AccountBalance.class);
        verify(repository).saveIfNewer(captor.capture());

        AccountBalance saved = captor.getValue();
        assertEquals(ACCOUNT_ID, saved.getAccountId());
        assertEquals(OWNER_ID, saved.getOwner());
        assertEquals(new BigDecimal("183.12"), saved.getBalance().getAmount());
        assertEquals("BRL", saved.getBalance().getCurrency());
        assertEquals(1751641364589998L, saved.getUpdatedAtMicros());
        assertEquals(TRANSACTION_ID, saved.getLastTransactionId());
    }

    @Test
    void deveProcessarTransacaoDeclinedNormalmente() {
        TransactionEvent event = new TransactionEvent(
                TRANSACTION_ID, TransactionType.DEBIT, TransactionStatus.DECLINED,
                123L, ACCOUNT_ID, OWNER_ID, new Balance(new BigDecimal("50.00"), "BRL")
        );
        when(repository.saveIfNewer(any(AccountBalance.class))).thenReturn(true);

        service.process(event);

        verify(repository).saveIfNewer(any(AccountBalance.class));
    }

    @Test
    void naoDeveLancarExcecaoQuandoEventoForAntigoOuDuplicado() {
        when(repository.saveIfNewer(any(AccountBalance.class))).thenReturn(false);

        service.process(validEvent());

        verify(repository).saveIfNewer(any(AccountBalance.class));
    }

    @Test
    void deveLancarExcecaoQuandoEventoForNulo() {
        assertThrows(InvalidTransactionEventException.class, () -> service.process(null));
        verify(repository, never()).saveIfNewer(any());
    }

    @Test
    void deveLancarExcecaoQuandoTransactionIdForNulo() {
        TransactionEvent event = new TransactionEvent(
                null, TransactionType.CREDIT, TransactionStatus.APPROVED,
                123L, ACCOUNT_ID, OWNER_ID, new Balance(new BigDecimal("10.00"), "BRL")
        );

        assertThrows(InvalidTransactionEventException.class, () -> service.process(event));
        verify(repository, never()).saveIfNewer(any());
    }

    @Test
    void deveLancarExcecaoQuandoAccountIdForNulo() {
        TransactionEvent event = new TransactionEvent(
                TRANSACTION_ID, TransactionType.CREDIT, TransactionStatus.APPROVED,
                123L, null, OWNER_ID, new Balance(new BigDecimal("10.00"), "BRL")
        );

        assertThrows(InvalidTransactionEventException.class, () -> service.process(event));
        verify(repository, never()).saveIfNewer(any());
    }

    @Test
    void deveLancarExcecaoQuandoTypeForNulo() {
        TransactionEvent event = new TransactionEvent(
                TRANSACTION_ID, null, TransactionStatus.APPROVED,
                123L, ACCOUNT_ID, OWNER_ID, new Balance(new BigDecimal("10.00"), "BRL")
        );

        assertThrows(InvalidTransactionEventException.class, () -> service.process(event));
        verify(repository, never()).saveIfNewer(any());
    }

    @Test
    void deveLancarExcecaoQuandoStatusForNulo() {
        TransactionEvent event = new TransactionEvent(
                TRANSACTION_ID, TransactionType.CREDIT, null,
                123L, ACCOUNT_ID, OWNER_ID, new Balance(new BigDecimal("10.00"), "BRL")
        );

        assertThrows(InvalidTransactionEventException.class, () -> service.process(event));
        verify(repository, never()).saveIfNewer(any());
    }

    @Test
    void deveLancarExcecaoQuandoTimestampForZeroOuNegativo() {
        TransactionEvent event = new TransactionEvent(
                TRANSACTION_ID, TransactionType.CREDIT, TransactionStatus.APPROVED,
                0L, ACCOUNT_ID, OWNER_ID, new Balance(new BigDecimal("10.00"), "BRL")
        );

        assertThrows(InvalidTransactionEventException.class, () -> service.process(event));
        verify(repository, never()).saveIfNewer(any());
    }

    @Test
    void deveLancarExcecaoQuandoBalanceForNulo() {
        TransactionEvent event = new TransactionEvent(
                TRANSACTION_ID, TransactionType.CREDIT, TransactionStatus.APPROVED,
                123L, ACCOUNT_ID, OWNER_ID, null
        );

        assertThrows(InvalidTransactionEventException.class, () -> service.process(event));
        verify(repository, never()).saveIfNewer(any());
    }

    @Test
    void deveLancarExcecaoQuandoAmountForNegativo() {
        TransactionEvent event = new TransactionEvent(
                TRANSACTION_ID, TransactionType.CREDIT, TransactionStatus.APPROVED,
                123L, ACCOUNT_ID, OWNER_ID, new Balance(new BigDecimal("-1.00"), "BRL")
        );

        assertThrows(InvalidTransactionEventException.class, () -> service.process(event));
        verify(repository, never()).saveIfNewer(any());
    }

    @Test
    void deveLancarExcecaoQuandoCurrencyForVazia() {
        assertThrows(IllegalArgumentException.class,
                () -> new Balance(new BigDecimal("10.00"), ""));
    }

    @Test
    void deveAceitarAmountZero() {
        TransactionEvent event = new TransactionEvent(
                TRANSACTION_ID, TransactionType.CREDIT, TransactionStatus.APPROVED,
                123L, ACCOUNT_ID, OWNER_ID, new Balance(BigDecimal.ZERO, "BRL")
        );
        when(repository.saveIfNewer(any(AccountBalance.class))).thenReturn(true);

        service.process(event);

        verify(repository).saveIfNewer(any(AccountBalance.class));
    }

    @Test
    void devePropagarExcecaoQuandoRepositorioFalhar() {
        when(repository.saveIfNewer(any(AccountBalance.class)))
                .thenThrow(new RuntimeException("DynamoDB indisponível"));

        assertThrows(RuntimeException.class, () -> service.process(validEvent()));
    }
}