package br.com.itau.challenge.hello.adapter.input.kafka;

import br.com.itau.challenge.hello.port.output.InvalidMessagePublisher;
import br.com.itau.challenge.hello.port.output.InvalidMessagePublisher.ErrorType;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import java.util.HashMap;
import java.util.Map;

/**
 * Mensagens estruturalmente inválidas (falha de desserialização/validação) já são tratadas e
 * confirmadas dentro de {@link TransactionEventConsumer}. Esta configuração cobre o outro caso:
 * falhas transitórias durante o processamento (ex.: DynamoDB indisponível), que o listener
 * propaga sem confirmar o offset. Aqui elas são reprocessadas com backoff exponencial e, se
 * continuarem falhando, publicadas na DLT pelo mesmo {@link InvalidMessagePublisher} usado para
 * as demais rejeições — mantendo um único formato de envelope na DLT.
 */
@Configuration
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Value("${KAFKA_BOOTSTRAP_SERVERS:localhost:19092}")
    private String bootstrapServers;

    @Value("${KAFKA_CONSUMER_GROUP_ID:consulta-saldo-consumer}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            InvalidMessagePublisher invalidMessagePublisher) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer(invalidMessagePublisher), backOff()));
        return factory;
    }

    ExponentialBackOffWithMaxRetries backOff() {
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(5);
        backOff.setInitialInterval(500L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(10_000L);
        return backOff;
    }

    ConsumerRecordRecoverer recoverer(InvalidMessagePublisher invalidMessagePublisher) {
        return (record, exception) -> {
            String payload = record.value() != null ? record.value().toString() : null;
            log.error("Falhas de processamento esgotadas para o registro (partition={}, offset={}). Publicando na DLT.",
                    record.partition(), record.offset(), exception);
            invalidMessagePublisher.publish(payload, exception.getMessage(), ErrorType.PROCESSING_ERROR);
        };
    }
}
