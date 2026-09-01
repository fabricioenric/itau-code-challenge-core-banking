package br.com.itau.challenge.hello.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlankRequesterNameExceptionTest {

	@Test
	void shouldCarryADescriptiveMessage() {
		BlankRequesterNameException exception = new BlankRequesterNameException();

		assertEquals("Requester name must not be blank", exception.getMessage());
	}
}
