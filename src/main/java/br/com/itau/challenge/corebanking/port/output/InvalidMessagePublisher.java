package br.com.itau.challenge.corebanking.port.output;

public interface InvalidMessagePublisher {

    void publish(String rawPayload, String reason, ErrorType errorType);

    enum ErrorType {
        DESERIALIZATION_ERROR,
        VALIDATION_ERROR,
        PROCESSING_ERROR
    }
}