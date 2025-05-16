package com.currencyApp.ui;

import com.currencyApp.model.Currency;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.Objects;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.util.Comparator;

public class ComboBoxElement extends HBox {
    private final ComboBox<Currency> comboBox;
    private final Label nameLabel;
    private final StringBuilder keyBuffer = new StringBuilder();
    private long lastKeyPressTime = 0;
    private static final long KEY_TIMEOUT = 1000; // 1 second timeout for keystrokes
    private int lastMatchIndex = -1;

    public ComboBoxElement(List<Currency> currencyList) {
        this(currencyList, null);
    }

    public ComboBoxElement(List<Currency> currencyList, Currency defaultCurrency) {
        super();

        // Set a fixed width for the container
        this.setPrefWidth(309);
        this.setMaxWidth(309);
        this.setMinWidth(309);

        // Create a sorted copy of the currency list to avoid modifying the original
        List<Currency> sortedList = new java.util.ArrayList<>(currencyList);
        sortedList.sort(Comparator.comparing(Currency::getName));

        // Create and configure the ComboBox
        comboBox = new ComboBox<>();
        comboBox.getItems().addAll(sortedList);
        comboBox.setPrefWidth(86);
        comboBox.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/currencyApp/assets/Style/ComboBox.css")).toExternalForm());

        // Create the label that will show the selected currency name
        nameLabel = new Label("");
        nameLabel.setMaxWidth(206);
        nameLabel.setStyle("-fx-text-fill: #B8B8B8;");
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 20));
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Set default currency if provided
        if (defaultCurrency != null) {
            comboBox.getSelectionModel().select(defaultCurrency);
            nameLabel.setText(defaultCurrency.getName());
        }

        // Handle selection changes
        comboBox.setOnAction(e -> {
            Currency selected = comboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                nameLabel.setText(selected.getName());
            }
        });

        // Enhanced keyboard navigation
        setupKeyboardNavigation();

        // Configure the layout
        this.setSpacing(10);
        this.setAlignment(Pos.CENTER_LEFT); // Align items left
        this.getChildren().addAll(nameLabel, comboBox); // Label on left, ComboBox on right
    }

    private void setupKeyboardNavigation() {
        comboBox.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            // Only handle if ComboBox is focused
            if (!comboBox.isFocused()) {
                return;
            }

            // If the dropdown is not showing and the user presses Space, Enter, or Down, let the default behavior handle
            if (!comboBox.isShowing() &&
                    (event.getCode() == KeyCode.SPACE ||
                            event.getCode() == KeyCode.ENTER ||
                            event.getCode() == KeyCode.DOWN)) {
                return;
            }

            // Handle letter/number keys for quick selection
            if (event.getCode().isLetterKey() || event.getCode().isDigitKey()) {
                long currentTime = System.currentTimeMillis();

                if (currentTime - lastKeyPressTime > KEY_TIMEOUT) {
                    keyBuffer.setLength(0);
                    lastMatchIndex = -1; // Reset cycling
                }

                lastKeyPressTime = currentTime;

                keyBuffer.append(event.getText().toUpperCase());

                boolean found = selectItemStartingWith(keyBuffer.toString());

                if (!found && !keyBuffer.isEmpty()) {
                    char latestChar = keyBuffer.charAt(keyBuffer.length() - 1);
                    selectItemStartingWith(String.valueOf(latestChar));
                }

                if (!comboBox.isShowing()) {
                    comboBox.show();
                }

                event.consume();
            }
        });
    }


    /**
     * Selects the first item in the ComboBox that starts with the given prefix
     * @return true if an item was found and selected, false otherwise
     */
    private boolean selectItemStartingWith(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return false;
        }

        String upperPrefix = prefix.toUpperCase();
        List<Currency> matchingItems = new java.util.ArrayList<>();

        // Find all matching items
        for (Currency currency : comboBox.getItems()) {
            if (currency.getName().toUpperCase().startsWith(upperPrefix)) {
                matchingItems.add(currency);
            }
        }

        if (matchingItems.isEmpty()) {
            return false;
        }

        // Cycle through matches
        int nextIndex = 0;

        // If the last match exists in this match list, go to the next
        if (lastMatchIndex != -1) {
            Currency lastMatchedCurrency = comboBox.getItems().get(lastMatchIndex);
            int currentMatchPos = matchingItems.indexOf(lastMatchedCurrency);
            if (currentMatchPos != -1) {
                nextIndex = (currentMatchPos + 1) % matchingItems.size();
            }
        }

        Currency toSelect = matchingItems.get(nextIndex);
        comboBox.getSelectionModel().select(toSelect);
        nameLabel.setText(toSelect.getName());

        // Update lastMatchIndex
        lastMatchIndex = comboBox.getItems().indexOf(toSelect);

        return true;
    }

    public ComboBox<Currency> getComboBox() {
        return comboBox;
    }

    public Label getNameLabel() {
        return nameLabel;
    }

    public String getSelectedItem() {
        Currency selected = comboBox.getValue();
        return selected != null ? selected.getCode() : "";
    }

}