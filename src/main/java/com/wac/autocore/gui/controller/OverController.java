package com.wac.autocore.gui.controller;

import com.wac.autocore.gui.util.LanguageManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

public abstract class OverController {

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
    protected void loadCenterView(String fxmlPath) {
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
            } else {
                if (messages != null) {
                    messages.showError("Could not find main layout to load view.");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            if (messages != null) {
                messages.showError("Could not open the requested view. Please try again.");
            }
        }
    }

    // ---------------------------------------------------------
    // HITTA HUVUDFÖNSTRET (BORDERPANE) FÖR JAVA 8
    // ---------------------------------------------------------
    protected BorderPane findMainLayout() {
        // Eftersom vi inte har Window.getWindows() i Java 8 på samma sätt,
        // letar vi via com.sun.javafx.stage.StageHelper eller fönstrets öppna noder.
        // Enklast och säkrast i Java 8 är att hämta alla Stage-objekt:
        for (javafx.stage.Window window : com.sun.javafx.stage.StageHelper.getStages()) {
            if (window.getScene() != null && window.getScene().getRoot() != null) {
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
            // Logga eller ignorera om bundle saknas
        }
        return key; // Fallback så att inte appen kraschar om nyckeln saknas
    }
}