package br.com.itau.challenge.hello.application;

import br.com.itau.challenge.hello.domain.exception.AccountBalanceNotFoundException;
import br.com.itau.challenge.hello.domain.model.AccountBalance;
import br.com.itau.challenge.hello.domain.model.Balance;
import br.com.itau.challenge.hello.port.output.BalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetBalanceServiceTest {

    @Mock
    private BalanceRepository repository;

    private GetBalanceService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new GetBalanceService(repository);
    }

    @Test
    void deveRetornarSaldoQuandoContaExistir() {
        UUID accountId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        AccountBalance accountBalance = new AccountBalance(
                accountId, ownerId,
                new Balance(new BigDecimal("183.12"), "BRL"),
                1751641364589998L,
                UUID.randomUUID()
        );

        when(repository.findByAccountId(accountId)).thenReturn(Optional.of(accountBalance));

        AccountBalance result = service.execute(accountId);

        assertEquals(accountId, result.getAccountId());
        assertEquals(ownerId, result.getOwner());
        assertEquals(new BigDecimal("183.12"), result.getBalance().getAmount());
        verify(repository).findByAccountId(accountId);
    }

    @Test
    void deveLancarExcecaoQuandoContaNaoExistir() {
        UUID accountId = UUID.randomUUID();
        when(repository.findByAccountId(accountId)).thenReturn(Optional.empty());

        AccountBalanceNotFoundException ex = assertThrows(
                AccountBalanceNotFoundException.class,
                () -> service.execute(accountId)
        );

        assertEquals(accountId, ex.getAccountId());
    }

    @Test
    void devePropagarExcecaoQuandoRepositorioFalhar() {
        UUID accountId = UUID.randomUUID();
        when(repository.findByAccountId(accountId))
                .thenThrow(new RuntimeException("DynamoDB indisponível"));

        assertThrows(RuntimeException.class, () -> service.execute(accountId));
    }
}