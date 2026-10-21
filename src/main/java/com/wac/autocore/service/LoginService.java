package com.wac.autocore.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final AuthenticationManager authenticationManager;

    public LoginService(AuthenticationManager authenticationManager){
        this.authenticationManager = authenticationManager;
    }


    public Authentication login(String username, String password){
        UsernamePasswordAuthenticationToken loginAttempt = new UsernamePasswordAuthenticationToken(username, password);

        return authenticationManager.authenticate(loginAttempt);

        //Logga här om autentiseringen misslyckades/lyckades?
    }
}
