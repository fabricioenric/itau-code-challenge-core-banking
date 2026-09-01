package br.com.itau.challenge.hello.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class GreetingTemplateTest {

	@Test
	void shouldExposeTheIdAndTemplateItWasCreatedWith() {
		GreetingTemplate greetingTemplate = new GreetingTemplate("k1", "Yo %s!");

		assertEquals("k1", greetingTemplate.id());
		assertEquals("Yo %s!", greetingTemplate.template());
	}

	@Test
	void shouldBeEqualWhenIdAndTemplateAreEqual() {
		assertEquals(new GreetingTemplate("k1", "Yo %s!"), new GreetingTemplate("k1", "Yo %s!"));
	}

	@Test
	void shouldNotBeEqualWhenTemplateDiffers() {
		assertNotEquals(new GreetingTemplate("k1", "Yo %s!"), new GreetingTemplate("k1", "Sup %s?"));
	}
}
