package org.bidribidi.currency.dao;

import org.bidribidi.currency.model.ExchangeRate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ExchangeRateDao {

    private final static String SELECT_ALL_STATEMENT = "select * from exchange_rate";
    private final static String SELECT_RATE_BY_ID_STATEMENT = "select id, base_currency_id, target_currency_id, rate from exchange_rate where id = ?";
    private final static String INSERT_RATE_STATEMENT = "insert into exchange_rate (base_currency_id, target_currency_id, rate) values (?, ?, ?)";
    private final static String UPDATE_RATE_BY_ID_STATEMENT = "update exchange_rate set base_currency_id = ?, target_currency_id = ?, rate = ? where id = ?";
    private final static String DELETE_RATE_BY_ID_STATEMENT = "delete from exchange_rate where id = ?";
    private final Connection connection;

    public ExchangeRateDao(Connection connection) {
        this.connection = connection;
    }

    public ExchangeRate addCurrency(int baseCurrencyId, int targetCurrencyId, double rate) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_RATE_STATEMENT)){
            ps.setInt(1, baseCurrencyId);
            ps.setInt(2, targetCurrencyId);
            ps.setDouble(3, rate);
            int affectedRows = ps.executeUpdate();
        } catch (SQLException e) {
            throw e;
        }
        return new ExchangeRate(baseCurrencyId, targetCurrencyId, rate);
    }
    public List<ExchangeRate> getAllExchangeRates() throws SQLException {
        List<ExchangeRate> exchangeRates = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_STATEMENT)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                exchangeRates.add(new ExchangeRate(
                        rs.getInt("id"),
                        rs.getInt("base_currency_id"),
                        rs.getInt("target_currency_id"),
                        rs.getDouble("rate")
                ));
            }
        } catch (SQLException e) {
            throw e;
        }
        return exchangeRates;
    }

    public ExchangeRate getExchangeRateById(int id) throws SQLException {
        ExchangeRate exchangeRate;
        try (PreparedStatement ps = connection.prepareStatement(SELECT_RATE_BY_ID_STATEMENT)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                exchangeRate = new ExchangeRate(
                        rs.getInt("id"),
                        rs.getInt("base_currency_id"),
                        rs.getInt("target_currency_id"),
                        rs.getDouble("rate")
                );
            } else {
                throw new NoSuchElementException("ExchangeRate with id " + id + " not found");
            }
        } catch (SQLException e) {
            throw e;
        }

        return exchangeRate;
    }

    public ExchangeRate updateExchangeRateById(int id, int baseCurrencyId, int targetCurrencyId, double rate) throws SQLException {
        try(PreparedStatement ps = connection.prepareStatement(UPDATE_RATE_BY_ID_STATEMENT)) {
            ps.setInt(1, baseCurrencyId);
            ps.setInt(2, targetCurrencyId);
            ps.setDouble(3, rate);
            ps.setInt(4, id);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new NoSuchElementException("ExchangeRate with id " + id + " not found");
            }
        } catch (SQLException e) {
            throw e;
        }

        return new ExchangeRate(id, baseCurrencyId, targetCurrencyId, rate);
    }

    public int deleteExchangeRateById(int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_RATE_BY_ID_STATEMENT)){
            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new NoSuchElementException("ExchangeRate with id " + id + " not found");
            }
            return affectedRows;
        } catch (SQLException e) {
            throw e;
        }

    }
}
