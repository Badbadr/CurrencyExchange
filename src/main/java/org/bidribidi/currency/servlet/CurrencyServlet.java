package org.bidribidi.currency.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bidribidi.currency.config.DatabaseConfig;
import org.bidribidi.currency.dao.CurrencyDao;
import org.bidribidi.currency.dto.CurrencyRequest;
import org.bidribidi.currency.dto.ErrorResponse;
import org.bidribidi.currency.service.CurrencyService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

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
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.sendError(500, ex.getMessage());
            }
        } else {
            try{
                String code = req.getPathInfo().split("/")[1];

                if (code != null) {
                    resp.getWriter().write(new JSONObject(currencyService.getCurrencyByCode(code)).toString());
                }
            } catch (SQLException e) {
                resp.sendError(500, e.getMessage());
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
            CurrencyRequest currencyRequest = new CurrencyRequest(
                req.getParameter("code"),
                req.getParameter("name"),
                req.getParameter("sign")
            );

            try {
                resp.getWriter().write(new JSONObject(currencyService.addCurrency(currencyRequest))
                        .toString());
            } catch (SQLException e) {
                resp.sendError(500, e.getMessage());
            } catch (IllegalArgumentException e2) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(new JSONObject(
                        new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e2.getMessage())
                ).toString());

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
            resp.sendError(500, "id cannot be null");
        } else {
            try {
                resp.getWriter().write(new JSONObject(currencyService.updateCurrency(
                    Integer.parseInt(id), currencyRequest)).toString());
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            } catch (IllegalArgumentException e2) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(new JSONObject(
                        new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e2.getMessage())
                ).toString());
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = req.getPathInfo().split("/")[1];
        if (code == null){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "code cannot be null");
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

        res.setHeader("Access-Control-Allow-Origin", "http://localhost");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type");
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
        res.setHeader("Content-Type", "application/json");
        res.setHeader("Allow","GET, POST, PUT, DELETE, PATCH, OPTIONS");
    }
}