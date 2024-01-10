package org.bidribidi.currency.service;

import org.bidribidi.currency.dao.CurrencyDao;
import org.bidribidi.currency.dto.CurrencyRequest;
import org.bidribidi.currency.model.Currency;

import java.sql.SQLException;
import java.util.List;

import static org.bidribidi.currency.service.validators.CurrencyValidator.isValidCode;
import static org.bidribidi.currency.service.validators.CurrencyValidator.validate;

public class CurrencyService {

    private final CurrencyDao currencyDao;

    public CurrencyService(CurrencyDao currencyDao) {
        this.currencyDao = currencyDao;
    }
    public Currency getCurrencyById(int id) throws SQLException {
        return currencyDao.getCurrencyById(id);
    }

    public Currency getCurrencyByCode(String code) throws SQLException {
        isValidCode(code);
        return currencyDao.getCurrencyByCode(code);
    }

    public List<Currency> getAllCurrencies() throws SQLException {
        return currencyDao.getAllCurrencies();
    }

    public Currency addCurrency(CurrencyRequest currencyRequest) throws SQLException, IllegalArgumentException{
        validate(currencyRequest);
        return currencyDao.addCurrency(currencyRequest);
    }

    public Currency updateCurrency(int id, CurrencyRequest currencyRequest) throws SQLException, IllegalArgumentException {
        validate(currencyRequest);
        return currencyDao.updateCurrency(id, currencyRequest);
    }

    public int deleteCurrency(int id) {
        return currencyDao.deleteCurrency(id);
    }

    public int deleteCurrencyByCode(String code) {
        isValidCode(code);
        return currencyDao.deleteCurrencyByCode(code);
    }
}
