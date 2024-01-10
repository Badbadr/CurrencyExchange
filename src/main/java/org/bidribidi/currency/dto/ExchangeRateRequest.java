package org.bidribidi.currency.dto;

public record ExchangeRateRequest(
    String baseCurrencyCode,
    String targetCurrencyCode,
    double rate
) { }
