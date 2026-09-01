package br.com.itau.challenge.hello.application;

import br.com.itau.challenge.hello.domain.exception.BlankRequesterNameException;
import br.com.itau.challenge.hello.domain.model.Greeting;
import br.com.itau.challenge.hello.port.input.GetGreetingUseCase;
import br.com.itau.challenge.hello.port.output.GreetingTemplateProvider;
import org.springframework.stereotype.Service;

@Service
public class GreetingService implements GetGreetingUseCase {

	private final GreetingTemplateProvider greetingTemplateProvider;

	public GreetingService(GreetingTemplateProvider greetingTemplateProvider) {
		this.greetingTemplateProvider = greetingTemplateProvider;
	}

	@Override
	public Greeting getGreeting(String requesterName) {
		if (requesterName.isBlank()) {
			throw new BlankRequesterNameException();
		}

		String template = greetingTemplateProvider.randomTemplate();
		return new Greeting(template.formatted(requesterName.trim()));
	}
}
