package org.bidribidi.currency.service;

import org.bidribidi.currency.dao.ExchangeRateDao;
import org.bidribidi.currency.model.ExchangeRate;

import java.sql.SQLException;
import java.util.List;

public class ExchangeRateService {

    private ExchangeRateDao exchangeRateDao;

    public ExchangeRateService(ExchangeRateDao exchangeRateDao) {
        this.exchangeRateDao = exchangeRateDao;
    }

    public ExchangeRate getExchangeRateById(int id) throws SQLException {
        return exchangeRateDao.getExchangeRateById(id);
    }

    public List<ExchangeRate> getAllExchangeRates() throws SQLException {
        return exchangeRateDao.getAllExchangeRates();
    }

    public ExchangeRate addExchangeRate(int baseCurrencyId, int targetCurrencyId, double rate) throws SQLException {
        return exchangeRateDao.addCurrency(baseCurrencyId, targetCurrencyId, rate);
    }

    public ExchangeRate updateExchangeRate(int id, int baseCurrencyId, int targetCurrencyId, double rate) throws SQLException {
        return exchangeRateDao.updateExchangeRateById(id, baseCurrencyId, targetCurrencyId, rate);
    }

    public int deleteExchangeRate(int id) throws SQLException {
        return exchangeRateDao.deleteExchangeRateById(id);
    }
}
