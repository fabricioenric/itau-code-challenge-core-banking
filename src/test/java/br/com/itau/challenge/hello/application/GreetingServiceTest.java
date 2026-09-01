package br.com.itau.challenge.hello.application;

import br.com.itau.challenge.hello.domain.exception.BlankRequesterNameException;
import br.com.itau.challenge.hello.domain.model.Greeting;
import br.com.itau.challenge.hello.port.output.GreetingTemplateProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GreetingServiceTest {

	@Test
	void shouldApplyRequesterNameToTheSelectedTemplate() {
		GreetingTemplateProvider templateProvider = () -> "Hello, %s!";
		GreetingService service = new GreetingService(templateProvider);

		Greeting greeting = service.getGreeting("Ada");

		assertEquals("Hello, Ada!", greeting.message());
	}

	@Test
	void shouldTrimSurroundingWhitespaceFromTheRequesterName() {
		GreetingTemplateProvider templateProvider = () -> "Hello, %s!";
		GreetingService service = new GreetingService(templateProvider);

		Greeting greeting = service.getGreeting("  Ada  ");

		assertEquals("Hello, Ada!", greeting.message());
	}

	@Test
	void shouldPreserveInternalWhitespaceOfMultiWordNames() {
		GreetingTemplateProvider templateProvider = () -> "Welcome, %s!";
		GreetingService service = new GreetingService(templateProvider);

		Greeting greeting = service.getGreeting("Ada Lovelace");

		assertEquals("Welcome, Ada Lovelace!", greeting.message());
	}

	@Test
	void shouldAcceptNamesContainingPunctuation() {
		GreetingTemplateProvider templateProvider = () -> "Hello, %s!";
		GreetingService service = new GreetingService(templateProvider);

		Greeting greeting = service.getGreeting("O'Connor");

		assertEquals("Hello, O'Connor!", greeting.message());
	}

	@Test
	void shouldFormatUsingWhicheverTemplateTheProviderReturns() {
		GreetingTemplateProvider templateProvider = () -> "Hey %s, great to see you!";
		GreetingService service = new GreetingService(templateProvider);

		Greeting greeting = service.getGreeting("Grace");

		assertEquals("Hey Grace, great to see you!", greeting.message());
	}

	@Test
	void shouldRejectABlankRequesterName() {
		GreetingTemplateProvider templateProvider = () -> "Hello, %s!";
		GreetingService service = new GreetingService(templateProvider);

		assertThrows(BlankRequesterNameException.class, () -> service.getGreeting("   "));
	}

	@Test
	void shouldRejectAnEmptyRequesterName() {
		GreetingTemplateProvider templateProvider = () -> "Hello, %s!";
		GreetingService service = new GreetingService(templateProvider);

		assertThrows(BlankRequesterNameException.class, () -> service.getGreeting(""));
	}
}
