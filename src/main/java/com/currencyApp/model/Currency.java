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

    public static Map<String, Map<String, Object>> getTodayRates_OneCurrency(String baseCurrency, String targetCurrency) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate secondDayBefore = today.minusDays(2);

        String endpoint = String.format("/time-series?from=%s&to=%s&start=%s&end=%s&",
                baseCurrency, targetCurrency, secondDayBefore.toString(), yesterday.toString());

        Map<String, Float> todayRates = new HashMap<>();
        Map<String, String> rateChanges = new HashMap<>();
        Map<String, Boolean> changeDirections = new HashMap<>();

        try {
            String response = ExchangeRateService.getResponse(endpoint);
            JsonObject json = gson.fromJson(response, JsonObject.class);

            // Verify we got results for the requested currency pair
            if (!json.has("results") || !json.getAsJsonObject("results").has(targetCurrency)) {
                throw new RuntimeException("No data available for " + baseCurrency + " to " + targetCurrency);
            }

            JsonObject currencyData = json.getAsJsonObject("results").getAsJsonObject(targetCurrency);

            // Get rates for both days
            float yesterdayRate = currencyData.get(yesterday.toString()).getAsFloat();
            float dayBeforeRate = currencyData.get(secondDayBefore.toString()).getAsFloat();

            // Store the current rate
            todayRates.put(targetCurrency, yesterdayRate);

            // Calculate percentage change
            float change = ((yesterdayRate - dayBeforeRate) / dayBeforeRate) * 100f;
            boolean isChangePositive = yesterdayRate >= dayBeforeRate;
            String formattedChange = String.format("%.2f%%", Math.abs(change));

            // Store the change information
            rateChanges.put(targetCurrency, formattedChange);
            changeDirections.put(targetCurrency, isChangePositive);

        } catch (Exception e) {
            e.printStackTrace();
            // Consider rethrowing or handling the error appropriately
        }

        // Prepare the result map
        Map<String, Map<String, Object>> result = new HashMap<>();
        result.put("rates", new HashMap<>(todayRates));
        result.put("changes", new HashMap<>(rateChanges));
        result.put("directions", new HashMap<>(changeDirections));

        return result;
    }

    public static Map<String, Map<String, Object>> getTodayRatesAnd24hChange_AllCurrencies(String baseCurrency) {
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

    public static Map<String, Map<String, Object>> getChangesInPast14days(String baseCurrency, String targetCurrency) {
        Map<String, Map<String, Object>> result = new HashMap<>();

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate day15Before = today.minusDays(14);

        String endpoint = String.format(
                "/time-series?from=%s&to=%s&start=%s&end=%s&",
                baseCurrency, targetCurrency, day15Before, yesterday
        );

        try {
            String response = ExchangeRateService.getResponse(endpoint); // implement this yourself
            JsonObject json = gson.fromJson(response, JsonObject.class);

            // Add base currency
            String base = json.get("base").getAsString();
            result.put("base", Map.of("code", base));  // You can customize this if needed

            // Add currency time-series data
            JsonObject results = json.getAsJsonObject("results");

            for (Map.Entry<String, JsonElement> currencyEntry : results.entrySet()) {
                String currency = currencyEntry.getKey(); // e.g., "USD"
                JsonObject dateRateObject = currencyEntry.getValue().getAsJsonObject();

                Map<String, Object> dateRateMap = new TreeMap<>(); // Sorted by date
                for (Map.Entry<String, JsonElement> rateEntry : dateRateObject.entrySet()) {
                    dateRateMap.put(rateEntry.getKey(), rateEntry.getValue().getAsDouble());
                }

                result.put(currency, dateRateMap);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
