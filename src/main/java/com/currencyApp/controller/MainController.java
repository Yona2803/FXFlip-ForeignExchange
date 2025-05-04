package com.currencyApp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class MainController {

    @FXML
    private ListView<String> currencyList;

    @FXML
    public void initialize() {
        // This method is automatically called after FXML is loaded
        currencyList.getItems().addAll("USD", "EUR", "MAD", "SAR");
    }
}
