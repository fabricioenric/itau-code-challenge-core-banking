package br.com.itau.challenge.hello.application;

import br.com.itau.challenge.hello.domain.exception.AccountBalanceNotFoundException;
import br.com.itau.challenge.hello.domain.model.AccountBalance;
import br.com.itau.challenge.hello.port.input.GetBalanceUseCase;
import br.com.itau.challenge.hello.port.output.BalanceRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetBalanceService implements GetBalanceUseCase {

    private final BalanceRepository repository;

    public GetBalanceService(BalanceRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountBalance execute(UUID accountId) {
        return repository.findByAccountId(accountId)
                .orElseThrow(() -> new AccountBalanceNotFoundException(accountId));
    }
}