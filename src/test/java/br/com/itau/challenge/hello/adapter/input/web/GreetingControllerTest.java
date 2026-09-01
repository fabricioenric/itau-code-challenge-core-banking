package br.com.itau.challenge.hello.adapter.input.web;

import br.com.itau.challenge.hello.port.output.GreetingTemplateProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GreetingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GreetingTemplateProvider greetingTemplateProvider;

	@BeforeEach
	void stubTemplateProvider() {
		given(greetingTemplateProvider.randomTemplate()).willReturn("Hello, %s!");
	}

	@Test
	void shouldReturnAJsonGreetingContainingTheRequesterName() throws Exception {
		mockMvc.perform(get("/hello").param("name", "Ada"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.message", containsString("Ada")));
	}

	@Test
	void shouldSupportUnicodeCharactersInTheRequesterName() throws Exception {
		mockMvc.perform(get("/hello").param("name", "José"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.message", containsString("José")));
	}

	@Test
	void shouldRejectUnsupportedHttpMethods() throws Exception {
		mockMvc.perform(post("/hello").param("name", "Ada"))
				.andExpect(status().isMethodNotAllowed());
	}
}
