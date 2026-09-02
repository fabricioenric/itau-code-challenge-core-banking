package br.com.itau.challenge.hello.adapter.input.kafka.mapper;

import br.com.itau.challenge.hello.adapter.input.kafka.dto.TransactionEventDTO;
import br.com.itau.challenge.hello.domain.exception.InvalidTransactionEventException;
import br.com.itau.challenge.hello.domain.model.TransactionEvent;
import br.com.itau.challenge.hello.domain.model.TransactionStatus;
import br.com.itau.challenge.hello.domain.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionEventMapperTest {

    private static final String TRANSACTION_ID = "8e8ae808-b154-48b5-9f3e-553935cc4543";
    private static final String ACCOUNT_ID = "5b19c8b6-0cc4-4c72-a989-0c2ee15fa975";
    private static final String OWNER_ID = "315e3cfe-f4af-4cd2-b298-a449e614349a";

    @Test
    void deveMapearDtoValidoParaDominio() {
        TransactionEventDTO dto = new TransactionEventDTO(
                new TransactionEventDTO.TransactionDTO(
                        TRANSACTION_ID, "CREDIT", new BigDecimal("97.07"), "BRL", "APPROVED", 1751641364589998L
                ),
                new TransactionEventDTO.AccountDTO(
                        ACCOUNT_ID, OWNER_ID, 1634874339000000L, "ENABLED",
                        new TransactionEventDTO.BalanceDTO(new BigDecimal("183.12"), "BRL")
                )
        );

        TransactionEvent event = TransactionEventMapper.toDomain(dto);

        assertEquals(UUID.fromString(TRANSACTION_ID), event.transactionId());
        assertEquals(TransactionType.CREDIT, event.type());
        assertEquals(TransactionStatus.APPROVED, event.status());
        assertEquals(1751641364589998L, event.timestampMicros());
        assertEquals(UUID.fromString(ACCOUNT_ID), event.accountId());
        assertEquals(UUID.fromString(OWNER_ID), event.accountOwner());
        assertEquals(new BigDecimal("183.12"), event.balance().getAmount());
        assertEquals("BRL", event.balance().getCurrency());
    }

    @Test
    void deveMapearStatusDeclined() {
        TransactionEventDTO dto = validDtoWithStatus("DECLINED");

        TransactionEvent event = TransactionEventMapper.toDomain(dto);

        assertEquals(TransactionStatus.DECLINED, event.status());
    }

    @Test
    void deveLancarExcecaoQuandoTransactionForNulo() {
        TransactionEventDTO dto = new TransactionEventDTO(null, validAccountDto());

        assertThrows(InvalidTransactionEventException.class, () -> TransactionEventMapper.toDomain(dto));
    }

    @Test
    void deveLancarExcecaoQuandoAccountForNulo() {
        TransactionEventDTO dto = new TransactionEventDTO(validTransactionDto("APPROVED"), null);

        assertThrows(InvalidTransactionEventException.class, () -> TransactionEventMapper.toDomain(dto));
    }

    @Test
    void deveLancarExcecaoQuandoDtoForNulo() {
        assertThrows(InvalidTransactionEventException.class, () -> TransactionEventMapper.toDomain(null));
    }

    @Test
    void deveLancarExcecaoQuandoBalanceForNulo() {
        TransactionEventDTO dto = new TransactionEventDTO(
                validTransactionDto("APPROVED"),
                new TransactionEventDTO.AccountDTO(ACCOUNT_ID, OWNER_ID, 0L, "ENABLED", null)
        );

        assertThrows(InvalidTransactionEventException.class, () -> TransactionEventMapper.toDomain(dto));
    }

    @Test
    void deveLancarExcecaoQuandoTransactionIdForUuidInvalido() {
        TransactionEventDTO dto = new TransactionEventDTO(
                new TransactionEventDTO.TransactionDTO(
                        "id-invalido", "CREDIT", new BigDecimal("10.00"), "BRL", "APPROVED", 123L
                ),
                validAccountDto()
        );

        assertThrows(InvalidTransactionEventException.class, () -> TransactionEventMapper.toDomain(dto));
    }

    @Test
    void deveLancarExcecaoQuandoAccountIdForUuidInvalido() {
        TransactionEventDTO dto = new TransactionEventDTO(
                validTransactionDto("APPROVED"),
                new TransactionEventDTO.AccountDTO(
                        "id-invalido", OWNER_ID, 0L, "ENABLED",
                        new TransactionEventDTO.BalanceDTO(new BigDecimal("10.00"), "BRL")
                )
        );

        assertThrows(InvalidTransactionEventException.class, () -> TransactionEventMapper.toDomain(dto));
    }

    @Test
    void deveLancarExcecaoQuandoTypeForInvalido() {
        TransactionEventDTO dto = new TransactionEventDTO(
                new TransactionEventDTO.TransactionDTO(
                        TRANSACTION_ID, "TRANSFER", new BigDecimal("10.00"), "BRL", "APPROVED", 123L
                ),
                validAccountDto()
        );

        assertThrows(InvalidTransactionEventException.class, () -> TransactionEventMapper.toDomain(dto));
    }

    @Test
    void deveLancarExcecaoQuandoStatusForInvalido() {
        TransactionEventDTO dto = validDtoWithStatus("PENDING");

        assertThrows(InvalidTransactionEventException.class, () -> TransactionEventMapper.toDomain(dto));
    }

    @Test
    void deveLancarExcecaoQuandoOwnerForNulo() {
        TransactionEventDTO dto = new TransactionEventDTO(
                validTransactionDto("APPROVED"),
                new TransactionEventDTO.AccountDTO(
                        ACCOUNT_ID, null, 0L, "ENABLED",
                        new TransactionEventDTO.BalanceDTO(new BigDecimal("10.00"), "BRL")
                )
        );

        TransactionEvent event = TransactionEventMapper.toDomain(dto);

        assertEquals(null, event.accountOwner());
    }

    @Test
    void deveLancarExcecaoQuandoAmountForNegativo() {
        TransactionEventDTO dto = new TransactionEventDTO(
                validTransactionDto("APPROVED"),
                new TransactionEventDTO.AccountDTO(
                        ACCOUNT_ID, OWNER_ID, 0L, "ENABLED",
                        new TransactionEventDTO.BalanceDTO(new BigDecimal("-10.00"), "BRL")
                )
        );

        assertThrows(InvalidTransactionEventException.class, () -> TransactionEventMapper.toDomain(dto));
    }

    private TransactionEventDTO validDtoWithStatus(String status) {
        return new TransactionEventDTO(validTransactionDto(status), validAccountDto());
    }

    private TransactionEventDTO.TransactionDTO validTransactionDto(String status) {
        return new TransactionEventDTO.TransactionDTO(
                TRANSACTION_ID, "CREDIT", new BigDecimal("10.00"), "BRL", status, 123L
        );
    }

    private TransactionEventDTO.AccountDTO validAccountDto() {
        return new TransactionEventDTO.AccountDTO(
                ACCOUNT_ID, OWNER_ID, 0L, "ENABLED",
                new TransactionEventDTO.BalanceDTO(new BigDecimal("10.00"), "BRL")
        );
    }
}