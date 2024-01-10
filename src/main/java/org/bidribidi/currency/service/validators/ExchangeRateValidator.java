package org.bidribidi.currency.service.validators;

import org.bidribidi.currency.dto.ExchangeRateRequest;
import org.bidribidi.currency.model.ExchangeRate;

import static org.bidribidi.currency.service.validators.CurrencyValidator.isValidCode;

public class ExchangeRateValidator {

    public static void validate(ExchangeRateRequest exchangeRateRequest) throws IllegalArgumentException {
        isValidCode(exchangeRateRequest.baseCurrencyCode());
        isValidCode(exchangeRateRequest.targetCurrencyCode());
    }

    public static void validate(ExchangeRate exchangeRate) throws IllegalArgumentException {
        CurrencyValidator.validate(exchangeRate.getBaseCurrency());
        CurrencyValidator.validate(exchangeRate.getTargetCurrency());
    }
}
