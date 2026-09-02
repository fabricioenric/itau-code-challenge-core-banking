package br.com.itau.challenge.corebanking.adapter.input.web.dto;

import br.com.itau.challenge.corebanking.domain.model.AccountBalance;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BalanceResponse(
    UUID id,
    UUID owner,
    BalanceDto balance,
    @JsonProperty("updated_at") OffsetDateTime updatedAt
) {

    public record BalanceDto(
        BigDecimal amount,
        String currency
    ) {}

    public static BalanceResponse from(AccountBalance accountBalance) {
        BalanceDto balanceDto = new BalanceDto(
                accountBalance.getBalance().getAmount(),
                accountBalance.getBalance().getCurrency()
        );

        OffsetDateTime updatedAt = accountBalance.getUpdatedAtInstant()
                .atOffset(ZoneOffset.UTC);

        return new BalanceResponse(
                accountBalance.getAccountId(),
                accountBalance.getOwner(),
                balanceDto,
                updatedAt
        );
    }
}