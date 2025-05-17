package com.currencyApp.util;

/**
 * Utility class for consistent currency rate formatting across the application
 */
public class CurrencyFormatter {

    /**
     * Format a currency rate with a consistent number of decimal places
     * Used for conversion results
     *
     * @param rate The rate to format
     * @return The formatted rate as a string
     */
    public static String formatConversionRate(double rate) {
        return String.format("%.4f", rate);
    }

    /**
     * Format a currency rate with extended precision
     * Used for displaying exchange rates in comparison section
     *
     * @param rate The rate to format
     * @return The formatted rate as a string
     */
    public static String formatExchangeRate(double rate) {
        return String.format("%.4f", rate);
    }

    /**
     * Format a percentage change value
     * Used for displaying percentage changes in currency values
     *
     * @param percentageChange The percentage change to format
     * @return The formatted percentage as a string
     */
    public static String formatPercentageChange(double percentageChange) {
        return String.format("%.2f%%", percentageChange);
    }

    /**
     * Format a chart tooltip value
     * Used for displaying values in chart tooltips
     *
     * @param value The value to format
     * @return The formatted value as a string
     */
    public static String formatChartValue(double value) {
        return String.format("%.4f%%", value);
    }
}