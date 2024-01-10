package org.bidribidi.currency.service;

import org.bidribidi.currency.dao.ExchangeRateDao;
import org.bidribidi.currency.dto.ExchangeRateRequest;
import org.bidribidi.currency.model.Currency;
import org.bidribidi.currency.model.ExchangeRate;
import org.bidribidi.currency.service.validators.CurrencyValidator;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

import static org.bidribidi.currency.service.validators.ExchangeRateValidator.validate;

public class ExchangeRateService {

    private final ExchangeRateDao exchangeRateDao;
    private final CurrencyService currencyService;

    public ExchangeRateService(ExchangeRateDao exchangeRateDao, CurrencyService currencyService) {
        this.exchangeRateDao = exchangeRateDao;
        this.currencyService = currencyService;
    }

    public ExchangeRate getExchangeRateById(int id) throws SQLException {
        return exchangeRateDao.getExchangeRateById(id);
    }

    public ExchangeRate getExchangeRateByCodes(String codes) throws SQLException {
        return exchangeRateDao.getExchangeRateByCodes(codes);
    }

    public List<ExchangeRate> getAllExchangeRates() throws SQLException {
        return exchangeRateDao.getAllExchangeRates();
    }

    public ExchangeRate addExchangeRate(ExchangeRateRequest exchangeRateRequest) throws SQLException {
        validate(exchangeRateRequest);
        Currency baseCurrency = currencyService.getCurrencyByCode(exchangeRateRequest.baseCurrencyCode());
        Currency targetCurrency = currencyService.getCurrencyByCode(exchangeRateRequest.targetCurrencyCode());
        return addExchangeRate(baseCurrency, targetCurrency, exchangeRateRequest.rate());
    }

    public ExchangeRate addExchangeRate(Currency baseCurrency, Currency targetCurrency, double rate) throws SQLException {;
        CurrencyValidator.validate(baseCurrency);
        CurrencyValidator.validate(targetCurrency);
        return exchangeRateDao.addExchangeRate(baseCurrency, targetCurrency, rate);
    }

    public ExchangeRate updateExchangeRate(ExchangeRate exchangeRate) throws SQLException {
        validate(exchangeRate);
        return exchangeRateDao.updateExchangeRate(exchangeRate);
    }

    public int deleteExchangeRateById(int id) throws SQLException {
        return exchangeRateDao.deleteExchangeRateById(id);
    }

    public int deleteExchangeRateByCodes(String codes) throws SQLException {
        ExchangeRate exchangeRateToDelete = exchangeRateDao.getExchangeRateByCodes(codes);
        return exchangeRateDao.deleteExchangeRateById(exchangeRateToDelete.getId());
    }

    public ExchangeRate getExchangeRateByLinkedCodes(String fromCode, String toCode) throws SQLException {
        ExchangeRate exchangeRate;

        try {
            exchangeRate = getExchangeRateByCodes(fromCode + toCode);
        } catch (NoSuchElementException e) {
            Currency fromCurrency = currencyService.getCurrencyByCode(fromCode);
            Currency toCurrency = currencyService.getCurrencyByCode(toCode);
            exchangeRate = exchangeRateDao.getExchangeRateByLinkedCurrencies(fromCurrency, toCurrency);
        }

        return exchangeRate;
    }
}
