package br.com.itau.challenge.corebanking.domain.model;

import java.util.UUID;

public record TransactionEvent(
    UUID transactionId,
    TransactionType type,
    TransactionStatus status,
    long timestampMicros,
    UUID accountId,
    UUID accountOwner,
    Balance balance
) {}