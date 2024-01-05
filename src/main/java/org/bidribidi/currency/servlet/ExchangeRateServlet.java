package org.bidribidi.currency.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bidribidi.currency.config.DatabaseConfig;
import org.bidribidi.currency.dao.ExchangeRateDao;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {

    private ExchangeRateDao exchangeRateDao;

    @Override
    public void init() throws ServletException {
        super.init();
        DatabaseConfig databaseConfig = new DatabaseConfig();
        this.exchangeRateDao = new ExchangeRateDao(databaseConfig.getConnection());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String queryParams = req.getQueryString();
        if (queryParams == null) {
            try {
                resp.getWriter().write(new JSONArray(exchangeRateDao.getAllExchangeRates()).toString());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        Map<String, String> queryParamsMap = Utils.getQueryParams(req);
        String id = queryParamsMap.get("id");
        String baseCurrencyId = queryParamsMap.get("baseCurrencyId");
        String targetCurrencyId = queryParamsMap.get("targetCurrencyId");
        String rate = queryParamsMap.get("rate");

        if (queryParamsMap.size() > 1) {
            throw new IllegalArgumentException("Too many query parameters");
        }

        if (id != null) {
            try {
                resp.getWriter().write(new JSONObject(exchangeRateDao.getExchangeRateById(Integer.parseInt(id))).toString());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        } else if (baseCurrencyId != null) {
            throw new IllegalArgumentException("get by baseCurrencyId not implemented yet");
        } else if (targetCurrencyId != null) {
            throw new IllegalArgumentException("get by targetCurrencyId not implemented yet");
        } else if (rate != null) {
            throw new IllegalArgumentException("get by rate not implemented yet");
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPatch(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String payload = Utils.getBody(req);
        JSONObject jsonObject = new JSONObject(payload);
        Integer baseCurrencyId = (Integer) jsonObject.get("baseCurrencyId");
        Integer targetCurrencyId = (Integer) jsonObject.get("targetCurrencyId");
        double rate = ((BigDecimal) jsonObject.get("rate")).doubleValue();

        try {
            resp.getWriter().write(new JSONObject(exchangeRateDao.addExchangeRate(
                    baseCurrencyId, targetCurrencyId, rate)).toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> queryParamsMap = Utils.getQueryParams(req);
        String id = queryParamsMap.get("id");

        if (queryParamsMap.size() > 1) {
            throw new IllegalArgumentException("Too many query parameters");
        }
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
        String payload = Utils.getBody(req);
        JSONObject jsonObject = new JSONObject(payload);
        Integer baseCurrencyId = (Integer) jsonObject.get("baseCurrencyId");
        Integer targetCurrencyId = (Integer) jsonObject.get("targetCurrencyId");
        double rate = ((BigDecimal) jsonObject.get("rate")).doubleValue();

        try {
            resp.getWriter().write(new JSONObject(exchangeRateDao.updateExchangeRateById(
                    Integer.parseInt(id), baseCurrencyId, targetCurrencyId, rate)).toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> queryParamsMap = Utils.getQueryParams(req);
        String id = queryParamsMap.get("id");

        if (queryParamsMap.size() > 1) {
            throw new IllegalArgumentException("Too many query parameters");
        }
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }

        try {
            int deletedId = exchangeRateDao.deleteExchangeRateById(Integer.parseInt(id));
            resp.getWriter().write(new JSONObject("{\"id\":" + deletedId + "}").toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
