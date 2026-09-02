package br.com.itau.challenge.corebanking.port.input;

import br.com.itau.challenge.corebanking.domain.model.TransactionEvent;

public interface ProcessTransactionEventUseCase {
    void process(TransactionEvent event);
}