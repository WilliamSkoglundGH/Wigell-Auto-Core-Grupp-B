package com.wac.autocore;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URL;

@SpringBootApplication
public class App extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // Startar Spring Boot-kontexten i bakgrunden när JavaFX startar upp
        springContext = SpringApplication.run(App.class);
    }

    @Override
    public void start(Stage stage) throws Exception {
        URL fxmlLocation = App.class.getResource("/com/wac/autocore/gui/view/MainView.fxml");

        if (fxmlLocation == null) {
            throw new IllegalArgumentException("Could not find the FXML file in the application resources!");
        }

        // Kopplar ihop FXMLLoader med Spring så att Dependency Injection fungerar i controllers
        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        loader.setControllerFactory(springContext::getBean);
        Parent root = loader.load();

        Scene scene = new Scene(root, 800, 600);

        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.ENTER
                    && scene.getFocusOwner() instanceof Button) {

                Button button = (Button) scene.getFocusOwner();
                button.fire();
            }
        });

        scene.getStylesheets().add(getClass().getResource("/com/wac/autocore/gui/css/styleguide.css").toExternalForm());
        stage.setTitle("Wigell Auto Core - Workshop Management System");
        stage.setScene(scene);
        stage.show();
        root.requestFocus();
    }

    @Override
    public void stop() {
        // Stänger ner Spring ordentligt när fönstret stängs av användaren
        if (springContext != null) {
            springContext.close();
        }
    }
}