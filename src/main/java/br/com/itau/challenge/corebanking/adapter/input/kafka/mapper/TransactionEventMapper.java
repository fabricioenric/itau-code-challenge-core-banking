package br.com.itau.challenge.corebanking.adapter.input.kafka.mapper;

import br.com.itau.challenge.corebanking.adapter.input.kafka.dto.TransactionEventDTO;
import br.com.itau.challenge.corebanking.domain.exception.InvalidTransactionEventException;
import br.com.itau.challenge.corebanking.domain.model.Balance;
import br.com.itau.challenge.corebanking.domain.model.TransactionEvent;
import br.com.itau.challenge.corebanking.domain.model.TransactionStatus;
import br.com.itau.challenge.corebanking.domain.model.TransactionType;

import java.util.UUID;

public final class TransactionEventMapper {

    private TransactionEventMapper() {
    }

    public static TransactionEvent toDomain(TransactionEventDTO dto) {
        if (dto == null || dto.transaction() == null || dto.account() == null) {
            throw new InvalidTransactionEventException(
                    "Payload incompleto: transaction ou account ausente");
        }

        var transaction = dto.transaction();
        var account = dto.account();

        if (account.balance() == null) {
            throw new InvalidTransactionEventException("account.balance ausente");
        }

        try {
            return new TransactionEvent(
                    UUID.fromString(transaction.id()),
                    parseType(transaction.type()),
                    parseStatus(transaction.status()),
                    transaction.timestamp(),
                    UUID.fromString(account.id()),
                    account.owner() != null ? UUID.fromString(account.owner()) : null,
                    new Balance(account.balance().amount(), account.balance().currency())
            );
        } catch (IllegalArgumentException ex) {
            throw new InvalidTransactionEventException("Erro ao converter payload: " + ex.getMessage());
        }
    }

    private static TransactionType parseType(String value) {
        try {
            return TransactionType.valueOf(value);
        } catch (Exception ex) {
            throw new InvalidTransactionEventException("transaction.type inválido: " + value);
        }
    }

    private static TransactionStatus parseStatus(String value) {
        try {
            return TransactionStatus.valueOf(value);
        } catch (Exception ex) {
            throw new InvalidTransactionEventException("transaction.status inválido: " + value);
        }
    }
}