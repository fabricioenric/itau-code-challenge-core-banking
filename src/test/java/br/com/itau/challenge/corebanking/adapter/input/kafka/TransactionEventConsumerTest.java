package br.com.itau.challenge.corebanking.adapter.input.kafka;

import br.com.itau.challenge.corebanking.domain.exception.InvalidTransactionEventException;
import br.com.itau.challenge.corebanking.port.input.ProcessTransactionEventUseCase;
import br.com.itau.challenge.corebanking.port.output.InvalidMessagePublisher;
import br.com.itau.challenge.corebanking.port.output.InvalidMessagePublisher.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TransactionEventConsumerTest {

    private ObjectMapper objectMapper;
    private ProcessTransactionEventUseCase processTransactionEventUseCase;
    private InvalidMessagePublisher invalidMessagePublisher;
    private Acknowledgment acknowledgment;
    private TransactionEventConsumer consumer;

    private static final String VALID_PAYLOAD = """
            {
              "transaction": {
                "id": "8e8ae808-b154-48b5-9f3e-553935cc4543",
                "type": "CREDIT",
                "amount": 97.07,
                "currency": "BRL",
                "status": "APPROVED",
                "timestamp": 1751641364589998
              },
              "account": {
                "id": "5b19c8b6-0cc4-4c72-a989-0c2ee15fa975",
                "owner": "315e3cfe-f4af-4cd2-b298-a449e614349a",
                "created_at": 1634874339000000,
                "status": "ENABLED",
                "balance": {
                  "amount": 183.12,
                  "currency": "BRL"
                }
              }
            }
            """;

    @BeforeEach
    void setUp() {
        objectMapper = mock(ObjectMapper.class);
        processTransactionEventUseCase = mock(ProcessTransactionEventUseCase.class);
        invalidMessagePublisher = mock(InvalidMessagePublisher.class);
        acknowledgment = mock(Acknowledgment.class);
        consumer = new TransactionEventConsumer(objectMapper, processTransactionEventUseCase, invalidMessagePublisher);
    }

    @Test
    void deveProcessarEConfirmarQuandoMensagemForValida() {
        var dto = mock(br.com.itau.challenge.corebanking.adapter.input.kafka.dto.TransactionEventDTO.class);
        when(objectMapper.readValue(anyString(), eq(
                br.com.itau.challenge.corebanking.adapter.input.kafka.dto.TransactionEventDTO.class))).thenReturn(dto);

        try (var mapperMock = mockStatic(
                br.com.itau.challenge.corebanking.adapter.input.kafka.mapper.TransactionEventMapper.class)) {
            var event = mock(br.com.itau.challenge.corebanking.domain.model.TransactionEvent.class);
            mapperMock.when(() -> br.com.itau.challenge.corebanking.adapter.input.kafka.mapper.TransactionEventMapper
                    .toDomain(dto)).thenReturn(event);

            consumer.consume(VALID_PAYLOAD, acknowledgment);

            verify(processTransactionEventUseCase).process(event);
            verify(acknowledgment).acknowledge();
            verifyNoInteractions(invalidMessagePublisher);
        }
    }

    @Test
    void devePublicarNaDltEConfirmarQuandoFalharDesserializacao() {
        when(objectMapper.readValue(anyString(), eq(
                br.com.itau.challenge.corebanking.adapter.input.kafka.dto.TransactionEventDTO.class)))
                .thenThrow(new RuntimeException("json malformado"));

        consumer.consume("{invalid}", acknowledgment);

        verify(invalidMessagePublisher).publish(eq("{invalid}"), anyString(), eq(ErrorType.DESERIALIZATION_ERROR));
        verify(acknowledgment).acknowledge();
        verifyNoInteractions(processTransactionEventUseCase);
    }

    @Test
    void devePublicarNaDltEConfirmarQuandoFalharValidacaoDeNegocio() {
        var dto = mock(br.com.itau.challenge.corebanking.adapter.input.kafka.dto.TransactionEventDTO.class);
        when(objectMapper.readValue(anyString(), eq(
                br.com.itau.challenge.corebanking.adapter.input.kafka.dto.TransactionEventDTO.class))).thenReturn(dto);

        try (var mapperMock = mockStatic(
                br.com.itau.challenge.corebanking.adapter.input.kafka.mapper.TransactionEventMapper.class)) {
            mapperMock.when(() -> br.com.itau.challenge.corebanking.adapter.input.kafka.mapper.TransactionEventMapper
                    .toDomain(dto)).thenThrow(new InvalidTransactionEventException("account.id ausente"));

            consumer.consume(VALID_PAYLOAD, acknowledgment);

            verify(invalidMessagePublisher).publish(eq(VALID_PAYLOAD), eq("account.id ausente"),
                    eq(ErrorType.VALIDATION_ERROR));
            verify(acknowledgment).acknowledge();
            verifyNoInteractions(processTransactionEventUseCase);
        }
    }

    @Test
    void naoDeveConfirmarQuandoProcessamentoLancarErroTransitorio() {
        var dto = mock(br.com.itau.challenge.corebanking.adapter.input.kafka.dto.TransactionEventDTO.class);
        when(objectMapper.readValue(anyString(), eq(
                br.com.itau.challenge.corebanking.adapter.input.kafka.dto.TransactionEventDTO.class))).thenReturn(dto);

        try (var mapperMock = mockStatic(
                br.com.itau.challenge.corebanking.adapter.input.kafka.mapper.TransactionEventMapper.class)) {
            var event = mock(br.com.itau.challenge.corebanking.domain.model.TransactionEvent.class);
            mapperMock.when(() -> br.com.itau.challenge.corebanking.adapter.input.kafka.mapper.TransactionEventMapper
                    .toDomain(dto)).thenReturn(event);

            doThrow(new RuntimeException("DynamoDB indisponível"))
                    .when(processTransactionEventUseCase).process(event);

            org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                    () -> consumer.consume(VALID_PAYLOAD, acknowledgment));

            verify(acknowledgment, never()).acknowledge();
            verifyNoInteractions(invalidMessagePublisher);
        }
    }
}