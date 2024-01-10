package org.bidribidi.currency.service.validators;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurrencyValidator {
    private final static Pattern CODE_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private final static Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z]*[a-zA-Z]*[a-zA-Z]*$");

    private static void isValidCode(String code) {
        Matcher m = CODE_PATTERN.matcher(code);
        if (!m.matches()){
            throw new IllegalArgumentException("Invalid currency code: must be exactly 3 literal");
        }
    }

    private static void isValidName(String name) {
        Matcher m = NAME_PATTERN.matcher(name);
        if (!m.matches()){
            throw new IllegalArgumentException("Invalid currency name: must be not empty");
        }
    }

    private static void isValidSign(String sign) {
        // TODO: implement
    }

    public static void validate(String code, String name, String sign) {
        isValidCode(code);
        isValidName(name);
        isValidSign(sign);
    }


}
