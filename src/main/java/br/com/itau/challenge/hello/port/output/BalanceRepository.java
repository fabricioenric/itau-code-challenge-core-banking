package br.com.itau.challenge.hello.port.output;

import br.com.itau.challenge.hello.domain.model.AccountBalance;

import java.util.Optional;
import java.util.UUID;

public interface BalanceRepository {

    boolean saveIfNewer(AccountBalance accountBalance);

    Optional<AccountBalance> findByAccountId(UUID accountId);
}