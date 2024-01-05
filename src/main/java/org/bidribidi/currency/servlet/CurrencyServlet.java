package org.bidribidi.currency.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bidribidi.currency.config.DatabaseConfig;
import org.bidribidi.currency.dao.CurrencyDao;
import org.sqlite.util.StringUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {
    private CurrencyDao currencyDao;

    @Override
    public void init() throws ServletException {
        super.init();
        DatabaseConfig databaseConfig = new DatabaseConfig();
        this.currencyDao = new CurrencyDao(databaseConfig.getConnection());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String queryParams = req.getQueryString();
        if (queryParams == null) {
            try {
                resp.getWriter().write(currencyDao.getAllCurrencies().toString());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        try{
            String attribute = queryParams.split("=")[0];
            String value = queryParams.split("=")[1];

            switch (attribute) {
                case "id" -> resp.getWriter().write(currencyDao.getCurrencyById(Integer.parseInt(value)).toString());
                case "code" -> throw new IllegalArgumentException("Code not implemented yet");
                case "fullname" -> throw new IllegalArgumentException("fullname not implemented yet");
                default -> throw new IllegalArgumentException("Invalid attribute: " + attribute);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPatch(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> queryParam = Arrays.stream(req.getQueryString().split("&"))
                .collect(Collectors.toMap(s -> s.split("=")[0], s -> s.split("=")[1]));

        try {
            resp.getWriter().write(currencyDao.addCurrency(
                queryParam.get("code"), queryParam.get("fullname"), queryParam.get("sign")).toString()
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> queryParam = Arrays.stream(req.getQueryString().split("&"))
                .collect(Collectors.toMap(s -> s.split("=")[0], s -> s.split("=")[1]));

        resp.getWriter().write(currencyDao.updateCurrency(Integer.parseInt(queryParam.get("id")),
                queryParam.get("code"), queryParam.get("fullname"), queryParam.get("sign")).toString()
        );
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doDelete(req, resp);
    }
}