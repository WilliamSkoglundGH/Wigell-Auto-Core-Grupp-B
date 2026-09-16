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
        // Läs in FXML med absolut sökväg från resources-mappen
        URL fxmlLocation = App.class.getResource("/com/wac/autocore/gui/view/MainView.fxml");

        if (fxmlLocation == null) {
            throw new IllegalArgumentException("Hittade inte FXML-filen under resources!");
        }

        Parent root = FXMLLoader.load(fxmlLocation);

        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Wigell Auto Core - Verkstadssystem");
        stage.setScene(scene);
        stage.show();
    }
}