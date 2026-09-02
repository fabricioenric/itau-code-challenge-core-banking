package br.com.itau.challenge.corebanking.adapter.input.kafka.dto;

import java.math.BigDecimal;

public record TransactionEventDTO(
    TransactionDTO transaction,
    AccountDTO account
) {

    public record TransactionDTO(
        String id,
        String type,
        BigDecimal amount,
        String currency,
        String status,
        long timestamp
    ) {}

    public record AccountDTO(
        String id,
        String owner,
        long created_at,
        String status,
        BalanceDTO balance
    ) {}

    public record BalanceDTO(
        BigDecimal amount,
        String currency
    ) {}
}