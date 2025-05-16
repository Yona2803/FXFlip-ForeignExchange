package com.currencyApp;

import com.currencyApp.config.Config;
import com.currencyApp.model.Currency;
import com.currencyApp.ui.ComboBoxElement;
import javafx.application.Application;
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

import java.awt.*;
import java.util.List;
import java.util.Objects;

import com.currencyApp.model.Currency;

public class App extends Application {

    /**
     * Finds a currency by its code in the given list
     * @param currencies List of currencies to search
     * @param code Currency code to find
     * @return The found Currency or the first currency in the list if not found
     */
    private Currency findDefaultCurrency(List<Currency> currencies, String code) {
        if (currencies == null || currencies.isEmpty()) {
            return null;
        }

        return currencies.stream()
                .filter(c -> c.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(currencies.get(0));
    }

    /**
     * Creates a labeled container for a currency ComboBox
     * @param comboBoxElement The ComboBoxElement
     * @return VBox containing the ComboBoxElement
     */
    private VBox createLabeledCurrencyBox(ComboBoxElement comboBoxElement) {
        VBox container = new VBox(5);
        container.getChildren().addAll( comboBoxElement);
        return container;
    }

    @Override
    public void start(Stage primaryStage) {
        // Window drag support variables
        final double[] offsetX = {0};
        final double[] offsetY = {0};

        // Configure primary stage
        setupPrimaryStage(primaryStage);

        // Create the main layout
        VBox layout = createMainLayout();

        // Add window drag support
        addDragSupport(layout, primaryStage, offsetX, offsetY);

        // Create and add an inner frame
        VBox innerFrame = createInnerFrame();
        layout.getChildren().add(innerFrame);

        // Get a currency list from API
        List<Currency> currencies = Currency.getCurrencyListFromAPI();
        List<Currency> currencyRates = Currency.getCurrencyRatesFromAPI();

        if (currencies == null || currencies.isEmpty() || currencyRates == null || currencyRates.isEmpty()) {
            showErrorAlert("Failed to load currency data. Please check your connection.");
            return;
        }
        // Create multiple ComboBoxElements with different default currencies
        ComboBoxElement fromCurrencyBox = new ComboBoxElement(currencies, findDefaultCurrency(currencies, "MAD"));
        ComboBoxElement toCurrencyBox = new ComboBoxElement(currencies, findDefaultCurrency(currencies, "USD"));
        fromCurrencyBox.setPadding(new javafx.geometry.Insets(0, 0, 18,0 ));
        toCurrencyBox.setPadding(new javafx.geometry.Insets(0, 0, 18,0 ));

        TextField amountField = new TextField();
        amountField.setPromptText("");
        amountField.getStyleClass().add("input-Field");
        amountField.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/currencyApp/assets/Style/EntryField.css")).toExternalForm());

        // Accept only valid float/double inputs
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                amountField.setText(oldVal); // revert to old value
            }
        });

        Label leftLabel = new Label();
        leftLabel.setText("From:");
        leftLabel.getStyleClass().add("label-Field");
        leftLabel.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/currencyApp/assets/Style/EntryField.css")).toExternalForm());

        Label resultLabel = new Label();
        resultLabel.getStyleClass().add("output-Field");
        resultLabel.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/currencyApp/assets/Style/EntryField.css")).toExternalForm());

        Label rightLabel = new Label();
        rightLabel.setText("To:");
        rightLabel.getStyleClass().add("label-Field");
        rightLabel.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/currencyApp/assets/Style/EntryField.css")).toExternalForm());

        // Add listeners for ComboBox changes
        fromCurrencyBox.getComboBox().valueProperty().addListener((obs, oldVal, newVal) -> {
            calculateAndDisplay(amountField, fromCurrencyBox, toCurrencyBox, currencyRates, resultLabel);
        });

        toCurrencyBox.getComboBox().valueProperty().addListener((obs, oldVal, newVal) -> {
            calculateAndDisplay(amountField, fromCurrencyBox, toCurrencyBox, currencyRates, resultLabel);
        });

        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            calculateAndDisplay(amountField, fromCurrencyBox, toCurrencyBox, currencyRates, resultLabel);
        });

        // Layout for multiple currency selectors
        VBox leftSection = new VBox(2);
        leftSection.setPrefSize(309, 148);
        leftSection.setMaxWidth(309);
        leftSection.getChildren().addAll(
                fromCurrencyBox,
                leftLabel,
                amountField
        );

        VBox rightSection = new VBox(2);
        rightSection.setPrefSize(309, 148);
        rightSection.setMaxWidth(309);
        rightSection.getChildren().addAll(
                toCurrencyBox,
                rightLabel,
                resultLabel
        );

        HBox exchangeSection = new HBox();
        exchangeSection.setPrefSize(733, 100);
        exchangeSection.setPadding(new javafx.geometry.Insets(24, 0, 0, 0));

//        HBox separatorLine  = new HBox();
        Region separatorLine = new Region();
        separatorLine.setPrefSize(1, 92);
        separatorLine.setStyle("-fx-background-color: #A6A6A6;");
        VBox separatorWrapper = new VBox(separatorLine);
        separatorWrapper.setPadding(new javafx.geometry.Insets(45, 43, 0, 43));


        exchangeSection.getChildren().addAll(leftSection, separatorWrapper, rightSection);
        innerFrame.getChildren().addAll(exchangeSection);

//        Comparison section
        HBox middleSection = new HBox();
        middleSection.setPrefSize(733, 40);
        middleSection.setAlignment(Pos.CENTER_LEFT);

        HBox comparisonSection = createCurrencyComparisonSection(currencyRates);
        comparisonSection.setMinWidth(559);

        Button toggleButton = new Button("Show Statistics");
        toggleButton.getStyleClass().add("custom-button");
        toggleButton.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/currencyApp/assets/Style/Button.css")).toExternalForm());
        toggleButton.setMinWidth(174);


        middleSection.getChildren().addAll(comparisonSection, toggleButton  );
        layout.getChildren().add(middleSection);



        // Scene
        Scene scene = new Scene(layout);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // shows the exchange rates of specific currencies (USD, CNY, EUR, SAR) compared to MAD
    private HBox createCurrencyComparisonSection(List<Currency> currencyRates) {
        HBox comparisonContainer = new HBox(10);
//        comparisonContainer.setPadding(new javafx.geometry.Insets(10, 0, 0, 0));
        comparisonContainer.setAlignment(Pos.TOP_LEFT);

        // Currencies to compare against MAD
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
//                "-fx-background-color: #2A2A2A;" +
//                        "-fx-background-radius: 8;" +
                        "-fx-padding: 0 10 3 10;" +
                        "-fx-min-width: 94;"
        );

        // Currency pair label (e.g., "USD / MAD")
        HBox pairLabelContainer = new HBox(0);

        Label MainCurrency = new Label(" / MAD");
        MainCurrency.setStyle("-fx-text-fill: #B2B2B2; -fx-font-weight: bold; -fx-font-size: 12;");

        Label pairLabel = new Label(fromCurrency);
        pairLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 12;");

        pairLabelContainer.getChildren().addAll(pairLabel, MainCurrency);

        // Rate value label
        Label rateLabel = new Label();
        rateLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 12;");

        try {
            double fromRate = getRateForCurrency(currencyRates, fromCurrency);
            double toRate = getRateForCurrency(currencyRates, toCurrency);
            double rate = fromRate / toRate;
            rateLabel.setText(String.format("%.4f", rate));
        } catch (Exception e) {
            rateLabel.setText("N/A");
        }

        container.getChildren().addAll(pairLabelContainer, rateLabel);
        return container;
    }


    private void calculateAndDisplay(TextField amountField, ComboBoxElement fromBox, ComboBoxElement toBox, List<Currency> currencyRates, Label resultLabel) {
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

            // Convert from 'fromCode' to base, then to 'toCode'
            double baseAmount = amount / fromRate;
            double converted = baseAmount * toRate;

            resultLabel.setText(String.format("%.2f", converted));

        } catch (NumberFormatException e) {
            resultLabel.setText("");
        } catch (Exception e) {
            resultLabel.setText("Error: " + e.getMessage());
        }
    }

    private double getRateForCurrency(List<Currency> currencyList, String code) {
        for (Currency c : currencyList) {
            if (c.getCode().equals(code)) {
                double rate = c.getRate();

                return c.getRate();
            }
        }
        throw new IllegalArgumentException("Rate not found for " + code);
    }


    /**
     * Configure primary stage properties
     */
    private void setupPrimaryStage(Stage primaryStage) {
        primaryStage.setTitle(Config.get("appName"));
        primaryStage.initStyle(StageStyle.TRANSPARENT);
    }

    /**
     * Create the main application layout
     */
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

    /**
     * Add window drag support to the layout
     */
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

    /**
     * Create the inner frame containing application content
     */
    private VBox createInnerFrame() {
        VBox innerFrame = new VBox(10);
        innerFrame.setPrefSize(733, 268);
        innerFrame.setStyle(
                "-fx-background-color: #1E1E1E;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 17;"
        );

        // Add title to inner frame
        Label title = new Label("FXFlip - Foreign Exchange");
        title.setStyle("-fx-text-fill: white;");
        title.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 26));
        innerFrame.getChildren().add(title);

        return innerFrame;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

