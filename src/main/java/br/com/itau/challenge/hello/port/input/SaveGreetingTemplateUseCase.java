package br.com.itau.challenge.hello.port.input;

import br.com.itau.challenge.hello.domain.model.GreetingTemplate;

@FunctionalInterface
public interface SaveGreetingTemplateUseCase {

	void saveGreetingTemplate(GreetingTemplate greetingTemplate);
}
