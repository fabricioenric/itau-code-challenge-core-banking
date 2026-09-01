package br.com.itau.challenge.hello.domain.exception;

public class BlankRequesterNameException extends RuntimeException {

	public BlankRequesterNameException() {
		super("Requester name must not be blank");
	}
}
