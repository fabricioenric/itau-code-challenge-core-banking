package br.com.itau.challenge.hello.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvalidGreetingTemplateExceptionTest {

	@Test
	void shouldCarryTheMessageItWasCreatedWith() {
		InvalidGreetingTemplateException exception =
				new InvalidGreetingTemplateException("Greeting template must not be blank");

		assertEquals("Greeting template must not be blank", exception.getMessage());
	}
}
