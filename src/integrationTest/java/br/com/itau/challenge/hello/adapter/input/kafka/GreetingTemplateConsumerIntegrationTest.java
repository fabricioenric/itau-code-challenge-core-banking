package br.com.itau.challenge.hello.adapter.input.kafka;

import br.com.itau.challenge.hello.domain.model.GreetingTemplate;
import br.com.itau.challenge.hello.port.output.GreetingTemplateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Exercises the real Kafka wiring end to end: boots the actual Spring application
 * context — including the real {@link GreetingTemplateConsumer} {@code @KafkaListener} — connected
 * to the real broker running in Docker (see `make redpanda-up`). A message is published
 * onto the real topic and picked up by the running listener container on its own, proving
 * the actual docker-compose `redpanda` service, network config, and Spring Kafka wiring
 * all work together. Only the DynamoDB-backed persistence port is faked, since that
 * adapter is covered separately. Not part of `./gradlew test`/`check`; run explicitly
 * with `./gradlew integrationTest` or `make integration-test`.
 */
@SpringBootTest
class GreetingTemplateConsumerIntegrationTest {

	private static final String TOPIC = "greeting-templates";

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@MockitoBean
	private GreetingTemplateRepository greetingTemplateRepository;

	@Test
	void shouldConsumeAMessagePublishedToTheRealTopicViaTheRealApplicationListener() {
		String testId = "it-" + UUID.randomUUID();

		kafkaTemplate.send(TOPIC, "{\"id\": \"" + testId + "\", \"template\": \"Yo %s! From the real broker!\"}");

		verify(greetingTemplateRepository, timeout(10_000))
				.save(new GreetingTemplate(testId, "Yo %s! From the real broker!"));
	}
}
