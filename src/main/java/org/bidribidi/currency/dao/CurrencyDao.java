package org.bidribidi.currency.dao;

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
    private final static String INSERT_CURRENCY_STATEMENT = "insert into currencies (code, fullname, sign) values (?, ?, ?)";
    private final static String UPDATE_CURRENCY_BY_ID_STATEMENT = "update currencies set code = ?, fullname = ?, sign = ? where id = ?";
    private final static String DELETE_CURRENCY_BY_ID_STATEMENT = "delete from currencies where id = ?";
    private final Connection connection;

    public CurrencyDao(Connection connection) {
        this.connection = connection;
    }

    public Currency addCurrency(String code, String fullname, String sign) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_CURRENCY_STATEMENT)){
            ps.setString(1, code);
            ps.setString(2, fullname);
            ps.setString(3, sign);
            int affectedRows = ps.executeUpdate();
        } catch (SQLException e) {
            throw e;
        }
        return new Currency(code, fullname, sign);
    }

    public List<Currency> getAllCurrencies() throws SQLException {
        List<Currency> currencies = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_STATEMENT)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                currencies.add(new Currency(
                    rs.getInt("id"),
                    rs.getString("code"),
                    rs.getString("fullname"),
                    rs.getString("sign")
                ));
            }
        } catch (SQLException e) {
            throw e;
        }
        return currencies;
    }

    public Currency getCurrencyById(int id) throws SQLException {
        Currency currency;
        try (PreparedStatement ps = connection.prepareStatement(SELECT_CURRENCY_BY_ID_STATEMENT)) {
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
        } catch (SQLException e) {
            throw e;
        }

        return currency;
    }

    public Currency updateCurrency(int id, String code, String fullname, String sign) {
        try(PreparedStatement ps = connection.prepareStatement(UPDATE_CURRENCY_BY_ID_STATEMENT)) {
            ps.setString(1, code);
            ps.setString(2, fullname);
            ps.setString(3, sign);
            ps.setInt(4, id);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new NoSuchElementException("Currency with id " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new Currency(id, code, fullname, sign);
    }

    public int deleteCurrency(int id) {
        try(PreparedStatement ps = connection.prepareStatement(DELETE_CURRENCY_BY_ID_STATEMENT)) {
            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new NoSuchElementException("Currency with id " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return id;
    }

}
