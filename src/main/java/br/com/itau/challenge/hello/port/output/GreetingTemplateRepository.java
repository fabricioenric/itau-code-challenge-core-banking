package br.com.itau.challenge.hello.port.output;

import br.com.itau.challenge.hello.domain.model.GreetingTemplate;

@FunctionalInterface
public interface GreetingTemplateRepository {

	void save(GreetingTemplate greetingTemplate);
}
