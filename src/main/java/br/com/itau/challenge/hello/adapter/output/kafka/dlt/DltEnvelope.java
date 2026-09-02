package br.com.itau.challenge.hello.adapter.output.kafka.dlt;

import java.time.Instant;

public record DltEnvelope(
    String originalPayload,
    String reason,
    String errorType,
    String sourceTopic,
    Instant failedAt
) {

    public static DltEnvelope of(String originalPayload, String reason, String errorType, String sourceTopic) {
        return new DltEnvelope(originalPayload, reason, errorType, sourceTopic, Instant.now());
    }
}