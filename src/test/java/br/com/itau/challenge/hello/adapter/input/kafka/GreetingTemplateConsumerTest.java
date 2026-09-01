package br.com.itau.challenge.hello.adapter.input.kafka;

import br.com.itau.challenge.hello.domain.model.GreetingTemplate;
import br.com.itau.challenge.hello.port.input.SaveGreetingTemplateUseCase;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreetingTemplateConsumerTest {

	private final JsonMapper objectMapper = JsonMapper.builder().build();

	@Test
	void shouldDeserializeTheJsonPayloadAndDelegateItToTheSaveUseCase() {
		List<GreetingTemplate> savedTemplates = new ArrayList<>();
		SaveGreetingTemplateUseCase useCase = savedTemplates::add;
		GreetingTemplateConsumer consumer = new GreetingTemplateConsumer(useCase, objectMapper);

		consumer.consume("{\"id\": \"k1\", \"template\": \"Yo %s!\"}");

		assertEquals(List.of(new GreetingTemplate("k1", "Yo %s!")), savedTemplates);
	}
}
