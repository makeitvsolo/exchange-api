package com.makeitvsolo.exchangeapi.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.UUID;

@DisplayName("ReverseExchange")
public class ReverseExchangeTests {
    private Currency usd = Currency.from("USD", "United States Dollar", "$");
    private Currency cad = Currency.from("CAD", "Canadian Dollar", "C$");

    @Test
    @DisplayName("converts amount by reversed rate")
    public void convertsAmountByReversedRate() {
        var amount = BigDecimal.TEN;
        var rate = BigDecimal.valueOf(1.5);
        var base = Exchange.from(usd, cad, rate);

        var reversed = Exchange.Reversed.of(base);

        Assertions.assertEquals(
                amount.multiply(BigDecimal.ONE.divide(rate, MathContext.DECIMAL64)), reversed.convert(amount)
        );
    }
}
