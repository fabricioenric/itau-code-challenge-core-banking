package br.com.itau.challenge.hello.application;

import br.com.itau.challenge.hello.domain.exception.InvalidGreetingTemplateException;
import br.com.itau.challenge.hello.domain.model.GreetingTemplate;
import br.com.itau.challenge.hello.port.output.GreetingTemplateRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SaveGreetingTemplateServiceTest {

	@Test
	void shouldSaveAValidGreetingTemplate() {
		List<GreetingTemplate> savedTemplates = new ArrayList<>();
		GreetingTemplateRepository repository = savedTemplates::add;
		SaveGreetingTemplateService service = new SaveGreetingTemplateService(repository);

		service.saveGreetingTemplate(new GreetingTemplate("k1", "Yo %s!"));

		assertEquals(List.of(new GreetingTemplate("k1", "Yo %s!")), savedTemplates);
	}

	@Test
	void shouldRejectABlankId() {
		GreetingTemplateRepository repository = greetingTemplate -> { };
		SaveGreetingTemplateService service = new SaveGreetingTemplateService(repository);

		assertThrows(
				InvalidGreetingTemplateException.class,
				() -> service.saveGreetingTemplate(new GreetingTemplate("  ", "Yo %s!")));
	}

	@Test
	void shouldRejectABlankTemplate() {
		GreetingTemplateRepository repository = greetingTemplate -> { };
		SaveGreetingTemplateService service = new SaveGreetingTemplateService(repository);

		assertThrows(
				InvalidGreetingTemplateException.class,
				() -> service.saveGreetingTemplate(new GreetingTemplate("k1", "   ")));
	}
}
