package br.com.itau.challenge.hello.adapter.output.dynamodb;

import br.com.itau.challenge.hello.domain.model.GreetingTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the DynamoDB adapters against a real, running DynamoDB Local instance
 * (see `make db-up`). Not part of `./gradlew test`/`check` since it needs live
 * infrastructure; run explicitly with `./gradlew integrationTest` or `make integration-test`.
 */
class DynamoDbGreetingTemplateIntegrationTest {

	private final String tableName = envOrDefault("GREETING_TABLE_NAME", "GreetingMessages");

	private final DynamoDbClient dynamoDbClient = DynamoDbClient
			.builder()
			.endpointOverride(URI.create(envOrDefault("DYNAMODB_ENDPOINT", "http://localhost:8000")))
			.region(Region.of(envOrDefault("DYNAMODB_REGION", "us-east-1")))
			.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("local", "local")))
			.build();

	private final DynamoDbGreetingTemplateWriter writer = new DynamoDbGreetingTemplateWriter(dynamoDbClient, tableName);
	private final DynamoDbGreetingTemplateProvider provider = new DynamoDbGreetingTemplateProvider(dynamoDbClient, tableName);

	private String testId;

	private static String envOrDefault(String name, String defaultValue) {
		String value = System.getenv(name);
		return value != null ? value : defaultValue;
	}

	@BeforeEach
	void setUp() {
		testId = "integration-test-" + UUID.randomUUID();
	}

	@AfterEach
	void tearDown() {
		dynamoDbClient.deleteItem(
				DeleteItemRequest
						.builder()
						.tableName(tableName)
						.key(Map.of("id", AttributeValue.builder().s(testId).build()))
						.build());
	}

	@Test
	void shouldPersistAGreetingTemplateThatCanBeReadBackFromTheRealTable() {
		GreetingTemplate template = new GreetingTemplate(testId, "Yo %s! From the integration test!");

		writer.save(template);

		GetItemResponse response = dynamoDbClient.getItem(
				GetItemRequest
						.builder()
						.tableName(tableName)
						.key(Map.of("id", AttributeValue.builder().s(testId).build()))
						.build());

		assertTrue(response.hasItem());
		assertEquals(testId, response.item().get("id").s());
		assertEquals("Yo %s! From the integration test!", response.item().get("template").s());
	}

	@Test
	void shouldReadATemplateBackFromTheRealTableViaALiveScan() {
		writer.save(new GreetingTemplate(testId, "Yo %s! From the integration test!"));

		String template = provider.randomTemplate();

		assertTrue(!template.isBlank());
		assertTrue(template.contains("%s"));
	}
}
