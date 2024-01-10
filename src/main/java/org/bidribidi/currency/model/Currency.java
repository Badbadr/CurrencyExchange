package org.bidribidi.currency.model;

import org.bidribidi.currency.dto.CurrencyRequest;

public class Currency {
    private Integer id;
    private String code;
    private String name;
    private String sign;

    public Currency(String code, String fullname, String sign) {
        this.code = code;
        this.name = fullname;
        this.sign = sign;
    }

    public Currency(CurrencyRequest currencyRequest) {
        this.code = currencyRequest.code();
        this.name = currencyRequest.fullname();
        this.sign = currencyRequest.sign();
    }

    public Currency(Integer id, CurrencyRequest currencyRequest) {
        this.id = id;
        this.code = currencyRequest.code();
        this.name = currencyRequest.fullname();
        this.sign = currencyRequest.sign();
    }

    public Currency(Integer id, String code, String fullname, String sign) {
        this.id = id;
        this.code = code;
        this.name = fullname;
        this.sign = sign;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Currency{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", fullname='" + name + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
