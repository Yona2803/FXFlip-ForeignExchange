package com.currencyApp.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import java.time.LocalDate;
import java.util.*;

public class Currency {
    private static final Gson gson = new Gson();

    private String code;
    private String name;
    private double rate;

    public Currency(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return code; // Show code in ComboBox
    }

    public static List<Currency> getCurrencyListFromAPI() {
        String endpoint = "/currencies?";
        String jsonString = ExchangeRateService.getResponse(endpoint); // Need to ensure this class exists
        JsonObject json = gson.fromJson(jsonString, JsonObject.class);
        JsonObject currenciesJson = json.getAsJsonObject("currencies");

        List<Currency> currencyList = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : currenciesJson.entrySet()) {
            String code = entry.getKey();
            String name = entry.getValue().getAsString();
            currencyList.add(new Currency(code, name));
        }

        return currencyList;
    }

    public static List<Currency> getCurrencyRatesFromAPI() {
        String endpoint = "/fetch-all?from=MAD&";
        String jsonString = ExchangeRateService.getResponse(endpoint);
        JsonObject json = gson.fromJson(jsonString, JsonObject.class);
        JsonObject currenciesJson = json.getAsJsonObject("results");

        List<Currency> currencyList = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : currenciesJson.entrySet()) {
            String currencyCode = entry.getKey();
            String rateStr = entry.getValue().getAsString();
            try {
                double rate = Double.parseDouble(rateStr);
                Currency currencyObj = new Currency(currencyCode, "");
                currencyObj.setRate(rate);
                currencyList.add(currencyObj);
            } catch (NumberFormatException e) {
                System.err.println("Invalid rate format for " + currencyCode + ": " + rateStr);
            }
        }

        return currencyList;
    }
    public static Map<String, Map<String, Object>> getTodayRatesAnd24hChange(String baseCurrency) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate secondDayBefore = today.minusDays(2);

        String endpointToday = String.format("/historical?date=%s&from=%s&", yesterday.toString(), baseCurrency);
        String endpointYesterday = String.format("/historical?date=%s&from=%s&", secondDayBefore.toString(), baseCurrency);

        Map<String, Float> todayRates = new HashMap<>();
        Map<String, String> rateChanges = new HashMap<>();
        Map<String, Boolean> changeDirections = new HashMap<>();

        try {
            String todayResponse = ExchangeRateService.getResponse(endpointToday);
            String yesterdayResponse = ExchangeRateService.getResponse(endpointYesterday);

            JsonObject todayJson = gson.fromJson(todayResponse, JsonObject.class);
            JsonObject yesterdayJson = gson.fromJson(yesterdayResponse, JsonObject.class);

            JsonObject todayConversions = todayJson.getAsJsonObject("results");
            JsonObject yesterdayConversions = yesterdayJson.getAsJsonObject("results");

            Set<Map.Entry<String, JsonElement>> entries = todayConversions.entrySet();
            for (Map.Entry<String, JsonElement> entry : entries) {
                String currency = entry.getKey();
                float todayRate = entry.getValue().getAsFloat();
                float yesterdayRate = yesterdayConversions.has(currency)
                        ? yesterdayConversions.get(currency).getAsFloat()
                        : todayRate;

                todayRates.put(currency, todayRate);

                float change = ((todayRate - yesterdayRate) / yesterdayRate) * 100f;
                boolean isChangePositive = todayRate >= yesterdayRate;
                String formattedChange = String.format("%.2f%%", Math.abs(change));

                rateChanges.put(currency, formattedChange);
                changeDirections.put(currency, isChangePositive);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Map<String, Object>> result = new HashMap<>();
        result.put("rates", (Map<String, Object>) (Map<?, ?>) todayRates);
        result.put("changes", (Map<String, Object>) (Map<?, ?>) rateChanges);
        result.put("directions", (Map<String, Object>) (Map<?, ?>) changeDirections);

        return result;
    }
}
