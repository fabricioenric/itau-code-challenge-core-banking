package br.com.itau.challenge.hello.port.output;

@FunctionalInterface
public interface GreetingTemplateProvider {

	/**
	 * Returns a randomly selected greeting template containing a single `%s` placeholder for the requester name.
	 */
	String randomTemplate();
}
