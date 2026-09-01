package br.com.itau.challenge.hello.adapter.output.dynamodb;

import br.com.itau.challenge.hello.port.output.GreetingTemplateProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DynamoDbGreetingTemplateProvider implements GreetingTemplateProvider {

	private static final String TEMPLATE_ATTRIBUTE = "template";

	private final DynamoDbClient dynamoDbClient;
	private final String tableName;

	public DynamoDbGreetingTemplateProvider(
			DynamoDbClient dynamoDbClient,
			@Value("${dynamodb.table-name}") String tableName) {
		this.dynamoDbClient = dynamoDbClient;
		this.tableName = tableName;
	}

	@Override
	public String randomTemplate() {
		ScanRequest request = ScanRequest
				.builder()
				.tableName(tableName)
				.projectionExpression(TEMPLATE_ATTRIBUTE)
				.build();

		List<String> templates = dynamoDbClient
				.scan(request)
				.items()
				.stream()
				.map(item -> item.get(TEMPLATE_ATTRIBUTE).s())
				.toList();

		if (templates.isEmpty()) {
			throw new IllegalStateException("No greeting templates found in table '" + tableName + "'");
		}

		return templates.get(ThreadLocalRandom.current().nextInt(templates.size()));
	}
}
