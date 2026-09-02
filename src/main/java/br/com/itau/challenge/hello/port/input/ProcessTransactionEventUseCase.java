package br.com.itau.challenge.hello.port.input;

import br.com.itau.challenge.hello.domain.model.TransactionEvent;

public interface ProcessTransactionEventUseCase {
    void process(TransactionEvent event);
}