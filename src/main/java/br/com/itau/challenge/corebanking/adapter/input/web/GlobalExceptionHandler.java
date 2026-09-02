package br.com.itau.challenge.corebanking.adapter.input.web;

import br.com.itau.challenge.corebanking.adapter.input.web.dto.ErrorResponse;
import br.com.itau.challenge.corebanking.domain.exception.AccountBalanceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccountBalanceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(AccountBalanceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "ACCOUNT_BALANCE_NOT_FOUND",
                        "Saldo não encontrado para a conta informada"
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUuid(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "INVALID_ACCOUNT_ID",
                        "O identificador da conta informado é inválido"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Erro inesperado ao processar requisição", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "INTERNAL_ERROR",
                        "Ocorreu um erro inesperado ao processar a requisição"
                ));
    }
}