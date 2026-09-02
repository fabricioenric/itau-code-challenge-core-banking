package br.com.itau.challenge.hello.adapter.input.web;

import br.com.itau.challenge.hello.adapter.input.web.dto.ErrorResponse;
import br.com.itau.challenge.hello.domain.exception.AccountBalanceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deveRetornar404QuandoContaNaoEncontrada() {
        UUID accountId = UUID.randomUUID();
        AccountBalanceNotFoundException ex = new AccountBalanceNotFoundException(accountId);

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("ACCOUNT_BALANCE_NOT_FOUND", response.getBody().code());
        assertEquals("Saldo não encontrado para a conta informada", response.getBody().message());
    }

    @Test
    void deveRetornar400QuandoUuidInvalido() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);

        ResponseEntity<ErrorResponse> response = handler.handleInvalidUuid(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("INVALID_ACCOUNT_ID", response.getBody().code());
        assertEquals("O identificador da conta informado é inválido", response.getBody().message());
    }

    @Test
    void deveRetornar500ParaErroGenericoSemExporDetalhesInternos() {
        RuntimeException ex = new RuntimeException("stacktrace sensível com detalhes de infraestrutura");

        ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_ERROR", response.getBody().code());
        assertEquals("Ocorreu um erro inesperado ao processar a requisição", response.getBody().message());
    }
}