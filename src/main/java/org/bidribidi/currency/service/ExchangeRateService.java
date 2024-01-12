package org.bidribidi.currency.service;

import org.bidribidi.currency.dao.ExchangeRateDao;
import org.bidribidi.currency.dto.ExchangeRateRequest;
import org.bidribidi.currency.model.Currency;
import org.bidribidi.currency.model.ExchangeRate;
import org.bidribidi.currency.service.validators.CurrencyValidator;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.bidribidi.currency.service.validators.ExchangeRateValidator.validate;

public class ExchangeRateService {

    private final ExchangeRateDao exchangeRateDao = new ExchangeRateDao();
    private final CurrencyService currencyService = new CurrencyService();

    public ExchangeRateService() throws SQLException {
    }

    public ExchangeRate getExchangeRateById(int id) throws SQLException {
        return exchangeRateDao.getExchangeRateById(id);
    }

    public ExchangeRate getExchangeRateByCodes(String codes) throws NoSuchElementException, SQLException {
        return exchangeRateDao.getExchangeRateByCodes(codes);
    }

    public List<ExchangeRate> getAllExchangeRates() throws SQLException {
        return exchangeRateDao.getAllExchangeRates();
    }

    public ExchangeRate addExchangeRate(ExchangeRateRequest exchangeRateRequest) throws SQLException {
        validate(exchangeRateRequest);
        Currency baseCurrency = currencyService.getCurrencyByCode(exchangeRateRequest.baseCurrencyCode());
        Currency targetCurrency = currencyService.getCurrencyByCode(exchangeRateRequest.targetCurrencyCode());
        return addExchangeRate(baseCurrency, targetCurrency, exchangeRateRequest.rate());
    }

    public ExchangeRate addExchangeRate(Currency baseCurrency, Currency targetCurrency, double rate) throws SQLException {
        CurrencyValidator.validate(baseCurrency);
        CurrencyValidator.validate(targetCurrency);
        return exchangeRateDao.addExchangeRate(baseCurrency, targetCurrency, rate);
    }

    public ExchangeRate updateExchangeRate(ExchangeRate exchangeRate) throws SQLException {
        validate(exchangeRate);
        return exchangeRateDao.updateExchangeRate(exchangeRate);
    }

    public int deleteExchangeRateById(int id) throws SQLException {
        return exchangeRateDao.deleteExchangeRateById(id);
    }

    public int deleteExchangeRateByCodes(String codes) throws SQLException {
        ExchangeRate exchangeRateToDelete = exchangeRateDao.getExchangeRateByCodes(codes);
        return exchangeRateDao.deleteExchangeRateById(exchangeRateToDelete.getId());
    }

    public ExchangeRate getExchangeRateByLinkedCodes(String fromCode, String toCode) throws SQLException {
        ExchangeRate exchangeRate;

        try {
            exchangeRate = getExchangeRateByCodes(fromCode + toCode);
            return exchangeRate;
        } catch (NoSuchElementException e) {
            System.out.println("No such exchange rate. Trying to get reversed exchange rate.");
        }

        try {
            exchangeRate = getExchangeRateByCodes(toCode + fromCode);
            exchangeRate.setRate(1.0 / exchangeRate.getRate());
            return exchangeRate;
        } catch (NoSuchElementException e) {
            System.out.println("No reversed exchange rate. Trying to get indirect exchange rate.");
        }

        exchangeRate = getIndirectExchangeRateByCodes(fromCode, toCode);
        return exchangeRate;
    }

    public ExchangeRate getIndirectExchangeRateByCodes(String fromCode, String toCode) throws NoSuchElementException, SQLException {
        // Getting all exchange rates
        List<ExchangeRate> allExchangeRates = getAllExchangeRates();
        // Getting reversed exchange rates
        List<ExchangeRate> reversedExchangeRates = allExchangeRates.stream().map((rate) ->
                new ExchangeRate(rate.getTargetCurrency(), rate.getBaseCurrency(), 1.0 / rate.getRate())).toList();

        allExchangeRates.addAll(reversedExchangeRates);

        // Creating map, where key is currency code and value is list of exchange rates for that currency
        Map<String, List<ExchangeRate>> currencyCodeToRatesMap = allExchangeRates.stream().collect(
                Collectors.groupingBy((rate) -> rate.getBaseCurrency().getCode())
        );

        // Searching for a way to from baseCurrency to targetCurrency (BFS) and getting all paths
        Map<String, String> childParentMap = bfs(fromCode, toCode, currencyCodeToRatesMap);

        // Getting last edges of each path
        List<Map.Entry<String, String>> pathsLastEdges = new ArrayList<>();
        for(Map.Entry<String, String> entry : childParentMap.entrySet()) {
            if (entry.getKey().startsWith("fin")) {
                pathsLastEdges.add(entry);
            }
        }

        List<Double> rates = getRates(fromCode, toCode, childParentMap, currencyCodeToRatesMap, pathsLastEdges);

        Optional<Double> min = rates.stream().min(Double::compareTo);

        if (min.isPresent()) {
            return new ExchangeRate(currencyService.getCurrencyByCode(fromCode), currencyService.getCurrencyByCode(toCode), min.get());
        } else {
            throw new NoSuchElementException("No exchange rate between " + fromCode + " and " + toCode);
        }
    }

    /**
     * Performs a breadth-first search from the specified starting currency code to the
     * specified target currency code using the provided currency code to exchange rates
     * mapping.
     *
     * @param  fromCode                the starting currency code
     * @param  toCode                  the target currency code
     * @param  currencyCodeToRatesMap  the mapping of currency codes to exchange rates
     * @return                         a mapping of child currency codes to their parent
     *                                 currency codes, representing the path from the
     *                                 starting currency code to the target currency code
     */
    private Map<String, String> bfs(String fromCode, String toCode, Map<String, List<ExchangeRate>> currencyCodeToRatesMap) {
        Set<String> checkedCodes = new HashSet<>();
        checkedCodes.add(fromCode);
        String currentCode;
        LinkedList<String> queue = new LinkedList<>();
        queue.add(fromCode);
        Map<String, String> childParentMap = new HashMap<>();
        int pathsNum = 1;

        while (!queue.isEmpty()) {
            currentCode = queue.removeFirst();
            List<ExchangeRate> rates = currencyCodeToRatesMap.get(currentCode);
            for (ExchangeRate rate: rates) {
                String nextCode = rate.getTargetCurrency().getCode();
                if (!checkedCodes.contains(nextCode) && !nextCode.equals(toCode)) {
                    childParentMap.put(nextCode, currentCode);
                    queue.addLast(nextCode);
                }
                if (nextCode.equals(toCode)) {
                    childParentMap.put("fin"+pathsNum, currentCode);
                    pathsNum++;
                }
            }
            checkedCodes.add(currentCode);
        }

        return childParentMap;
    }

    private List<Double> getRates(String fromCode, String toCode, Map<String, String> childParentMap,
                                  Map<String, List<ExchangeRate>> currencyCodeToRatesMap,
                                  List<Map.Entry<String, String>> pathsLastEdges) {
        List<Double> rates = new ArrayList<>();
        for (Map.Entry<String, String> entry : pathsLastEdges) {
            double accumulativeRate = 1.0;
            String nextCode = entry.getValue();
            String codeToFind = toCode;
            outerloop:
            while(true) {
                for(ExchangeRate rate: currencyCodeToRatesMap.get(nextCode)) {
                    if (rate.getTargetCurrency().getCode().equals(codeToFind)) {
                        accumulativeRate *= rate.getRate();
                        break;
                    }
                }
                codeToFind = nextCode;
                nextCode = childParentMap.get(nextCode);
                if (nextCode.equals(fromCode)) {
                    for(ExchangeRate rate: currencyCodeToRatesMap.get(nextCode)) {
                        if (rate.getTargetCurrency().getCode().equals(codeToFind)) {
                            accumulativeRate *= rate.getRate();
                            rates.add(accumulativeRate);
                            break outerloop;
                        }
                    }
                }
            }
        }

        return rates;
    }
}
