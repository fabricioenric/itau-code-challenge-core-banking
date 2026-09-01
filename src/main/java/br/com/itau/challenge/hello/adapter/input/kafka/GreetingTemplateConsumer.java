package br.com.itau.challenge.hello.adapter.input.kafka;

import br.com.itau.challenge.hello.adapter.input.kafka.dto.GreetingTemplateMessage;
import br.com.itau.challenge.hello.domain.model.GreetingTemplate;
import br.com.itau.challenge.hello.port.input.SaveGreetingTemplateUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class GreetingTemplateConsumer {

	private final SaveGreetingTemplateUseCase saveGreetingTemplateUseCase;
	private final ObjectMapper objectMapper;

	public GreetingTemplateConsumer(
			SaveGreetingTemplateUseCase saveGreetingTemplateUseCase,
			ObjectMapper objectMapper) {
		this.saveGreetingTemplateUseCase = saveGreetingTemplateUseCase;
		this.objectMapper = objectMapper;
	}

	@KafkaListener(topics = "${greeting-templates.topic-name}")
	public void consume(String payload) {
		GreetingTemplateMessage message = objectMapper.readValue(payload, GreetingTemplateMessage.class);
		saveGreetingTemplateUseCase.saveGreetingTemplate(
				new GreetingTemplate(message.id(), message.template()));
	}
}
