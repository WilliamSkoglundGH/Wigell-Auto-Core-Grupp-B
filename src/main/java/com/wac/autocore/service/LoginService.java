package com.wac.autocore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);
    private final AuthenticationManager authenticationManager;

    public LoginService(AuthenticationManager authenticationManager){
        this.authenticationManager = authenticationManager;
    }


    public Authentication login(String username, String password){
        UsernamePasswordAuthenticationToken loginAttempt = new UsernamePasswordAuthenticationToken(username, password);

        try{
            Authentication authentication = authenticationManager.authenticate(loginAttempt);

            logger.info("Method: login | Authentication successful | Username={}", authentication.getName());
            return authentication;

        }catch(BadCredentialsException e){
            logger.warn("Method: login | Authentication failed | Invalid credentials");
            throw e;

        } catch (Exception e) {
            logger.error("Method: login | Authentication could not be completed", e);
            throw e;
        }
    }
}
