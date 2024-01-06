package org.bidribidi.currency.dao;

import org.bidribidi.currency.model.Currency;
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
    private final static String SELECT_RATE_BY_IDS_STATEMENT = "select id, base_currency_id, target_currency_id, rate from exchange_rate where base_currency_id = ? and target_currency_id = ?";
    private final static String INSERT_RATE_STATEMENT = "insert into exchange_rate (base_currency_id, target_currency_id, rate) values (?, ?, ?)";
    private final static String UPDATE_RATE_BY_ID_STATEMENT = "update exchange_rate set rate = ? where id = ?";
    private final static String DELETE_RATE_BY_ID_STATEMENT = "delete from exchange_rate where id = ?";
    private final static String SELECT_BY_LINKED_CODES = """
        select 0 as id, er1.base_currency_id, er2.target_currency_id, er1.rate * er2.rate as rate
        from exchange_rate er1
        inner join exchange_rate er2
        on er1.target_currency_id = er2.base_currency_id
        where er1.base_currency_id = ?
            and er2.target_currency_id = ?
    """;

    private final Connection connection;
    private CurrencyDao currencyDao;

    public ExchangeRateDao(Connection connection, CurrencyDao currencyDao) {
        this.connection = connection;
        this.currencyDao = currencyDao;
    }

    public ExchangeRate addExchangeRate(Currency baseCurrency, Currency targetCurrency, double rate) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_RATE_STATEMENT)){
            ps.setInt(1, baseCurrency.getId());
            ps.setInt(2, targetCurrency.getId());
            ps.setDouble(3, rate);
            int affectedRows = ps.executeUpdate();
        } catch (SQLException e) {
            throw e;
        }
        return new ExchangeRate(baseCurrency, targetCurrency, rate);
    }
    public List<ExchangeRate> getAllExchangeRates() throws SQLException {
        List<ExchangeRate> exchangeRates = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_STATEMENT)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                exchangeRates.add(new ExchangeRate(
                        rs.getInt("id"),
                        currencyDao.getCurrencyById(rs.getInt("base_currency_id")),
                        currencyDao.getCurrencyById(rs.getInt("target_currency_id")),
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
                        currencyDao.getCurrencyById(rs.getInt("base_currency_id")),
                        currencyDao.getCurrencyById(rs.getInt("target_currency_id")),
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

    public ExchangeRate updateExchangeRate(ExchangeRate exchangeRate) throws SQLException {
        try(PreparedStatement ps = connection.prepareStatement(UPDATE_RATE_BY_ID_STATEMENT)) {
            ps.setDouble(1, exchangeRate.getRate());
            ps.setInt(2, exchangeRate.getId());
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new NoSuchElementException("ExchangeRate with code not found");
            }

        } catch (SQLException e) {
            throw e;
        }

        return exchangeRate;
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

    public ExchangeRate getExchangeRateByCodes(String codes) {
        String baseCode = codes.substring(0, 3);
        String targetCode = codes.substring(3);
        Currency baseCurrency = currencyDao.getCurrencyByCode(baseCode);
        Currency targetCurrency = currencyDao.getCurrencyByCode(targetCode);

        try(PreparedStatement ps = connection.prepareStatement(SELECT_RATE_BY_IDS_STATEMENT)) {
            ps.setInt(1, baseCurrency.getId());
            ps.setInt(2, targetCurrency.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new ExchangeRate(
                        rs.getInt("id"),
                        currencyDao.getCurrencyById(rs.getInt("base_currency_id")),
                        currencyDao.getCurrencyById(rs.getInt("target_currency_id")),
                        rs.getDouble("rate")
                );
            } else {
                throw new NoSuchElementException("ExchangeRate with codes " + codes + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("ExchangeRate with codes " + codes + " not found");
        }
    }

    public ExchangeRate getExchangeRateByLinkedCurrencies(Currency from, Currency to) {
        try(PreparedStatement ps = connection.prepareStatement(SELECT_BY_LINKED_CODES)) {
            ps.setInt(1, from.getId());
            ps.setInt(2, to.getId());
            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                return new ExchangeRate(
                    rs.getInt("id"),
                    currencyDao.getCurrencyById(rs.getInt("base_currency_id")),
                    currencyDao.getCurrencyById(rs.getInt("target_currency_id")),
                    rs.getDouble("rate")
                );
            } else {
                throw new NoSuchElementException("ExchangeRate with codes " + from.getCode() + " and " + to.getCode() + " not found");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("ExchangeRate with codes " + from.getCode() + " and " + to.getCode() + " not found");
        }
    }
}
