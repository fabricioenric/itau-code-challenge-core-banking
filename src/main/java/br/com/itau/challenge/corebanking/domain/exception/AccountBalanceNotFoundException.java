package br.com.itau.challenge.corebanking.domain.exception;

import java.util.UUID;

public class AccountBalanceNotFoundException extends RuntimeException {

    private final UUID accountId;

    public AccountBalanceNotFoundException(UUID accountId) {
        super("Saldo não encontrado para a conta: " + accountId);
        this.accountId = accountId;
    }

    public UUID getAccountId() {
        return accountId;
    }
}