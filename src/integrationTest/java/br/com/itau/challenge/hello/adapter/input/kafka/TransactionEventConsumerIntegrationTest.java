package br.com.itau.challenge.hello.adapter.input.kafka;

import br.com.itau.challenge.hello.adapter.output.dynamodb.DynamoDbBalanceRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TransactionEventConsumerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private DynamoDbBalanceRepository repository;

    @Autowired
    private DynamoDbClient dynamoDbClient;

    private UUID accountIdParaLimpeza;

    @AfterEach
    void cleanup() {
        if (accountIdParaLimpeza != null) {
            dynamoDbClient.deleteItem(DeleteItemRequest.builder()
                    .tableName("AccountBalances")
                    .key(Map.of("account_id", AttributeValue.builder().s(accountIdParaLimpeza.toString()).build()))
                    .build());
        }
    }

    @Test
    void deveConsumirEventoValidoEPersistirSaldoNoDynamoDb() {
        UUID accountId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        accountIdParaLimpeza = accountId;

        String payload = """
                {
                  "transaction": {
                    "id": "%s",
                    "type": "CREDIT",
                    "amount": 97.07,
                    "currency": "BRL",
                    "status": "APPROVED",
                    "timestamp": %d
                  },
                  "account": {
                    "id": "%s",
                    "owner": "%s",
                    "created_at": 1634874339000000,
                    "status": "ENABLED",
                    "balance": {
                      "amount": 183.12,
                      "currency": "BRL"
                    }
                  }
                }
                """.formatted(transactionId, System.currentTimeMillis() * 1000, accountId, UUID.randomUUID());

        kafkaTemplate.send("transacoes-financeiras-processadas", payload);

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    var found = repository.findByAccountId(accountId);
                    assertTrue(found.isPresent());
                    assertEquals(new java.math.BigDecimal("183.12"), found.get().getBalance().getAmount());
                });
    }

    @Test
    void naoDeveProcessarSaldoQuandoEventoForEstruturalmenteInvalido() {
        UUID accountId = UUID.randomUUID();

        String payloadInvalido = """
                {
                  "transaction": {
                    "id": "nao-e-um-uuid",
                    "type": "CREDIT",
                    "amount": 10.00,
                    "currency": "BRL",
                    "status": "APPROVED",
                    "timestamp": 123
                  },
                  "account": {
                    "id": "%s",
                    "owner": "%s",
                    "created_at": 0,
                    "status": "ENABLED",
                    "balance": { "amount": 10.00, "currency": "BRL" }
                  }
                }
                """.formatted(accountId, UUID.randomUUID());

        kafkaTemplate.send("transacoes-financeiras-processadas", payloadInvalido);

        Awaitility.await()
                .during(Duration.ofSeconds(3))
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    var found = repository.findByAccountId(accountId);
                    assertTrue(found.isEmpty());
                });
    }
}