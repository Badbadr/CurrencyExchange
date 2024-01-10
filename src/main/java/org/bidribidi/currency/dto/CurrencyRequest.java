package org.bidribidi.currency.dto;

public record CurrencyRequest(
    String code,
    String fullname,
    String sign
) {
}
