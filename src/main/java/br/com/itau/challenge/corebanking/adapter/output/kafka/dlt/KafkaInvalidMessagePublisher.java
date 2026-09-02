package br.com.itau.challenge.corebanking.adapter.output.kafka.dlt;

import br.com.itau.challenge.corebanking.port.output.InvalidMessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class KafkaInvalidMessagePublisher implements InvalidMessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaInvalidMessagePublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String sourceTopic;
    private final String dltTopic;

    public KafkaInvalidMessagePublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${TRANSACTIONS_TOPIC:transacoes-financeiras-processadas}") String sourceTopic,
            @Value("${TRANSACTIONS_DLT_TOPIC:transacoes-financeiras-processadas.DLT.manual}") String dltTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.sourceTopic = sourceTopic;
        this.dltTopic = dltTopic;
    }

    @Override
    public void publish(String rawPayload, String reason, ErrorType errorType) {
        try {
            DltEnvelope envelope = DltEnvelope.of(rawPayload, reason, errorType.name(), sourceTopic);
            String serialized = objectMapper.writeValueAsString(envelope);

            kafkaTemplate.send(dltTopic, serialized).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Falha ao publicar mensagem inválida na DLT. reason={}", reason, ex);
                } else {
                    log.info("Mensagem inválida publicada na DLT. reason={}, errorType={}", reason, errorType);
                }
            });
        } catch (Exception ex) {
            // Falha ao publicar na DLT não deve derrubar o consumer principal.
            // O log estruturado é a última linha de defesa para não perder o rastro.
            log.error("Erro crítico: não foi possível publicar mensagem na DLT. reason={}", reason, ex);
        }
    }
}