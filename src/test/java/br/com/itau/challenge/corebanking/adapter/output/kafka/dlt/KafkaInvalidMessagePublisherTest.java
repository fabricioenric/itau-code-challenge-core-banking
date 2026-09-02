package br.com.itau.challenge.corebanking.adapter.output.kafka.dlt;

import br.com.itau.challenge.corebanking.port.output.InvalidMessagePublisher.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class KafkaInvalidMessagePublisherTest {

    private KafkaTemplate<String, String> kafkaTemplate;
    private KafkaInvalidMessagePublisher publisher;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        publisher = new KafkaInvalidMessagePublisher(
                kafkaTemplate,
                new ObjectMapper(),
                "transacoes-financeiras-processadas",
                "transacoes-financeiras-processadas.DLT.manual"
        );
    }

    @Test
    void devePublicarEnvelopeComContextoDoErro() {
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.complete(null);
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(future);

        publisher.publish("{invalid json}", "campo obrigatório ausente", ErrorType.VALIDATION_ERROR);

        var topicCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        var payloadCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), payloadCaptor.capture());

        assertTrue(topicCaptor.getValue().endsWith(".DLT.manual"));
        assertTrue(payloadCaptor.getValue().contains("campo obrigatório ausente"));
        assertTrue(payloadCaptor.getValue().contains("VALIDATION_ERROR"));
        assertTrue(payloadCaptor.getValue().contains("transacoes-financeiras-processadas"));
    }

    @Test
    void naoDeveLancarExcecaoQuandoFalhaSincronaAoPublicar() {
        when(kafkaTemplate.send(anyString(), anyString()))
                .thenThrow(new RuntimeException("Kafka indisponível"));

        publisher.publish("{}", "motivo qualquer", ErrorType.DESERIALIZATION_ERROR);

        verify(kafkaTemplate).send(anyString(), anyString());
    }

    @Test
    void deveLogarErroQuandoCallbackRetornarFalha() {
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("timeout"));
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(future);

        publisher.publish("{}", "motivo qualquer", ErrorType.VALIDATION_ERROR);

        verify(kafkaTemplate).send(anyString(), anyString());
    }
}