package br.com.itau.challenge.corebanking.adapter.output.dynamodb;

import br.com.itau.challenge.corebanking.domain.model.AccountBalance;
import br.com.itau.challenge.corebanking.domain.model.Balance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DynamoDbBalanceRepositoryTest {

    private DynamoDbClient dynamoDbClient;
    private DynamoDbBalanceRepository repository;

    private static final String TABLE_NAME = "AccountBalances";
    private static final UUID ACCOUNT_ID = UUID.fromString("5b19c8b6-0cc4-4c72-a989-0c2ee15fa975");
    private static final UUID OWNER_ID = UUID.fromString("315e3cfe-f4af-4cd2-b298-a449e614349a");
    private static final UUID TRANSACTION_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        dynamoDbClient = mock(DynamoDbClient.class);
        repository = new DynamoDbBalanceRepository(dynamoDbClient, TABLE_NAME);
    }

    private AccountBalance validAccountBalance() {
        return new AccountBalance(
                ACCOUNT_ID, OWNER_ID,
                new Balance(new BigDecimal("183.12"), "BRL"),
                1751641364589998L,
                TRANSACTION_ID
        );
    }

    @Test
    void deveSalvarERetornarTrueQuandoEscritaForBemSucedida() {
        when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                .thenReturn(PutItemResponse.builder().build());

        boolean result = repository.saveIfNewer(validAccountBalance());

        assertTrue(result);

        ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(dynamoDbClient).putItem(captor.capture());

        PutItemRequest request = captor.getValue();
        assertEquals(TABLE_NAME, request.tableName());
        assertEquals("attribute_not_exists(updated_at) OR updated_at < :newTs", request.conditionExpression());
        assertEquals(ACCOUNT_ID.toString(), request.item().get("account_id").s());
        assertEquals(OWNER_ID.toString(), request.item().get("owner").s());
        assertEquals("183.12", request.item().get("balance_amount").n());
        assertEquals("BRL", request.item().get("balance_currency").s());
        assertEquals("1751641364589998", request.item().get("updated_at").n());
        assertEquals(TRANSACTION_ID.toString(), request.item().get("last_transaction_id").s());
        assertEquals("1751641364589998", request.expressionAttributeValues().get(":newTs").n());
    }

    @Test
    void deveRetornarFalseQuandoEventoForAntigoOuDuplicado() {
        when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                .thenThrow(ConditionalCheckFailedException.builder().message("condição falhou").build());

        boolean result = repository.saveIfNewer(validAccountBalance());

        assertFalse(result);
        verify(dynamoDbClient).putItem(any(PutItemRequest.class));
    }

    @Test
    void devePropagarExcecaoQuandoDynamoDbLancarErroDeInfraestrutura() {
        when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                .thenThrow(software.amazon.awssdk.services.dynamodb.model.DynamoDbException.builder()
                        .message("serviço indisponível").build());

        assertThrows(DynamoDbException.class, () -> repository.saveIfNewer(validAccountBalance()));
    }

    @Test
    void deveGravarOwnerVazioQuandoOwnerForNulo() {
        AccountBalance semOwner = new AccountBalance(
                ACCOUNT_ID, null,
                new Balance(new BigDecimal("50.00"), "BRL"),
                123L,
                TRANSACTION_ID
        );
        when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                .thenReturn(PutItemResponse.builder().build());

        repository.saveIfNewer(semOwner);

        ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(dynamoDbClient).putItem(captor.capture());
        assertEquals("", captor.getValue().item().get("owner").s());
    }

    @Test
    void deveRetornarVazioQuandoContaNaoExistir() {
        when(dynamoDbClient.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder().build());

        Optional<AccountBalance> result = repository.findByAccountId(ACCOUNT_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void deveRetornarSaldoQuandoContaExistir() {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("account_id", AttributeValue.builder().s(ACCOUNT_ID.toString()).build());
        item.put("owner", AttributeValue.builder().s(OWNER_ID.toString()).build());
        item.put("balance_amount", AttributeValue.builder().n("183.12").build());
        item.put("balance_currency", AttributeValue.builder().s("BRL").build());
        item.put("updated_at", AttributeValue.builder().n("1751641364589998").build());
        item.put("last_transaction_id", AttributeValue.builder().s(TRANSACTION_ID.toString()).build());

        when(dynamoDbClient.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder().item(item).build());

        Optional<AccountBalance> result = repository.findByAccountId(ACCOUNT_ID);

        assertTrue(result.isPresent());
        AccountBalance found = result.get();
        assertEquals(ACCOUNT_ID, found.getAccountId());
        assertEquals(OWNER_ID, found.getOwner());
        assertEquals(new BigDecimal("183.12"), found.getBalance().getAmount());
        assertEquals("BRL", found.getBalance().getCurrency());
        assertEquals(1751641364589998L, found.getUpdatedAtMicros());
        assertEquals(TRANSACTION_ID, found.getLastTransactionId());
    }

    @Test
    void deveRetornarOwnerNuloQuandoOwnerForVazio() {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("account_id", AttributeValue.builder().s(ACCOUNT_ID.toString()).build());
        item.put("owner", AttributeValue.builder().s("").build());
        item.put("balance_amount", AttributeValue.builder().n("50.00").build());
        item.put("balance_currency", AttributeValue.builder().s("BRL").build());
        item.put("updated_at", AttributeValue.builder().n("123").build());
        item.put("last_transaction_id", AttributeValue.builder().s(TRANSACTION_ID.toString()).build());

        when(dynamoDbClient.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder().item(item).build());

        Optional<AccountBalance> result = repository.findByAccountId(ACCOUNT_ID);

        assertNull(result.get().getOwner());
    }

    @Test
    void deveRetornarLastTransactionIdNuloQuandoCampoAusente() {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("account_id", AttributeValue.builder().s(ACCOUNT_ID.toString()).build());
        item.put("owner", AttributeValue.builder().s(OWNER_ID.toString()).build());
        item.put("balance_amount", AttributeValue.builder().n("50.00").build());
        item.put("balance_currency", AttributeValue.builder().s("BRL").build());
        item.put("updated_at", AttributeValue.builder().n("123").build());

        when(dynamoDbClient.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder().item(item).build());

        Optional<AccountBalance> result = repository.findByAccountId(ACCOUNT_ID);

        assertNull(result.get().getLastTransactionId());
    }

    @Test
    void deveUsarConsistentReadNaLeitura() {
        when(dynamoDbClient.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder().build());

        repository.findByAccountId(ACCOUNT_ID);

        ArgumentCaptor<GetItemRequest> captor = ArgumentCaptor.forClass(GetItemRequest.class);
        verify(dynamoDbClient).getItem(captor.capture());
        assertTrue(captor.getValue().consistentRead());
        assertEquals(TABLE_NAME, captor.getValue().tableName());
        assertEquals(ACCOUNT_ID.toString(), captor.getValue().key().get("account_id").s());
    }
}