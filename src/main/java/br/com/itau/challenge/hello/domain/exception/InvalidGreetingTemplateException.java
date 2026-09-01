package br.com.itau.challenge.hello.domain.exception;

public class InvalidGreetingTemplateException extends RuntimeException {

	public InvalidGreetingTemplateException(String message) {
		super(message);
	}
}
