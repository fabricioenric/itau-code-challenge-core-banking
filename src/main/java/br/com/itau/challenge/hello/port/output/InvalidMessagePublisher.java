package br.com.itau.challenge.hello.port.output;

public interface InvalidMessagePublisher {

    void publish(String rawPayload, String reason, ErrorType errorType);

    enum ErrorType {
        DESERIALIZATION_ERROR,
        VALIDATION_ERROR,
        PROCESSING_ERROR
    }
}