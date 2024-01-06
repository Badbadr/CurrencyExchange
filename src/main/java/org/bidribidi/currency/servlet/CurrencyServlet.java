package org.bidribidi.currency.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bidribidi.currency.config.DatabaseConfig;
import org.bidribidi.currency.dao.CurrencyDao;
import org.bidribidi.currency.service.CurrencyService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sqlite.util.StringUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/currency/*", "/currencies"})
public class CurrencyServlet extends HttpServlet {
    private CurrencyService currencyService;

    @Override
    public void init() throws ServletException {
        super.init();
        DatabaseConfig databaseConfig = new DatabaseConfig();
        this.currencyService = new CurrencyService(new CurrencyDao(databaseConfig.getConnection()));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/currencies".equals(req.getServletPath())) {
            try {
                resp.getWriter().write(new JSONArray(currencyService.getAllCurrencies()).toString());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            try{
                String code = req.getPathInfo().split("/")[1];

                if (code != null) {
                    resp.getWriter().write(new JSONObject(currencyService.getCurrencyByCode(code)).toString());
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPatch(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if ("/currencies".equals(req.getServletPath())) {
            String code = req.getParameter("code");
            String fullname = req.getParameter("fullname");
            String sign = req.getParameter("sign");

            try {
                resp.getWriter().write(new JSONObject(currencyService.addCurrency(code, fullname, sign))
                        .toString());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
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
        resp.getWriter().write(new JSONObject(currencyService.updateCurrency(
                Integer.parseInt(id), code, fullname, sign)).toString());
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> queryParams = Utils.getQueryParams(req);
        String id = queryParams.get("id");
        if (id == null){
            throw new IllegalArgumentException("id cannot be null");
        }
        int deletedId = currencyService.deleteCurrency(Integer.parseInt(id));
        resp.getWriter().write(new JSONObject("{\"id\":" + deletedId + "}").toString());

    }
}