package br.com.itau.challenge.hello.adapter.input.web;

import br.com.itau.challenge.hello.domain.exception.AccountBalanceNotFoundException;
import br.com.itau.challenge.hello.domain.model.AccountBalance;
import br.com.itau.challenge.hello.domain.model.Balance;
import br.com.itau.challenge.hello.port.input.GetBalanceUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BalanceController.class)
class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private GetBalanceUseCase getBalanceUseCase;

    private static final UUID ACCOUNT_ID = UUID.fromString("5b19c8b6-0cc4-4c72-a989-0c2ee15fa975");
    private static final UUID OWNER_ID = UUID.fromString("315e3cfe-f4af-4cd2-b298-a449e614349a");

    @Test
    void deveRetornar200ComSaldoQuandoContaExistir() throws Exception {
        AccountBalance accountBalance = new AccountBalance(
                ACCOUNT_ID, OWNER_ID,
                new Balance(new BigDecimal("183.12"), "BRL"),
                1751641364589998L,
                UUID.randomUUID()
        );

        when(getBalanceUseCase.execute(ACCOUNT_ID)).thenReturn(accountBalance);

        mockMvc.perform(get("/balances/{accountId}", ACCOUNT_ID)
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.id").value(ACCOUNT_ID.toString()))
                .andExpect(jsonPath("$.owner").value(OWNER_ID.toString()))
                .andExpect(jsonPath("$.balance.amount").value(183.12))
                .andExpect(jsonPath("$.balance.currency").value("BRL"))
                .andExpect(jsonPath("$.updated_at").exists())
                .andExpect(jsonPath("$.updatedAt").doesNotExist());
    }

    @Test
    void deveRetornar404QuandoContaNaoExistir() throws Exception {
        when(getBalanceUseCase.execute(ACCOUNT_ID))
                .thenThrow(new AccountBalanceNotFoundException(ACCOUNT_ID));

        mockMvc.perform(get("/balances/{accountId}", ACCOUNT_ID)
                        .accept("application/json"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACCOUNT_BALANCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deveRetornar400QuandoAccountIdForUuidInvalido() throws Exception {
        mockMvc.perform(get("/balances/{accountId}", "isso-nao-e-um-uuid")
                        .accept("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ACCOUNT_ID"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deveRetornar500QuandoUseCaseLancarErroInesperado() throws Exception {
        when(getBalanceUseCase.execute(ACCOUNT_ID))
                .thenThrow(new RuntimeException("DynamoDB indisponível"));

        mockMvc.perform(get("/balances/{accountId}", ACCOUNT_ID)
                        .accept("application/json"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deveRetornarOwnerNuloQuandoContaNaoTiverOwner() throws Exception {
        AccountBalance accountBalance = new AccountBalance(
                ACCOUNT_ID, null,
                new Balance(new BigDecimal("50.00"), "BRL"),
                123L,
                UUID.randomUUID()
        );

        when(getBalanceUseCase.execute(ACCOUNT_ID)).thenReturn(accountBalance);

        mockMvc.perform(get("/balances/{accountId}", ACCOUNT_ID)
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner").doesNotExist());
    }
}