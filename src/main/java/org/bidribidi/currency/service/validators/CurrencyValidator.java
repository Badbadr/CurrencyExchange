package org.bidribidi.currency.service.validators;

import org.bidribidi.currency.dto.CurrencyRequest;
import org.bidribidi.currency.model.Currency;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurrencyValidator {
    private final static Pattern CODE_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private final static Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z()]+(\\s+[a-zA-Z()]+)*$");

    public static void isValidCode(String code) throws IllegalArgumentException {
        Matcher m = CODE_PATTERN.matcher(code);
        if (!m.matches()){
            throw new IllegalArgumentException("""
                    Invalid currency code:
                    1) must be not empty
                    2) must be without leading or trailing whitespaces
                    3) must contain exactly 3 upper case literal""");
        }
    }

    public static void isValidName(String name) throws IllegalArgumentException {
        Matcher m = NAME_PATTERN.matcher(name);
        if (!m.matches()){
            throw new IllegalArgumentException("""
                    Invalid currency name:
                    1) must be not empty
                    2) must be without leading or trailing whitespaces"""
            );
        }
    }

    private static void isValidSign(String sign) {
        // TODO: implement
    }

    public static void validate(CurrencyRequest currencyRequest) throws IllegalArgumentException {
        isValidCode(currencyRequest.code());
        isValidName(currencyRequest.fullname());
        isValidSign(currencyRequest.sign());
    }

    public static void validate(Currency currency) throws IllegalArgumentException {
        isValidCode(currency.getCode());
        isValidName(currency.getName());
        isValidSign(currency.getSign());
    }


}
