package br.com.itau.challenge.corebanking.application;

import br.com.itau.challenge.corebanking.domain.exception.InvalidTransactionEventException;
import br.com.itau.challenge.corebanking.domain.model.AccountBalance;
import br.com.itau.challenge.corebanking.domain.model.TransactionEvent;
import br.com.itau.challenge.corebanking.port.input.ProcessTransactionEventUseCase;
import br.com.itau.challenge.corebanking.port.output.BalanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ProcessTransactionEventService implements ProcessTransactionEventUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessTransactionEventService.class);

    private final BalanceRepository repository;

    public ProcessTransactionEventService(BalanceRepository repository) {
        this.repository = repository;
    }

    @Override
    public void process(TransactionEvent event) {
        validate(event);

        AccountBalance candidate = new AccountBalance(
                event.accountId(),
                event.accountOwner(),
                event.balance(),
                event.timestampMicros(),
                event.transactionId()
        );

        boolean applied = repository.saveIfNewer(candidate);

        if (applied) {
            log.info("Saldo atualizado. accountId={}, transactionId={}, status={}, timestamp={}",
                    event.accountId(), event.transactionId(), event.status(), event.timestampMicros());
        } else {
            log.info("Evento ignorado (mais antigo ou duplicado). accountId={}, transactionId={}, timestamp={}",
                    event.accountId(), event.transactionId(), event.timestampMicros());
        }
    }

    private void validate(TransactionEvent event) {
        if (event == null) {
            throw new InvalidTransactionEventException("Evento nulo");
        }
        if (event.transactionId() == null) {
            throw new InvalidTransactionEventException("transaction.id ausente");
        }
        if (event.accountId() == null) {
            throw new InvalidTransactionEventException("account.id ausente");
        }
        if (event.type() == null) {
            throw new InvalidTransactionEventException("transaction.type ausente ou inválido");
        }
        if (event.status() == null) {
            throw new InvalidTransactionEventException("transaction.status ausente ou inválido");
        }
        if (event.timestampMicros() <= 0) {
            throw new InvalidTransactionEventException("timestamp inválido: " + event.timestampMicros());
        }
        if (event.balance() == null || event.balance().getAmount() == null) {
            throw new InvalidTransactionEventException("balance ausente");
        }
        if (event.balance().getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTransactionEventException(
                    "balance.amount negativo: " + event.balance().getAmount());
        }
        if (event.balance().getCurrency() == null || event.balance().getCurrency().isBlank()) {
            throw new InvalidTransactionEventException("balance.currency ausente");
        }
    }
}