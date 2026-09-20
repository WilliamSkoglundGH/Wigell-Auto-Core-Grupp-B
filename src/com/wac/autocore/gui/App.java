package com.wac.autocore.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URL;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        URL fxmlLocation = App.class.getResource("/com/wac/autocore/gui/view/MainView.fxml");

        if (fxmlLocation == null) {
            throw new IllegalArgumentException("Could not find the FXML file in the application resources!");
        }

        Parent root = FXMLLoader.load(fxmlLocation);

        Scene scene = new Scene(root, 800, 600);

        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.ENTER
                    && scene.getFocusOwner() instanceof Button) {

                Button button = (Button) scene.getFocusOwner();
                button.fire();
            }
        });

        scene.getStylesheets().add(getClass().getResource("/com/wac/autocore/gui/css/styleguide.css").toExternalForm());
        //scene.getStylesheets().add(getClass().getResource("styleguide.css").toExternalForm());
        stage.setTitle("Wigell Auto Core - Workshop Management System");
        stage.setScene(scene);
        stage.show();
        root.requestFocus();
    }
}