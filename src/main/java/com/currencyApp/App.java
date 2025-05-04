package com.currencyApp;

import com.currencyApp.config.Config;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class App extends Application {

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

        // Create currency switching interface
//        HBox currencySwitching = createCurrencySwitchingInterface();
//        innerFrame.getChildren().add(currencySwitching);

        // Scene
        Scene scene = new Scene(layout);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.show();
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
        innerFrame.setPrefSize(699, 244);
        innerFrame.setStyle(
                "-fx-background-color: #141414;" +
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
}
