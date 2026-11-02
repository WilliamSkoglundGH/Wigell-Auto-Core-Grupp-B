package com.wac.autocore.service;

import com.wac.autocore.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);
    private final AuthenticationManager authenticationManager;

    public LoginService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }


    public Authentication login(String username, String password) {
        UsernamePasswordAuthenticationToken loginAttempt = new UsernamePasswordAuthenticationToken(username, password);

        try {
            Authentication authentication = authenticationManager.authenticate(loginAttempt);

            logger.info("Method: login | Authentication successful | Username={}", authentication.getName());
            return authentication;

        } catch (BadCredentialsException e) {
            logger.warn("Method: login | Authentication failed | Invalid credentials | Username={}", username);
            throw e;
        } catch (InternalAuthenticationServiceException e) {
            if (e.getCause() instanceof DataAccessException) {
                logger.error("Method: login | Database error ", e);
                throw new DatabaseException("common.error.database");
            }
            throw e;
        }
    }
}
