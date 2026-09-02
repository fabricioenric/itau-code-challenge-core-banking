package br.com.itau.challenge.hello.adapter.input.web.dto;

import br.com.itau.challenge.hello.domain.model.AccountBalance;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public record BalanceResponse(
    UUID id,
    UUID owner,
    BalanceDto balance,
    OffsetDateTime updatedAt
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