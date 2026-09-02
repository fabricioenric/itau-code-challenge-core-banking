package br.com.itau.challenge.corebanking.domain.exception;

public class InvalidTransactionEventException extends RuntimeException {
    public InvalidTransactionEventException(String message) {
        super(message);
    }
}