package br.com.itau.challenge.corebanking.domain.model;

import java.time.Instant;
import java.util.UUID;

public final class AccountBalance {

    private final UUID accountId;
    private final UUID owner;
    private final Balance balance;
    private final long updatedAtMicros;
    private final UUID lastTransactionId;

    public AccountBalance(UUID accountId, UUID owner, Balance balance,
                           long updatedAtMicros, UUID lastTransactionId) {
        if (accountId == null) {
            throw new IllegalArgumentException("accountId não pode ser nulo");
        }
        if (balance == null) {
            throw new IllegalArgumentException("balance não pode ser nulo");
        }
        if (updatedAtMicros <= 0) {
            throw new IllegalArgumentException("updatedAtMicros deve ser positivo");
        }
        this.accountId = accountId;
        this.owner = owner;
        this.balance = balance;
        this.updatedAtMicros = updatedAtMicros;
        this.lastTransactionId = lastTransactionId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getOwner() {
        return owner;
    }

    public Balance getBalance() {
        return balance;
    }

    public long getUpdatedAtMicros() {
        return updatedAtMicros;
    }

    public UUID getLastTransactionId() {
        return lastTransactionId;
    }

    public Instant getUpdatedAtInstant() {
        return Instant.ofEpochSecond(
                updatedAtMicros / 1_000_000,
                (updatedAtMicros % 1_000_000) * 1_000
        );
    }
}