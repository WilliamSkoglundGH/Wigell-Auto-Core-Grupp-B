package com.wac.autocore.security;

import com.wac.autocore.model.User;
import com.wac.autocore.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public JpaUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ANVÄNDS AV SPRING SECURITY: Anropas automatiskt när en användare försöker logga in
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Användaren hittades inte: " + username));

        // 2. Gör om era UserRole-enums till Spring Security-behörigheter (ROLE_ADMIN, ROLE_USER)
        var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());

        // 3. Paketera i ett format Spring Security kräver för att kunna verifiera inloggningen
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // Det krypterade lösenordet från databasen
                authorities
        );
    }
}