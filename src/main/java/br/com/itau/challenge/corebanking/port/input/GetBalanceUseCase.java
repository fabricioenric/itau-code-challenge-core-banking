package br.com.itau.challenge.corebanking.port.input;

import br.com.itau.challenge.corebanking.domain.model.AccountBalance;

import java.util.UUID;

public interface GetBalanceUseCase {
    AccountBalance execute(UUID accountId);
}