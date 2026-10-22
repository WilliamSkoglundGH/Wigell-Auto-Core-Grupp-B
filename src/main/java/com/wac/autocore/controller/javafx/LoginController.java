package com.wac.autocore.controller.javafx;

import com.wac.autocore.controller.util.LanguageManager;
import com.wac.autocore.service.LoginService;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
import java.util.Locale;

@Controller
@Scope("prototype")
public class LoginController extends OverController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final LoginService loginService;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label messageLabel;

    public LoginController(LoginService loginService, ApplicationContext applicationContext){
        this.loginService = loginService;
        this.applicationContext = applicationContext;
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText(getString("login.required"));
            return;
        }

        SecurityContextHolder.clearContext();
        passwordField.clear();
        try {
            Authentication authentication =
                    loginService.login(username, password);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            loadRootView(
                    "/com/wac/autocore/controller/view/MainView.fxml", loginButton.getScene());

        } catch (BadCredentialsException e) {
            messageLabel.setText(getString("login.invalid"));

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            logger.error("Method: handleLogin | Unexpected error during login", e);
            messageLabel.setText(getString("login.error"));
        }
    }

    @FXML
    private void handleToggleLanguage() {
        Locale current = LanguageManager.getBundle().getLocale();

        Locale next = current.getLanguage().equals("sv")
                ? new Locale("en")
                : new Locale("sv");

        LanguageManager.setLocale(next);

        try {
            loadRootView(
                    "/com/wac/autocore/controller/view/LoginView.fxml",
                    loginButton.getScene()
            );
        } catch (IOException e) {
            logger.error("Method: handleToggleLanguage | Could not reload login view", e);
            messageLabel.setText(getString("login.error"));
        }
    }
}
