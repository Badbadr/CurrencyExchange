package org.bidribidi.currency.rest.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bidribidi.currency.config.DatabaseConfig;
import org.bidribidi.currency.dao.CurrencyDao;
import org.bidribidi.currency.dao.ExchangeRateDao;
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

    private final ExchangeRateService exchangeRateService = new ExchangeRateService();

    public ExchangeServlet() throws SQLException {
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> queryParamsMap = Utils.getQueryParams(req);

        if (queryParamsMap.size() != 3) {
            Utils.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Must be exactly 3 query parameters: from, to, amount");
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
            Utils.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e3.getMessage());
        } catch (NumberFormatException e4) {
            Utils.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "amount must be a number");
        }
    }
}
