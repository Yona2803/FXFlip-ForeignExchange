package com.currencyApp.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonUtils {
    public static void printRates(JsonObject json) {
        JsonObject rates = json.getAsJsonObject("rates");
        for (String key : rates.keySet()) {
            System.out.println(key + ": " + rates.get(key).getAsDouble());
        }
    }
}
