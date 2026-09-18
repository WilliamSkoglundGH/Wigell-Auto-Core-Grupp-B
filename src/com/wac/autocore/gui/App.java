package com.wac.autocore.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        URL fxmlLocation = App.class.getResource("/com/wac/autocore/gui/view/MainView.fxml");

        if (fxmlLocation == null) {
            throw new IllegalArgumentException("Hittade inte FXML-filen under resources!");
        }

        Parent root = FXMLLoader.load(fxmlLocation);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/com/wac/autocore/gui/css/styleguide.css").toExternalForm());
        //scene.getStylesheets().add(getClass().getResource("styleguide.css").toExternalForm());
        stage.setTitle("Wigell Auto Core - Verkstadssystem");
        stage.setScene(scene);
        stage.show();
        root.requestFocus();
    }
}