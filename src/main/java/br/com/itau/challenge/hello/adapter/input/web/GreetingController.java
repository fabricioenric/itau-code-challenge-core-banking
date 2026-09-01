package br.com.itau.challenge.hello.adapter.input.web;

import br.com.itau.challenge.hello.adapter.input.web.dto.GreetingResponse;
import br.com.itau.challenge.hello.domain.model.Greeting;
import br.com.itau.challenge.hello.port.input.GetGreetingUseCase;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

	private final GetGreetingUseCase getGreetingUseCase;

	public GreetingController(GetGreetingUseCase getGreetingUseCase) {
		this.getGreetingUseCase = getGreetingUseCase;
	}

	@GetMapping(value = "/hello", produces = MediaType.APPLICATION_JSON_VALUE)
	public GreetingResponse hello(@RequestParam String name) {
		Greeting greeting = getGreetingUseCase.getGreeting(name);
		return new GreetingResponse(greeting.message());
	}
}
