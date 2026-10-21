package com.wac.autocore.controller.javafx;

import com.wac.autocore.service.LoginService;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

@Controller
@Scope("prototype")
public class LoginController extends OverController {

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
        messageLabel.setText("");

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Ange användarnamn och lösenord.");
            return;
        }

        SecurityContextHolder.clearContext();
        passwordField.clear();

        //fixa loggning
        try {
            Authentication authentication =
                    loginService.login(username, password);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            messageLabel.setText("Inloggningen lyckades.");

            loadRootView(
                    "/com/wac/autocore/controller/view/MainView.fxml", loginButton.getScene());

        } catch (BadCredentialsException e) {
            messageLabel.setText("Felaktigt användarnamn eller lösenord.");

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            messageLabel.setText("Det gick inte att logga in. Försök igen senare.");
        }
    }
}
