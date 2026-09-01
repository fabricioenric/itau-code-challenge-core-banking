package br.com.itau.challenge.hello.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class GreetingTest {

	@Test
	void shouldExposeTheMessageItWasCreatedWith() {
		Greeting greeting = new Greeting("Hello, Ada!");

		assertEquals("Hello, Ada!", greeting.message());
	}

	@Test
	void shouldBeEqualWhenMessagesAreEqual() {
		assertEquals(new Greeting("Hello, Ada!"), new Greeting("Hello, Ada!"));
	}

	@Test
	void shouldNotBeEqualWhenMessagesDiffer() {
		assertNotEquals(new Greeting("Hello, Ada!"), new Greeting("Hi, Ada!"));
	}
}
