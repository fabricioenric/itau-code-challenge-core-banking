package br.com.itau.challenge.hello.adapter.output.dynamodb;

import br.com.itau.challenge.hello.domain.model.AccountBalance;
import br.com.itau.challenge.hello.domain.model.Balance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DynamoDbBalanceRepositoryIntegrationTest {

    @Autowired
    private DynamoDbBalanceRepository repository;

    @Autowired
    private DynamoDbClient dynamoDbClient;

    private UUID accountId;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
    }

    private void cleanup(UUID accountId) {
        dynamoDbClient.deleteItem(DeleteItemRequest.builder()
                .tableName("AccountBalances")
                .key(Map.of("account_id", AttributeValue.builder().s(accountId.toString()).build()))
                .build());
    }

    @Test
    void devePersistirELerSaldoRealNoDynamoDb() {
        AccountBalance accountBalance = new AccountBalance(
                accountId, UUID.randomUUID(),
                new Balance(new BigDecimal("183.12"), "BRL"),
                1751641364589998L,
                UUID.randomUUID()
        );

        boolean applied = repository.saveIfNewer(accountBalance);
        assertTrue(applied);

        Optional<AccountBalance> found = repository.findByAccountId(accountId);

        assertTrue(found.isPresent());
        assertEquals(accountId, found.get().getAccountId());
        assertEquals(new BigDecimal("183.12"), found.get().getBalance().getAmount());
        assertEquals("BRL", found.get().getBalance().getCurrency());

        cleanup(accountId);
    }

    @Test
    void naoDeveSobrescreverSaldoComEventoMaisAntigo() {
        AccountBalance recente = new AccountBalance(
                accountId, UUID.randomUUID(),
                new Balance(new BigDecimal("500.00"), "BRL"),
                2_000_000L,
                UUID.randomUUID()
        );
        AccountBalance antigo = new AccountBalance(
                accountId, UUID.randomUUID(),
                new Balance(new BigDecimal("100.00"), "BRL"),
                1_000_000L,
                UUID.randomUUID()
        );

        assertTrue(repository.saveIfNewer(recente));
        assertFalse(repository.saveIfNewer(antigo));

        Optional<AccountBalance> found = repository.findByAccountId(accountId);
        assertEquals(new BigDecimal("500.00"), found.get().getBalance().getAmount());

        cleanup(accountId);
    }

    @Test
    void deveAtualizarSaldoComEventoMaisNovo() {
        AccountBalance inicial = new AccountBalance(
                accountId, UUID.randomUUID(),
                new Balance(new BigDecimal("100.00"), "BRL"),
                1_000_000L,
                UUID.randomUUID()
        );
        AccountBalance atualizado = new AccountBalance(
                accountId, UUID.randomUUID(),
                new Balance(new BigDecimal("250.00"), "BRL"),
                2_000_000L,
                UUID.randomUUID()
        );

        assertTrue(repository.saveIfNewer(inicial));
        assertTrue(repository.saveIfNewer(atualizado));

        Optional<AccountBalance> found = repository.findByAccountId(accountId);
        assertEquals(new BigDecimal("250.00"), found.get().getBalance().getAmount());

        cleanup(accountId);
    }

    @Test
    void deveRetornarVazioParaContaInexistente() {
        Optional<AccountBalance> found = repository.findByAccountId(UUID.randomUUID());
        assertTrue(found.isEmpty());
    }
}