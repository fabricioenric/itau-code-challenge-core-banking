package br.com.itau.challenge.hello.domain.exception;

public class InvalidTransactionEventException extends RuntimeException {
    public InvalidTransactionEventException(String message) {
        super(message);
    }
}