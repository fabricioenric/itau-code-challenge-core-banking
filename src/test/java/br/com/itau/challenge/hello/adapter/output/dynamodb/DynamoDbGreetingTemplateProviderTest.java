package br.com.itau.challenge.hello.adapter.output.dynamodb;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DynamoDbGreetingTemplateProviderTest {

	private Map<String, AttributeValue> itemOf(String template) {
		return Map.of("template", AttributeValue.builder().s(template).build());
	}

	@Test
	void shouldScanTheConfiguredTableAndReturnOneOfItsTemplates() {
		DynamoDbClient client = mock(DynamoDbClient.class);
		given(client.scan(any(ScanRequest.class))).willReturn(
				ScanResponse.builder().items(itemOf("Hello, %s!")).build());
		DynamoDbGreetingTemplateProvider provider = new DynamoDbGreetingTemplateProvider(client, "GreetingMessages");

		String template = provider.randomTemplate();

		assertEquals("Hello, %s!", template);
	}

	@Test
	void shouldScanUsingTheConfiguredTableName() {
		DynamoDbClient client = mock(DynamoDbClient.class);
		given(client.scan(any(ScanRequest.class))).willReturn(
				ScanResponse.builder().items(itemOf("Hi there, %s!")).build());
		DynamoDbGreetingTemplateProvider provider = new DynamoDbGreetingTemplateProvider(client, "CustomTableName");

		provider.randomTemplate();

		ArgumentCaptor<ScanRequest> requestCaptor = ArgumentCaptor.forClass(ScanRequest.class);
		verify(client).scan(requestCaptor.capture());
		assertEquals("CustomTableName", requestCaptor.getValue().tableName());
	}

	@Test
	void shouldEventuallyReturnMoreThanOneDistinctTemplateWhenSeveralExist() {
		DynamoDbClient client = mock(DynamoDbClient.class);
		given(client.scan(any(ScanRequest.class))).willReturn(
				ScanResponse.builder()
						.items(itemOf("Hello, %s!"), itemOf("Hi there, %s!"), itemOf("Greetings, %s!"))
						.build());
		DynamoDbGreetingTemplateProvider provider = new DynamoDbGreetingTemplateProvider(client, "GreetingMessages");

		Set<String> observedTemplates = IntStream.rangeClosed(1, 50)
				.mapToObj(i -> provider.randomTemplate())
				.collect(Collectors.toSet());

		assertTrue(observedTemplates.size() > 1);
	}

	@Test
	void shouldFailFastWhenTheTableHasNoTemplates() {
		DynamoDbClient client = mock(DynamoDbClient.class);
		given(client.scan(any(ScanRequest.class))).willReturn(ScanResponse.builder().items(List.of()).build());
		DynamoDbGreetingTemplateProvider provider = new DynamoDbGreetingTemplateProvider(client, "GreetingMessages");

		assertThrows(IllegalStateException.class, provider::randomTemplate);
	}
}
