package br.com.itau.challenge.hello.adapter.output.dynamodb;

import br.com.itau.challenge.hello.domain.model.GreetingTemplate;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DynamoDbGreetingTemplateWriterTest {

	@Test
	void shouldPutTheGreetingTemplateIntoTheConfiguredTable() {
		DynamoDbClient client = mock(DynamoDbClient.class);
		given(client.putItem(any(PutItemRequest.class))).willReturn(PutItemResponse.builder().build());
		DynamoDbGreetingTemplateWriter writer = new DynamoDbGreetingTemplateWriter(client, "GreetingMessages");

		writer.save(new GreetingTemplate("k1", "Yo %s!"));

		ArgumentCaptor<PutItemRequest> requestCaptor = ArgumentCaptor.forClass(PutItemRequest.class);
		verify(client).putItem(requestCaptor.capture());
		PutItemRequest request = requestCaptor.getValue();
		assertEquals("GreetingMessages", request.tableName());
		assertEquals("k1", request.item().get("id").s());
		assertEquals("Yo %s!", request.item().get("template").s());
	}
}
