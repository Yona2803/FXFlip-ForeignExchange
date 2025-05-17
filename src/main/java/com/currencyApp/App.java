package com.currencyApp;

import com.currencyApp.config.Config;
import com.currencyApp.model.Currency;
import com.currencyApp.ui.ComboBoxElement;
import com.currencyApp.util.CurrencyFormatter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.currencyApp.model.Currency.getTodayRates_OneCurrency;
import com.currencyApp.ui.StatisticPreview;

public class App extends Application {
    private boolean isExpanded = false;

    private Currency findDefaultCurrency(List<Currency> currencies, String code) {
        if (currencies == null || currencies.isEmpty()) {
            return null;
        }
        return currencies.stream()
                .filter(c -> c.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(currencies.get(0));
    }

    @Override
    public void start(Stage primaryStage) {
        final double[] offsetX = {0};
        final double[] offsetY = {0};

        setupPrimaryStage(primaryStage);

        VBox layout = createMainLayout();
        addDragSupport(layout, primaryStage, offsetX, offsetY);

        VBox innerFrame = createInnerFrame();
        layout.getChildren().add(innerFrame);

        List<Currency> currencies = Currency.getCurrencyListFromAPI();
        List<Currency> currencyRates = Currency.getCurrencyRatesFromAPI();

        if (currencies == null || currencies.isEmpty() || currencyRates == null || currencyRates.isEmpty()) {
            showErrorAlert("Failed to load currency data. Please check your connection.");
            return;
        }

        ComboBoxElement fromCurrencyBox = new ComboBoxElement(currencies, findDefaultCurrency(currencies, "MAD"));
        ComboBoxElement toCurrencyBox = new ComboBoxElement(currencies, findDefaultCurrency(currencies, "USD"));
        fromCurrencyBox.setPadding(new Insets(0, 0, 18, 0));
        toCurrencyBox.setPadding(new Insets(0, 0, 18, 0));

        TextField amountField = new TextField();
        amountField.setPromptText("");
        amountField.getStyleClass().add("input-Field");
        amountField.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/currencyApp/assets/Style/EntryField.css")).toExternalForm());

        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                amountField.setText(oldVal);
            }
        });

        Label leftLabel = new Label("From:");
        leftLabel.getStyleClass().add("label-Field");
        leftLabel.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/currencyApp/assets/Style/EntryField.css")).toExternalForm());

        Label resultLabel = new Label();
        resultLabel.getStyleClass().add("output-Field");
        resultLabel.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/currencyApp/assets/Style/EntryField.css")).toExternalForm());

        Label rightLabel = new Label("To:");
        rightLabel.getStyleClass().add("label-Field");
        rightLabel.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/currencyApp/assets/Style/EntryField.css")).toExternalForm());

        // Create currency change section with array to hold reference
        HBox[] currencyChangeSection = new HBox[1];
        currencyChangeSection[0] = createCurrencyChangeSection(fromCurrencyBox.getSelectedItem(), toCurrencyBox.getSelectedItem());

        VBox rightSection = new VBox(2);
        rightSection.setPrefSize(309, 148);
        rightSection.setMaxWidth(309);
        rightSection.getChildren().addAll(
                toCurrencyBox,
                rightLabel,
                resultLabel,
                currencyChangeSection[0]
        );

        // Combined update method
        Runnable updateDisplay = () -> {
            calculateAndDisplay(amountField, fromCurrencyBox, toCurrencyBox, currencyRates, resultLabel);
            currencyChangeSection[0] = createCurrencyChangeSection(fromCurrencyBox.getSelectedItem(), toCurrencyBox.getSelectedItem());
            rightSection.getChildren().set(3, currencyChangeSection[0]);
        };

        fromCurrencyBox.getComboBox().valueProperty().addListener((obs, oldVal, newVal) -> updateDisplay.run());
        toCurrencyBox.getComboBox().valueProperty().addListener((obs, oldVal, newVal) -> updateDisplay.run());
        amountField.textProperty().addListener((obs, oldVal, newVal) -> updateDisplay.run());

        VBox leftSection = new VBox(2);
        leftSection.setPrefSize(309, 148);
        leftSection.setMaxWidth(309);
        leftSection.getChildren().addAll(
                fromCurrencyBox,
                leftLabel,
                amountField
        );

        Region separatorLine = new Region();
        separatorLine.setPrefSize(1, 92);
        separatorLine.setStyle("-fx-background-color: #A6A6A6;");
        VBox separatorWrapper = new VBox(separatorLine);
        separatorWrapper.setPadding(new Insets(45, 43, 0, 43));

        HBox exchangeSection = new HBox();
        exchangeSection.setPrefSize(733, 100);
        exchangeSection.setPadding(new Insets(24, 0, 0, 0));
        exchangeSection.getChildren().addAll(leftSection, separatorWrapper, rightSection);

        innerFrame.getChildren().add(exchangeSection);

        HBox middleSection = new HBox();
        middleSection.setPrefSize(733, 40);
        middleSection.setAlignment(Pos.CENTER_LEFT);

        HBox comparisonSection = createCurrencyComparisonSection(currencyRates);
        comparisonSection.setMinWidth(559);

        Button toggleButton = new Button("Show Statistics");
        toggleButton.getStyleClass().add("custom-button");
        toggleButton.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/currencyApp/assets/Style/Button.css")).toExternalForm());
        toggleButton.setMinWidth(174);

        middleSection.getChildren().addAll(comparisonSection, toggleButton);
        layout.getChildren().add(middleSection);

        // Create a more substantive statistics section
        VBox statisticViewer = createStatisticsSection(currencyRates);
        statisticViewer.setVisible(false); // Initially hidden
        statisticViewer.setManaged(false); // Won't take up space when hidden

        // Set a height that makes sense for the expanded window
        statisticViewer.setPrefSize(733, 300); // Increase height from 100 to 300
        statisticViewer.setMinHeight(300);     // Ensure it takes minimum height

        statisticViewer.setPadding(new Insets(24, 0, 0, 0));
        layout.getChildren().add(statisticViewer);

        // Modify the toggle button action
        toggleButton.setOnAction(event -> {
            isExpanded = !isExpanded;
            toggleButton.setText(isExpanded ? "Hide Statistics" : "Show Statistics");

            // Show/hide the statistics section
            statisticViewer.setVisible(isExpanded);
            statisticViewer.setManaged(isExpanded);

            // Smooth height animation
            double targetHeight = isExpanded ? 703 : 363;
            javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(
                            javafx.util.Duration.millis(300),
                            new javafx.animation.KeyValue(layout.prefHeightProperty(), targetHeight)
                    )
            );
            timeline.setCycleCount(1);
            timeline.play();

            // Also animate the stage height to match
            primaryStage.setHeight(targetHeight);
        });

        // Set initial size constraints
        layout.setMinHeight(363);
        layout.setMaxHeight(703);
        layout.setPrefHeight(363);

        Scene scene = new Scene(layout);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createStatisticsSection(List<Currency> currencyRates) {
        VBox statsSection = new VBox(20);
        statsSection.setPadding(new Insets(20));

        // Get the base currency from the application (default to MAD)
        String baseCurrency = "MAD";

        // Select target currencies for comparison
        List<String> targetCurrencies = Arrays.asList("EUR", "CNY", "USD", "SAR");

        // Create a map to store historical rates for each target currency
        Map<String, Map<String, Float>> historicalRates = new HashMap<>();

        // Process each target currency to get historical data
        for (String targetCurrency : targetCurrencies) {
            // Create a map to store date -> rate pairs
            Map<String, Float> dateRateMap = new HashMap<>();

            try {
                // Use the new method to fetch 14-day historical data from API
                Map<String, Map<String, Object>> historicalData = Currency.getChangesInPast14days(baseCurrency, targetCurrency);

                if (historicalData != null && historicalData.containsKey(targetCurrency)) {
                    // Extract the date -> rate map for this currency
                    Map<String, Object> currencyData = historicalData.get(targetCurrency);

                    // Convert the data to our format (date -> float)
                    for (Map.Entry<String, Object> entry : currencyData.entrySet()) {
                        String date = entry.getKey();
                        Double rate = (Double) entry.getValue();
                        dateRateMap.put(date, rate.floatValue());
                    }
                } else {
                    // If API call failed, fall back to current rate with deterministic pattern
                    double currentRate = getRateForCurrency(currencyRates, targetCurrency);
                    LocalDate currentDate = LocalDate.now();

                    for (int i = 0; i < 14; i++) {
                        LocalDate date = currentDate.minusDays(i);
                        String dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE);

                        // Create a deterministic pattern based on the date
                        // This ensures the chart looks the same every time
                        double dayOfYear = date.getDayOfYear();
                        double yearFactor = date.getYear() * 0.01;
                        double patternFactor = Math.sin(dayOfYear / 15.0) * 0.02;

                        // Use the current rate as baseline
                        float rate = (float) (currentRate * (1 + patternFactor + yearFactor));
                        dateRateMap.put(dateString, rate);
                    }

                    System.out.println("Using fallback data pattern for " + targetCurrency);
                }
            } catch (Exception e) {
                System.err.println("Error getting historical data for " + targetCurrency + ": " + e.getMessage());
                e.printStackTrace();

                // If there's an error, create fallback deterministic data
                LocalDate currentDate = LocalDate.now();
                for (int i = 0; i < 14; i++) {
                    LocalDate date = currentDate.minusDays(i);
                    String dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE);

                    // Use a base rate specific to the currency
                    float baseRate = switch(targetCurrency) {
                        case "EUR" -> 0.09f;
                        case "CNY" -> 0.72f;
                        case "USD" -> 0.1f;
                        case "SAR" -> 0.078f;
                        default -> 0.1f;
                    };

                    // Apply deterministic pattern
                    double dayOfYear = date.getDayOfYear();
                    double patternFactor = Math.sin(dayOfYear / 15.0) * 0.02;
                    float rate = baseRate * (1 + (float)patternFactor);

                    dateRateMap.put(dateString, rate);
                }
            }

            // Add the date -> rate map for this currency to the historical rates map
            historicalRates.put(targetCurrency, dateRateMap);
        }

        // Create and add the StatisticPreview component
        StatisticPreview statisticPreview = new StatisticPreview(
                baseCurrency,
                targetCurrencies,
                historicalRates
        );

        // Add the statistic preview to the stats section
        statsSection.getChildren().add(statisticPreview);

        return statsSection;
    }

    private HBox createCurrencyChangeSection(String fromCurrency, String toCurrency) {
        HBox mainContainer = new HBox(8);
        mainContainer.setAlignment(Pos.CENTER_LEFT);

        try {
            Map<String, Map<String, Object>> result = getTodayRates_OneCurrency(fromCurrency, toCurrency);

            if (result == null || !result.containsKey("rates")) {
                throw new Exception("Invalid currency data");
            }

            float currentRate = (Float) result.get("rates").get(toCurrency);
            String changePercentage = (String) result.get("changes").get(toCurrency);
            boolean isPositive = (Boolean) result.get("directions").get(toCurrency);

            HBox changeContainer = new HBox();
            changeContainer.setAlignment(Pos.CENTER);
            changeContainer.setPadding(new Insets(5, 6, 5, 6));
            changeContainer.setStyle("-fx-background-radius: 2;");

            Label rateLabel = new Label(CurrencyFormatter.formatConversionRate(currentRate));
            rateLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

            Label percentageLabel = new Label();
            percentageLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));

            if (isPositive) {
                changeContainer.setStyle("-fx-background-color: #1E2920;");
                rateLabel.setText("+ " + rateLabel.getText());
                rateLabel.setStyle("-fx-text-fill: #7AEA8A;");
                percentageLabel.setText("▲ " + changePercentage + " Today");
                percentageLabel.setStyle("-fx-text-fill: #7AEA8A;");
            } else {
                changeContainer.setStyle("-fx-background-color: #291D20;");
                rateLabel.setText("- " + rateLabel.getText());
                rateLabel.setStyle("-fx-text-fill: #E86F8A;");
                percentageLabel.setText("▼ " + changePercentage + " Today");
                percentageLabel.setStyle("-fx-text-fill: #E86F8A;");
            }

            changeContainer.getChildren().add(percentageLabel);
            mainContainer.getChildren().addAll(changeContainer, rateLabel);

        } catch (Exception e) {
            Label errorLabel = new Label("Rate data unavailable");
            errorLabel.setStyle("-fx-text-fill: #FF5555;");
            mainContainer.getChildren().add(errorLabel);
        }

        return mainContainer;
    }

    private HBox currencyChangePercentage(String fromCurrency, String toCurrency) {
        HBox changeContainer = new HBox();
        changeContainer.setAlignment(Pos.CENTER_LEFT);

        try {
            Map<String, Map<String, Object>> result = getTodayRates_OneCurrency(fromCurrency, toCurrency);

            if (result == null || !result.containsKey("rates")) {
                throw new Exception("Invalid currency data");
            }

            float currentRate = (Float) result.get("rates").get(toCurrency);
            String changePercentage = (String) result.get("changes").get(toCurrency);
            boolean isPositive = (Boolean) result.get("directions").get(toCurrency);

            Label FromCurrency = new Label(fromCurrency);
            FromCurrency.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            FromCurrency.setStyle("-fx-text-fill: #FFFFFF;");

            Label percentageLabel = new Label();
            percentageLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));

            if (isPositive) {
                percentageLabel.setText(" ▲ " + changePercentage);
                percentageLabel.setStyle("-fx-text-fill: #7AEA8A; -fx-padding: 0 0 0 5;");
            } else {
                percentageLabel.setText(" ▼ " + changePercentage);
                percentageLabel.setStyle("-fx-text-fill: #E86F8A; -fx-padding: 0 0 0 5;");
            }

            changeContainer.getChildren().addAll(FromCurrency, percentageLabel);

        } catch (Exception e) {
            Label errorLabel = new Label("Rate data unavailable");
            errorLabel.setStyle("-fx-text-fill: #FF5555;");
            changeContainer.getChildren().add(errorLabel);
        }

        return changeContainer;
    }

    private HBox createCurrencyComparisonSection(List<Currency> currencyRates) {
        HBox comparisonContainer = new HBox(10);
        comparisonContainer.setAlignment(Pos.TOP_LEFT);

        String[] currenciesToCompare = {"EUR", "CNY", "USD", "SAR"};

        for (String currencyCode : currenciesToCompare) {
            VBox currencyBox = createSingleCurrencyComparison(currencyCode, "MAD", currencyRates);
            comparisonContainer.getChildren().add(currencyBox);
        }

        return comparisonContainer;
    }

    private VBox createSingleCurrencyComparison(String fromCurrency, String toCurrency, List<Currency> currencyRates) {
        VBox container = new VBox();
        container.setAlignment(Pos.TOP_LEFT);
        container.setStyle(
                "-fx-padding: 0 10 3 10;" +
                        "-fx-min-width: 94;"
        );

        HBox pairLabelContainer = new HBox(0);
        Label mainCurrency = new Label(" / MAD");
        mainCurrency.setStyle("-fx-text-fill: #B2B2B2; -fx-font-weight: bold; -fx-font-size: 12;");
        Label pairLabel = new Label(fromCurrency);
        pairLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 12;");
        pairLabelContainer.getChildren().addAll(pairLabel, mainCurrency);

        Label rateLabel = new Label();
        rateLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 12;");

        HBox CurrencyChangeSection = currencyChangePercentage(fromCurrency, toCurrency);
        try {
            double fromRate = getRateForCurrency(currencyRates, fromCurrency);
            double toRate = getRateForCurrency(currencyRates, toCurrency);
            double rate = fromRate / toRate;
            rateLabel.setText(CurrencyFormatter.formatExchangeRate(rate));

        } catch (Exception e) {
            rateLabel.setText("N/A");
        }

        container.getChildren().addAll(pairLabelContainer, rateLabel, CurrencyChangeSection);
        return container;
    }

    private void calculateAndDisplay(TextField amountField, ComboBoxElement fromBox,
                                     ComboBoxElement toBox, List<Currency> currencyRates,
                                     Label resultLabel) {
        try {
            String fromCode = fromBox.getSelectedItem();
            String toCode = toBox.getSelectedItem();

            if (amountField.getText() == null || amountField.getText().trim().isEmpty()) {
                resultLabel.setText("");
                return;
            }

            double amount = Double.parseDouble(amountField.getText());
            double fromRate = getRateForCurrency(currencyRates, fromCode);
            double toRate = getRateForCurrency(currencyRates, toCode);

            if (fromRate == 0 || toRate == 0) {
                resultLabel.setText("Error: Invalid exchange rate");
                return;
            }

            double baseAmount = amount / fromRate;
            double converted = baseAmount * toRate;
            resultLabel.setText(CurrencyFormatter.formatConversionRate(converted));

        } catch (NumberFormatException e) {
            resultLabel.setText("");
        } catch (Exception e) {
            resultLabel.setText("Error: " + e.getMessage());
        }
    }

    private double getRateForCurrency(List<Currency> currencyList, String code) {
        for (Currency c : currencyList) {
            if (c.getCode().equals(code)) {
                return c.getRate();
            }
        }
        throw new IllegalArgumentException("Rate not found for " + code);
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupPrimaryStage(Stage primaryStage) {
        primaryStage.setTitle(Config.get("appName"));
        primaryStage.initStyle(StageStyle.TRANSPARENT);
    }

    private VBox createMainLayout() {
        VBox layout = new VBox(10);
        layout.setPrefSize(757, 363);
        layout.setStyle(
                "-fx-background-color: #010101;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 12;"
        );
        return layout;
    }

    private void addDragSupport(VBox layout, Stage primaryStage, double[] offsetX, double[] offsetY) {
        layout.setOnMousePressed(event -> {
            offsetX[0] = event.getSceneX();
            offsetY[0] = event.getSceneY();
        });

        layout.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - offsetX[0]);
            primaryStage.setY(event.getScreenY() - offsetY[0]);
        });
    }

    private VBox createInnerFrame() {
        VBox innerFrame = new VBox(10);
        innerFrame.setPrefSize(733, 268);
        innerFrame.setStyle(
                "-fx-background-color: #1E1E1E;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 17;"
        );

        HBox titleContainer = new HBox(10);
        titleContainer.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("FXFlip - Foreign Exchange");
        title.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 26));
        title.setStyle("-fx-text-fill: white;");
        title.setMinWidth(650);

        Label close = new Label("❌ Close");
        close.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        close.setStyle("-fx-text-fill: #B14143; -fx-cursor: hand; ");
        close.setOnMouseClicked(event -> {
             Platform.exit();
        });


        titleContainer.getChildren().addAll(title, close);

        innerFrame.getChildren().add(titleContainer);

        return innerFrame;
    }

    public static void main(String[] args) {
        launch(args);
    }
}