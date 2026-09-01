package br.com.itau.challenge.hello.adapter.output.dynamodb;

import br.com.itau.challenge.hello.domain.model.GreetingTemplate;
import br.com.itau.challenge.hello.port.output.GreetingTemplateRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;

@Component
public class DynamoDbGreetingTemplateWriter implements GreetingTemplateRepository {

	private static final String ID_ATTRIBUTE = "id";
	private static final String TEMPLATE_ATTRIBUTE = "template";

	private final DynamoDbClient dynamoDbClient;
	private final String tableName;

	public DynamoDbGreetingTemplateWriter(
			DynamoDbClient dynamoDbClient,
			@Value("${dynamodb.table-name}") String tableName) {
		this.dynamoDbClient = dynamoDbClient;
		this.tableName = tableName;
	}

	@Override
	public void save(GreetingTemplate greetingTemplate) {
		PutItemRequest request = PutItemRequest
				.builder()
				.tableName(tableName)
				.item(Map.of(
						ID_ATTRIBUTE, AttributeValue.builder().s(greetingTemplate.id()).build(),
						TEMPLATE_ATTRIBUTE, AttributeValue.builder().s(greetingTemplate.template()).build()))
				.build();

		dynamoDbClient.putItem(request);
	}
}
