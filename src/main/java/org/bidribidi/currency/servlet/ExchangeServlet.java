package org.bidribidi.currency.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bidribidi.currency.config.DatabaseConfig;
import org.bidribidi.currency.dao.CurrencyDao;
import org.bidribidi.currency.dao.ExchangeRateDao;
import org.bidribidi.currency.dto.ErrorResponse;
import org.bidribidi.currency.model.Exchange;
import org.bidribidi.currency.model.ExchangeRate;
import org.bidribidi.currency.service.CurrencyService;
import org.bidribidi.currency.service.ExchangeRateService;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {

    private ExchangeRateService exchangeRateService;

    @Override
    public void init() throws ServletException {
        super.init();
        DatabaseConfig databaseConfig = new DatabaseConfig();
        CurrencyDao currencyDao = new CurrencyDao(databaseConfig.getConnection());
        ExchangeRateDao exchangeRateDao = new ExchangeRateDao(databaseConfig.getConnection(), currencyDao);
        this.exchangeRateService = new ExchangeRateService(exchangeRateDao, new CurrencyService(currencyDao));
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> queryParamsMap = Utils.getQueryParams(req);

        if (queryParamsMap.size() != 3) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(new JSONObject(
                    new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Must be exactly 3 query parameters: from, to, amount")
            ).toString());
        }

        try {
            String fromCode = queryParamsMap.get("from");
            String toCode = queryParamsMap.get("to");
            double amount = Double.parseDouble(queryParamsMap.get("amount"));

            ExchangeRate exchangeRate;
            Exchange exchange = new Exchange();

            exchangeRate = exchangeRateService.getExchangeRateByLinkedCodes(fromCode, toCode);
            exchange.setBaseCurrency(exchangeRate.getBaseCurrency());
            exchange.setTargetCurrency(exchangeRate.getTargetCurrency());
            exchange.setAmount(amount);
            exchange.setRate(exchangeRate.getRate());
            exchange.setConvertedAmount(exchangeRate.getRate() * amount);
            resp.getWriter().write(new JSONObject(exchange).toString());
        } catch (SQLException | IOException e3) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(new JSONObject(
                    new ErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e3.getMessage())
            ).toString());
        } catch (NumberFormatException e4) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(new JSONObject(
                    new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "amount must be a number")
            ).toString());
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doOptions(req, resp);
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        super.service(req, res);
        res.addHeader("Access-Control-Allow-Origin", "http://localhost");
        res.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        res.addHeader("Access-Control-Allow-Headers", "Content-Type");
        res.addHeader("Content-Type", "application/json");
    }
}
