package org.bidribidi.currency.service;

import org.bidribidi.currency.dao.CurrencyDao;
import org.bidribidi.currency.model.Currency;

import java.sql.SQLException;
import java.util.List;

public class CurrencyService {

    private CurrencyDao currencyDao;

    public CurrencyService(CurrencyDao currencyDao) {
        this.currencyDao = currencyDao;
    }
    public Currency getCurrencyById(int id) throws SQLException {
        return currencyDao.getCurrencyById(id);
    }

    public List<Currency> getAllCurrencies() throws SQLException {
        return currencyDao.getAllCurrencies();
    }

    public Currency addCurrency(String code, String fullname, String sign) throws SQLException {
        return currencyDao.addCurrency(code, fullname, sign);
    }

    public Currency updateCurrency(int id, String code, String fullname, String sign) {
        return currencyDao.updateCurrency(id, code, fullname, sign);
    }

    public int deleteCurrency(int id) {
        return currencyDao.deleteCurrency(id);
    }
}
