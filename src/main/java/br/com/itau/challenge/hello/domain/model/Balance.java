package br.com.itau.challenge.hello.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class Balance {

    private final BigDecimal amount;
    private final String currency;

    public Balance(BigDecimal amount, String currency) {
        if (amount == null) {
            throw new IllegalArgumentException("amount não pode ser nulo");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency não pode ser vazio");
        }
        this.amount = amount;
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Balance other)) return false;
        return amount.compareTo(other.amount) == 0 && currency.equals(other.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}