package org.bidribidi.currency.dao;

import org.bidribidi.currency.config.DatabaseConfig;
import org.bidribidi.currency.dto.CurrencyRequest;
import org.bidribidi.currency.model.Currency;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class CurrencyDao {

    private final static String SELECT_ALL_STATEMENT = "select * from currencies";
    private final static String SELECT_CURRENCY_BY_ID_STATEMENT = "select id, code, fullname, sign from currencies where id = ?";
    private final static String SELECT_CURRENCY_BY_CODE_STATEMENT = "select id, code, fullname, sign from currencies where code = ?";
    private final static String INSERT_CURRENCY_STATEMENT = "insert into currencies (code, fullname, sign) values (?, ?, ?)";
    private final static String UPDATE_CURRENCY_BY_ID_STATEMENT = "update currencies set code = ?, fullname = ?, sign = ? where id = ?";
    private final static String DELETE_CURRENCY_BY_CODE_STATEMENT = "delete from currencies where code = ?";
    private final static String DELETE_CURRENCY_BY_ID_STATEMENT = "delete from currencies where id = ?";
    private final static String ENABLE_FOREIGN_KEYS_STATEMENT = "PRAGMA foreign_keys = on;";
    private final Connection connection = DatabaseConfig.getConnection();

    public CurrencyDao() throws SQLException {
    }

    public Currency addCurrency(CurrencyRequest currencyRequest) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_CURRENCY_STATEMENT)) {
            ps.setString(1, currencyRequest.code());
            ps.setString(2, currencyRequest.fullname());
            ps.setString(3, currencyRequest.sign());
            int affectedRows = ps.executeUpdate();
        }

        return new Currency(currencyRequest);
    }

    public List<Currency> getAllCurrencies() throws SQLException {
        List<Currency> currencies = new ArrayList<>();
        try(PreparedStatement ps = connection.prepareStatement(SELECT_ALL_STATEMENT)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                currencies.add(new Currency(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("fullname"),
                        rs.getString("sign")
                ));
            }
        }

        return currencies;
    }

    public Currency getCurrencyById(int id) throws NoSuchElementException, SQLException {
        Currency currency;
        try(PreparedStatement ps = connection.prepareStatement(SELECT_CURRENCY_BY_ID_STATEMENT)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                currency = new Currency(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("fullname"),
                        rs.getString("sign")
                );
            } else {
                throw new NoSuchElementException("Currency with id " + id + " not found");
            }
        }

        return currency;
    }

    public Currency updateCurrency(int id, CurrencyRequest currencyRequest) throws NoSuchElementException, SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_CURRENCY_BY_ID_STATEMENT)) {
            ps.setString(1, currencyRequest.code());
            ps.setString(2, currencyRequest.fullname());
            ps.setString(3, currencyRequest.sign());
            ps.setInt(4, id);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new NoSuchElementException("Currency with id " + id + " not found");
            }
        }
        return new Currency(id, currencyRequest);
    }

    public int deleteCurrency(int id) throws NoSuchElementException, SQLException {
        enableForeignKeys();
        try(PreparedStatement ps = connection.prepareStatement(DELETE_CURRENCY_BY_ID_STATEMENT)) {
            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new NoSuchElementException("Currency with id " + id + " not found");
            }
        }
        return id;
    }

    public Currency getCurrencyByCode(String code) throws NoSuchElementException, SQLException {
        try(PreparedStatement ps = connection.prepareStatement(SELECT_CURRENCY_BY_CODE_STATEMENT)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Currency(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("fullname"),
                        rs.getString("sign")
                );
            } else {
                throw new NoSuchElementException("Currency with code " + code + " not found");
            }
        }
    }

    public int deleteCurrencyByCode(String code) throws NoSuchElementException, SQLException {
        enableForeignKeys();
        try(PreparedStatement ps = connection.prepareStatement(DELETE_CURRENCY_BY_CODE_STATEMENT)) {
            ps.setString(1, code);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new NoSuchElementException("Currency with code " + code + " not found");
            }
        }
        return 1;
    }

    private void enableForeignKeys() throws SQLException {
        try(PreparedStatement ps = connection.prepareStatement(ENABLE_FOREIGN_KEYS_STATEMENT)) {
            ps.executeUpdate();
        }
    }
}
