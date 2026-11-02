package com.wac.autocore.controller.javafx;

import com.wac.autocore.controller.util.LanguageManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

public abstract class OverController {

    private static final Logger logger = LoggerFactory.getLogger(OverController.class);

    protected UserMessages messages;
    protected ApplicationContext applicationContext;

    public void setMessages(UserMessages messages) {
        this.messages = messages;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    // ---------------------------------------------------------
    // GEMENSAM VYLADDARE FÖR ALLA SUB-CONTROLLERS
    // ---------------------------------------------------------
    protected <T> T loadCenterView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath), LanguageManager.getBundle());

            if (applicationContext != null) {
                loader.setControllerFactory(applicationContext::getBean);
            }

            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof OverController) {
                OverController oc = (OverController) controller;
                oc.setMessages(messages);
                oc.setApplicationContext(applicationContext);
            }

            BorderPane mainLayout = findMainLayout();
            if (mainLayout != null) {
                mainLayout.setCenter(view);
            }

            return (T) controller;

        } catch (IOException e) {
            logger.error("Method: loadCenterView | Could not load view | Path={}", fxmlPath, e);
            if (messages != null) {
                messages.showError(getString("common.error.open_view"));
            }
            return null;
        }
    }

    // ---------------------------------------------------------
    // HITTA HUVUDFÖNSTRET (BORDERPANE) FÖR JAVA 8
    // ---------------------------------------------------------
    protected BorderPane findMainLayout() {
        // Använd den officiella, publika metoden för att hämta öppna fönster i JavaFX
        for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
            if (window.isShowing() && window.getScene() != null && window.getScene().getRoot() != null) {
                Parent root = window.getScene().getRoot();
                if (root instanceof BorderPane) {
                    return (BorderPane) root;
                }
                BorderPane found = searchBorderPaneRecursive(root);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
    protected BorderPane searchBorderPaneRecursive(Parent parent) {
        if (parent instanceof BorderPane) {
            return (BorderPane) parent;
        }
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof Parent) {
                BorderPane found = searchBorderPaneRecursive((Parent) child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /**
     * Hämtar översatt text från den aktuella ResourceBundlen via LanguageManager.
     * Om nyckeln saknas returneras nyckeln själv som reserv.
     */
    protected String getString(String key) {
        try {
            if (LanguageManager.getBundle() != null && LanguageManager.getBundle().containsKey(key)) {
                return LanguageManager.getBundle().getString(key);
            }
        } catch (Exception e) {
            logger.error("Method: getString | Could not read translation | Key={}", key, e);
        }
        return key; // Fallback så att inte appen kraschar om nyckeln saknas
    }

    protected void loadRootView(String fxmlPath, Scene scene)
            throws IOException {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(fxmlPath),
                LanguageManager.getBundle()
        );

        loader.setControllerFactory(applicationContext::getBean);

        Parent view = loader.load();

        scene.setRoot(view);
    }


}