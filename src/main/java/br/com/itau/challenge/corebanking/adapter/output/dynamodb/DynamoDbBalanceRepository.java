package br.com.itau.challenge.corebanking.adapter.output.dynamodb;

import br.com.itau.challenge.corebanking.domain.model.AccountBalance;
import br.com.itau.challenge.corebanking.domain.model.Balance;
import br.com.itau.challenge.corebanking.port.output.BalanceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class DynamoDbBalanceRepository implements BalanceRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDbBalanceRepository(
            DynamoDbClient dynamoDbClient,
            @Value("${BALANCES_TABLE_NAME:AccountBalances}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public boolean saveIfNewer(AccountBalance accountBalance) {
        Map<String, AttributeValue> item = toItem(accountBalance);

        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":newTs", AttributeValue.builder()
                .n(String.valueOf(accountBalance.getUpdatedAtMicros()))
                .build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .conditionExpression("attribute_not_exists(updated_at) OR updated_at < :newTs")
                .expressionAttributeValues(expressionValues)
                .build();

        try {
            dynamoDbClient.putItem(request);
            return true;
        } catch (ConditionalCheckFailedException ex) {
            // Evento antigo ou duplicado — comportamento esperado, não é erro
            return false;
        }
    }

    @Override
    public Optional<AccountBalance> findByAccountId(UUID accountId) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("account_id", AttributeValue.builder().s(accountId.toString()).build()))
                .consistentRead(true)
                .build();

        GetItemResponse response = dynamoDbClient.getItem(request);

        if (!response.hasItem() || response.item().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(toDomain(response.item()));
    }

    private Map<String, AttributeValue> toItem(AccountBalance accountBalance) {
        Map<String, AttributeValue> item = new HashMap<>();

        item.put("account_id", AttributeValue.builder()
                .s(accountBalance.getAccountId().toString())
                .build());

        item.put("owner", AttributeValue.builder()
                .s(accountBalance.getOwner() != null ? accountBalance.getOwner().toString() : "")
                .build());

        item.put("balance_amount", AttributeValue.builder()
                .n(accountBalance.getBalance().getAmount().toPlainString())
                .build());

        item.put("balance_currency", AttributeValue.builder()
                .s(accountBalance.getBalance().getCurrency())
                .build());

        item.put("updated_at", AttributeValue.builder()
                .n(String.valueOf(accountBalance.getUpdatedAtMicros()))
                .build());

        item.put("last_transaction_id", AttributeValue.builder()
                .s(accountBalance.getLastTransactionId().toString())
                .build());

        return item;
    }

    private AccountBalance toDomain(Map<String, AttributeValue> item) {
        Balance balance = new Balance(
                new BigDecimal(item.get("balance_amount").n()),
                item.get("balance_currency").s()
        );

        UUID owner = item.containsKey("owner") && !item.get("owner").s().isBlank()
                ? UUID.fromString(item.get("owner").s())
                : null;

        UUID lastTransactionId = item.containsKey("last_transaction_id")
                ? UUID.fromString(item.get("last_transaction_id").s())
                : null;

        return new AccountBalance(
                UUID.fromString(item.get("account_id").s()),
                owner,
                balance,
                Long.parseLong(item.get("updated_at").n()),
                lastTransactionId
        );
    }
}