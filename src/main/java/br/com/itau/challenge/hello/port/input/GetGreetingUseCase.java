package br.com.itau.challenge.hello.port.input;

import br.com.itau.challenge.hello.domain.model.Greeting;

@FunctionalInterface
public interface GetGreetingUseCase {

	Greeting getGreeting(String requesterName);
}
