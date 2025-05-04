package com.currencyApp.model;

public class currency {
    private String code;
    private String name;

    public currency(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
}
