package br.com.itau.challenge.corebanking.adapter.input.kafka;

import br.com.itau.challenge.corebanking.port.output.InvalidMessagePublisher;
import br.com.itau.challenge.corebanking.port.output.InvalidMessagePublisher.ErrorType;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KafkaConsumerConfigTest {

    private KafkaConsumerConfig config;

    @BeforeEach
    void setUp() {
        config = new KafkaConsumerConfig();
        ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:19092");
        ReflectionTestUtils.setField(config, "groupId", "consulta-saldo-consumer");
    }

    @Test
    void deveConfigurarConsumerFactoryComPropriedadesManuais() {
        ConsumerFactory<String, String> factory = config.consumerFactory();

        assertEquals(DefaultKafkaConsumerFactory.class, factory.getClass());
        var props = ((DefaultKafkaConsumerFactory<String, String>) factory).getConfigurationProperties();
        assertEquals("localhost:19092", props.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("consulta-saldo-consumer", props.get(ConsumerConfig.GROUP_ID_CONFIG));
        assertEquals(false, props.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG));
        assertEquals("earliest", props.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
    }

    @Test
    void deveConfigurarContainerFactoryComAckModeManual() {
        ConsumerFactory<String, String> consumerFactory = config.consumerFactory();
        InvalidMessagePublisher publisher = mock(InvalidMessagePublisher.class);

        ConcurrentKafkaListenerContainerFactory<String, String> containerFactory =
                config.kafkaListenerContainerFactory(consumerFactory, publisher);

        assertEquals(ContainerProperties.AckMode.MANUAL,
                containerFactory.getContainerProperties().getAckMode());
    }

    @Test
    void deveConfigurarBackOffExponencialComLimiteDeTentativas() {
        ExponentialBackOffWithMaxRetries backOff = config.backOff();

        assertEquals(500L, backOff.getInitialInterval());
        assertEquals(2.0, backOff.getMultiplier());
        assertEquals(10_000L, backOff.getMaxInterval());
    }

    @Test
    void devePublicarNaDltQuandoRetentativasSeEsgotarem() {
        InvalidMessagePublisher publisher = mock(InvalidMessagePublisher.class);
        ConsumerRecordRecoverer recoverer = config.recoverer(publisher);
        ConsumerRecord<String, String> record =
                new ConsumerRecord<>("transacoes-financeiras-processadas", 0, 42L, "key", "{\"payload\":true}");
        RuntimeException exception = new RuntimeException("DynamoDB indisponível");

        recoverer.accept(record, exception);

        verify(publisher).publish("{\"payload\":true}", "DynamoDB indisponível", ErrorType.PROCESSING_ERROR);
    }

    @Test
    void devePublicarPayloadNuloQuandoValorDoRegistroForNulo() {
        InvalidMessagePublisher publisher = mock(InvalidMessagePublisher.class);
        ConsumerRecordRecoverer recoverer = config.recoverer(publisher);
        ConsumerRecord<String, String> record =
                new ConsumerRecord<>("transacoes-financeiras-processadas", 0, 42L, "key", null);
        RuntimeException exception = new RuntimeException("falha qualquer");

        recoverer.accept(record, exception);

        verify(publisher).publish(null, "falha qualquer", ErrorType.PROCESSING_ERROR);
    }
}
