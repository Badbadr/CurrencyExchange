package org.bidribidi.currency.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bidribidi.currency.config.DatabaseConfig;
import org.bidribidi.currency.dao.CurrencyDao;
import org.bidribidi.currency.dao.ExchangeRateDao;
import org.bidribidi.currency.model.ExchangeRate;
import org.bidribidi.currency.service.CurrencyService;
import org.bidribidi.currency.service.ExchangeRateService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet(urlPatterns = {"/exchangeRate/*", "/exchangeRates"})
public class ExchangeRateServlet extends HttpServlet {

    private ExchangeRateService exchangeRateService;

    @Override
    public void init() throws ServletException {
        super.init();
        DatabaseConfig databaseConfig = new DatabaseConfig();
        CurrencyDao currencyDao = new CurrencyDao(databaseConfig.getConnection());
        ExchangeRateDao exchangeRateDao = new ExchangeRateDao(databaseConfig.getConnection(), currencyDao);
        this.exchangeRateService = new ExchangeRateService(exchangeRateDao,
                new CurrencyService(currencyDao));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/exchangeRates".equals(req.getServletPath())) {
            try {
                resp.getWriter().write(new JSONArray(exchangeRateService.getAllExchangeRates()).toString());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            String queryParams = req.getQueryString();
            if (queryParams == null) {
                throw new IllegalArgumentException("queryParams are required");
            }
            Map<String, String> queryParamsMap = Utils.getQueryParams(req);

            if (queryParamsMap.size() > 1) {
                throw new IllegalArgumentException("Too many query parameters");
            }

            if (queryParamsMap.size() == 0) {
                String codes = req.getPathInfo().split("/")[1];
                if (codes == null) {
                    throw new IllegalArgumentException("codes are required");
                }
                try {
                    resp.getWriter().write(new JSONObject(exchangeRateService.getExchangeRateByCodes(codes)).toString());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                String id = queryParamsMap.get("id");
                String baseCurrencyId = queryParamsMap.get("baseCurrencyId");
                String targetCurrencyId = queryParamsMap.get("targetCurrencyId");
                String rate = queryParamsMap.get("rate");

                if (id != null) {
                    try {
                        resp.getWriter().write(new JSONObject(exchangeRateService.getExchangeRateById(Integer.parseInt(id)))
                                .toString());
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
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String codes = req.getPathInfo().split("/")[1];
        if (codes == null) {
            throw new IllegalArgumentException("codes is required");
        }
        try {
            ExchangeRate exchangeRate = exchangeRateService.getExchangeRateByCodes(codes);
            String payload = Utils.getBody(req);
            String key = payload.split("=")[0];
            if (!key.equals("rate")) {
                throw new IllegalArgumentException("key must be rate");
            }
            double value = Double.parseDouble(payload.split("=")[1]);
            exchangeRate.setRate(value);
            resp.getWriter().write(new JSONObject(exchangeRateService.updateExchangeRate(exchangeRate)).toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        double rate = Double.parseDouble(req.getParameter("rate"));

        try {
            resp.getWriter().write(new JSONObject(exchangeRateService.addExchangeRate(
                    baseCurrencyCode, targetCurrencyCode, rate)).toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> queryParamsMap = Utils.getQueryParams(req);
        if (queryParamsMap.size() > 1) {
            throw new IllegalArgumentException("Too many query parameters");
        }

        String id = queryParamsMap.get("id");
        String codes = queryParamsMap.get("codes");


        try {
            if (id != null) {
                int deletedId = exchangeRateService.deleteExchangeRateById(Integer.parseInt(id));
                resp.getWriter().write(new JSONObject("{\"id\":" + deletedId + "}").toString());
            }
            if (codes != null) {
                int deletedId = exchangeRateService.deleteExchangeRateByCodes(codes);
                resp.getWriter().write(new JSONObject("{\"id\":" + deletedId + "}").toString());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();
        if (!method.equals("PATCH")) {
            super.service(req, resp);
        }

        this.doPatch(req, resp);
    }
}
