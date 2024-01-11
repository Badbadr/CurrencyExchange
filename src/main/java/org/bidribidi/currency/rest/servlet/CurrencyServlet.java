package org.bidribidi.currency.rest.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bidribidi.currency.config.DatabaseConfig;
import org.bidribidi.currency.dao.CurrencyDao;
import org.bidribidi.currency.dto.CurrencyRequest;
import org.bidribidi.currency.service.CurrencyService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import static org.bidribidi.currency.rest.servlet.Utils.sendError;

@WebServlet(urlPatterns = {"/currency/*", "/currencies", "/currencies/*"})
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
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            }
        } else {
            // implement on frontend
            try{
                String code = req.getPathInfo().split("/")[1];

                if (code != null) {
                    resp.getWriter().write(new JSONObject(currencyService.getCurrencyByCode(code)).toString());
                }
            } catch (SQLException e) {
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // implement on frontend
        super.doPatch(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if ("/currencies".equals(req.getServletPath())) {
            CurrencyRequest currencyRequest = new CurrencyRequest(
                req.getParameter("code"),
                req.getParameter("name"),
                req.getParameter("sign")
            );

            try {
                resp.getWriter().write(new JSONObject(currencyService.addCurrency(currencyRequest)).toString());
            } catch (SQLException e) {
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            } catch (IllegalArgumentException e2) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e2.getMessage());

            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> queryParams = Utils.getQueryParams(req);
        String payload = Utils.getBody(req);
        JSONObject jsonObject = new JSONObject(payload);
        String id = queryParams.get("id");

        CurrencyRequest currencyRequest = new CurrencyRequest(
            (String) jsonObject.get("code"),
            (String) jsonObject.get("fullname"),
            (String) jsonObject.get("sign")
        );

        if (id == null){
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "id cannot be null");
        } else {
            try {
                resp.getWriter().write(new JSONObject(currencyService.updateCurrency(Integer.parseInt(id), currencyRequest)).toString());
            } catch (SQLException e) {
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            } catch (IllegalArgumentException e2) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e2.getMessage());
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = req.getPathInfo().split("/")[1];
        if (code == null){
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "code cannot be null");
        } else {
            int deletedId = currencyService.deleteCurrencyByCode(code);
            resp.getWriter().write(new JSONObject("{\"id\":" + deletedId + "}").toString());
        }

    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!req.getMethod().equals("PATCH") && !req.getMethod().equals("OPTIONS")) {
            super.service(req, res);
        }
        if (req.getMethod().equals("PATCH")) {
            this.doPatch(req, res);
        }
    }
}