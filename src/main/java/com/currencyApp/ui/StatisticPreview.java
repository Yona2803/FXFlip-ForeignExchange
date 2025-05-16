package com.currencyApp.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.*;

public class StatisticPreview extends VBox {
    // Modern color palette
    private final Color[] CHART_COLORS = {
            Color.web("#7AEA8A"),
            Color.web("#8265FC"),
            Color.web("#DE754F"),
            Color.web("#D94C4E"),
    };

    public StatisticPreview(
            String baseCurrency,
            List<String> targetCurrencies,
            Map<String, Map<String, Float>> historicalRates
    ) {
        setSpacing(10);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: #010101;");
        setAlignment(Pos.CENTER);

        Label titleLabel = new Label("14-Day Exchange Rate Trend");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setTextAlignment(TextAlignment.LEFT);

        LineChart<String, Number> percentageChart = createPercentageChart(targetCurrencies, historicalRates);
        percentageChart.setPrefHeight(268);
        percentageChart.setMinHeight(268);
        percentageChart.setMaxHeight(268);
        percentageChart.setPrefWidth(Double.MAX_VALUE);
        VBox.setVgrow(percentageChart, Priority.ALWAYS);

        getChildren().addAll(percentageChart);
    }

    private LineChart<String, Number> createPercentageChart(
            List<String> targetCurrencies,
            Map<String, Map<String, Float>> historicalRates
    ) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        // Axis styling (unchanged)
        xAxis.setLabel("");
        xAxis.setTickLabelFont(Font.font("Segoe UI", FontWeight.LIGHT, 10));
        xAxis.setTickLabelFill(Color.web("#B2B2B2"));
        xAxis.setTickLabelRotation(45);
        xAxis.setStyle("-fx-tick-label-fill: #B2B2B2;");

        yAxis.setLabel("");
        yAxis.setTickLabelFont(Font.font("Segoe UI", FontWeight.LIGHT, 10));
        yAxis.setTickLabelFill(Color.web("#B2B2B2"));
        yAxis.setAutoRanging(true);
        yAxis.setStyle("-fx-tick-label-fill: #B2B2B2;");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendSide(Side.RIGHT);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(false);
        chart.setStyle("-fx-background-color: #010101;");

        // Add series for each currency with error handling
        int colorIndex = 0;
        for (String currency : targetCurrencies) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(currency);

            Map<String, Float> dateRateMap = historicalRates.get(currency);
            if (dateRateMap == null || dateRateMap.isEmpty()) {
                System.err.println("Warning: No historical data for " + currency);
                continue;
            }

            List<String> dates = new ArrayList<>(dateRateMap.keySet());
            try {
                Collections.sort(dates); // Sort dates chronologically
            } catch (Exception e) {
                System.err.println("Error sorting dates for " + currency + ": " + e.getMessage());
                continue;
            }

            // Get first valid rate as baseline
            Float firstDayValue = getFirstValidRate(dates, dateRateMap);
            if (firstDayValue == null || firstDayValue == 0f) {
                System.err.println("Invalid baseline rate for " + currency + " (zero or missing)");
                continue;
            }

            for (String date : dates) {
                Float currentValue = dateRateMap.get(date);
                if (currentValue == null) {
                    System.err.println("Missing rate for " + currency + " on " + date);
                    continue;
                }

                try {
                    double percentageChange = ((currentValue - firstDayValue) / firstDayValue) * 100;
                    String formattedDate = formatDateSafely(date);
                    series.getData().add(new XYChart.Data<>(formattedDate, percentageChange));
                } catch (ArithmeticException e) {
                    System.err.println("Calculation error for " + currency + " on " + date + ": " + e.getMessage());
                }
            }

            if (!series.getData().isEmpty()) {
                chart.getData().add(series);
                colorIndex++;
            }
        }

        // Apply styling (unchanged)
        applyChartStyles(chart);
        return chart;
    }

    private Float getFirstValidRate(List<String> dates, Map<String, Float> dateRateMap) {
        for (String date : dates) {
            Float rate = dateRateMap.get(date);
            if (rate != null && rate != 0f) {
                return rate;
            }
        }
        return null;
    }

    private String formatDateSafely(String date) {
        try {
            if (date.length() >= 10) { // YYYY-MM-DD format
                return date.substring(8) + "-" + date.substring(5, 7);
            }
            return date; // Fallback to raw date if unexpected format
        } catch (Exception e) {
            System.err.println("Date formatting error for " + date + ": " + e.getMessage());
            return date;
        }
    }

    private void applyChartStyles(LineChart<String, Number> chart) {
        chart.lookupAll(".chart-plot-background").forEach(node ->
                node.setStyle("-fx-background-color: #010101;")
        );
        chart.applyCss();

//        chart.lookupAll(".chart-legend-item-text").forEach(node ->
//                node.setStyle("-fx-text-fill: #FF0000;")
//        );

        chart.lookupAll(".chart-legend").forEach(node ->
                node.setStyle("-fx-background-color: transparent;")
        );

        chart.lookupAll(".chart-horizontal-grid-lines").forEach(node ->
                node.setStyle("-fx-stroke: transparent;"));

        for (int i = 0; i < chart.getData().size(); i++) {
            XYChart.Series<String, Number> series = chart.getData().get(i);
            Color color = CHART_COLORS[i % CHART_COLORS.length];
            String colorHex = colorToHex(color);

            // 1. Style the line
            Node line = series.getNode().lookup(".chart-series-line");
            if (line != null) {
                line.setStyle(
                        "-fx-stroke: " + colorHex + ";" +
                                "-fx-stroke-width: 2px;"
                );
            }


            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle(
                            "-fx-background-radius: 5px;" +
                                    "-fx-border-radius: 5px;" +
                                    "-fx-min-width: 10px;" +
                                    "-fx-min-height: 10px;" +
                                    "-fx-max-width: 10px;" +
                                    "-fx-max-height: 10px;" +
                                    "-fx-pref-width: 10px;" +
                                    "-fx-pref-height: 10px;" +
                                    "-fx-background-color: black;" +
                                    "-fx-border-color: " + colorHex + ";" +
                                    "-fx-border-width: 2px;"
                    );


//                    Tooltip tooltip = new Tooltip(
//                            String.format("%s\nDate: %s\nChange: %.2f%%",
//                                    series.getName(),
//                                    data.getXValue(),
//                                    data.getYValue())
//                    );
//                    tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: #FFDD00;");
//                    Tooltip.install(data.getNode(), tooltip);
                    // Update tooltip style if needed
                    Tooltip.install(data.getNode(), new Tooltip(String.format("%s\nDate: %s\nChange: %.2f%%",
                            series.getName(), data.getXValue(), data.getYValue())));

                }
            }
        }
        // Set legend text colors to match series colors
        List<Node> legendItems = new ArrayList<>(chart.lookupAll(".chart-legend-item")); // Convert Set to List
        for (int i = 0; i < legendItems.size(); i++) {
            Node legendItem = legendItems.get(i);
            Color color = CHART_COLORS[i % CHART_COLORS.length];
            String colorHex = colorToHex(color);
            Node textNode = legendItem.lookup(".chart-legend-item-text");
            if (textNode != null) {
                textNode.setStyle("-fx-text-fill: " + colorHex + ";");
            }
        }

    }


    private String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}