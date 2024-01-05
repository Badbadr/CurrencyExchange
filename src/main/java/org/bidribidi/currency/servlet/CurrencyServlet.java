package org.bidribidi.currency.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bidribidi.currency.config.DatabaseConfig;
import org.bidribidi.currency.dao.CurrencyDao;
import org.json.JSONArray;
import org.json.JSONObject;
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
                resp.getWriter().write(new JSONArray(currencyDao.getAllCurrencies()).toString());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        try{
            Map<String, String> queryParamsMap = Utils.getQueryParams(req);
            String id = queryParamsMap.get("id");
            String code = queryParamsMap.get("code");
            String fullname = queryParamsMap.get("fullname");
            String sign = queryParamsMap.get("sign");

            if (queryParamsMap.size() > 1) {
                throw new IllegalArgumentException("Too many query parameters");
            }

            if (id != null) {
                resp.getWriter().write(new JSONObject(currencyDao.getCurrencyById(Integer.parseInt(id))).toString());
            } else if (code != null) {
                throw new IllegalArgumentException("get by code not implemented yet");
            } else if (fullname != null) {
                throw new IllegalArgumentException("get by fullname not implemented yet");
            } else if (sign != null) {
                throw new IllegalArgumentException("get by sign not implemented yet");
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String payload = Utils.getBody(req);
        JSONObject jsonObject = new JSONObject(payload);
        String code = (String) jsonObject.get("code");
        String fullname = (String) jsonObject.get("fullname");
        String sign = (String) jsonObject.get("sign");

        try {
            resp.getWriter().write(new JSONObject(currencyDao.addCurrency(code, fullname, sign))
                    .toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> queryParams = Utils.getQueryParams(req);
        String payload = Utils.getBody(req);
        JSONObject jsonObject = new JSONObject(payload);
        String id = queryParams.get("id");
        String code = (String) jsonObject.get("code");
        String fullname = (String) jsonObject.get("fullname");
        String sign = (String) jsonObject.get("sign");

        if (id == null){
            throw new IllegalArgumentException("id cannot be null");
        }
        resp.getWriter().write(new JSONObject(currencyDao.updateCurrency(Integer.parseInt(id), code, fullname, sign))
                .toString());
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> queryParams = Utils.getQueryParams(req);
        String id = queryParams.get("id");
        if (id == null){
            throw new IllegalArgumentException("id cannot be null");
        }
        int deletedId = currencyDao.deleteCurrency(Integer.parseInt(id));
        resp.getWriter().write(new JSONObject("{\"id\":" + deletedId + "}").toString());

    }
}