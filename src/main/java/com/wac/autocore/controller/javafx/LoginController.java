package com.wac.autocore.controller.javafx;

import com.wac.autocore.controller.util.LanguageManager;
import com.wac.autocore.exception.DatabaseException;
import com.wac.autocore.model.enums.UserRole;
import com.wac.autocore.security.AuthUser;
import com.wac.autocore.security.SecurityContext;
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
import java.util.Set;
import java.util.stream.Collectors;

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
        SecurityContext.clear();
        passwordField.clear();
        try {
            Authentication authentication =
                    loginService.login(username, password);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            SecurityContext.setCurrentUser(toAuthUser(authentication));
            loadRootView(
                    "/com/wac/autocore/controller/view/MainView.fxml", loginButton.getScene());

        } catch (BadCredentialsException e) {
            messageLabel.setText(getString("login.invalid"));

        } catch (DatabaseException e) {
            messageLabel.setText(getString(e.getMessage()));

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            SecurityContext.clear();
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

    // Pilot: Spring Security sköter fortfarande inloggningen, rollerna speglas till SecurityContext för SecurityProxy
    private AuthUser toAuthUser(Authentication authentication) {
        Set<UserRole> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> UserRole.valueOf(authority.substring("ROLE_".length())))
                .collect(Collectors.toSet());
        return new AuthUser(authentication.getName(), roles);
    }
}
