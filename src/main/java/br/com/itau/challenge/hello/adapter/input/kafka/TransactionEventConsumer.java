package br.com.itau.challenge.hello.adapter.input.kafka;

import br.com.itau.challenge.hello.adapter.input.kafka.dto.TransactionEventDTO;
import br.com.itau.challenge.hello.adapter.input.kafka.mapper.TransactionEventMapper;
import br.com.itau.challenge.hello.domain.exception.InvalidTransactionEventException;
import br.com.itau.challenge.hello.domain.model.TransactionEvent;
import br.com.itau.challenge.hello.port.input.ProcessTransactionEventUseCase;
import br.com.itau.challenge.hello.port.output.InvalidMessagePublisher;
import br.com.itau.challenge.hello.port.output.InvalidMessagePublisher.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class TransactionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final ProcessTransactionEventUseCase processTransactionEventUseCase;
    private final InvalidMessagePublisher invalidMessagePublisher;

    public TransactionEventConsumer(ObjectMapper objectMapper,
                                     ProcessTransactionEventUseCase processTransactionEventUseCase,
                                     InvalidMessagePublisher invalidMessagePublisher) {
        this.objectMapper = objectMapper;
        this.processTransactionEventUseCase = processTransactionEventUseCase;
        this.invalidMessagePublisher = invalidMessagePublisher;
    }

    @KafkaListener(
        topics = "${TRANSACTIONS_TOPIC:transacoes-financeiras-processadas}",
        groupId = "${KAFKA_CONSUMER_GROUP_ID:consulta-saldo-consumer}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(String rawPayload, Acknowledgment acknowledgment) {
        TransactionEventDTO dto;

        try {
            dto = objectMapper.readValue(rawPayload, TransactionEventDTO.class);
        } catch (Exception ex) {
            log.warn("Falha ao deserializar mensagem. motivo={}", ex.getMessage());
            invalidMessagePublisher.publish(rawPayload, ex.getMessage(), ErrorType.DESERIALIZATION_ERROR);
            acknowledgment.acknowledge();
            return;
        }

        TransactionEvent event;
        try {
            event = TransactionEventMapper.toDomain(dto);
        } catch (InvalidTransactionEventException ex) {
            log.warn("Mensagem estruturalmente inválida. motivo={}", ex.getMessage());
            invalidMessagePublisher.publish(rawPayload, ex.getMessage(), ErrorType.VALIDATION_ERROR);
            acknowledgment.acknowledge();
            return;
        }

        try {
            processTransactionEventUseCase.process(event);
            acknowledgment.acknowledge();
        } catch (Exception ex) {
            log.error("Erro ao processar evento. Será reprocessado conforme política de retry.", ex);
            throw ex;
        }
    }
}