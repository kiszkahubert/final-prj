package com.kiszka.prj.services;

import com.kiszka.prj.DTOs.ParentDTO;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager
    ){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }
    public Parent signup(ParentDTO input){
        Parent parent = new Parent()
                .setUsername(input.getUsername())
                .setPassword(passwordEncoder.encode(input.getPassword()));
        return userRepository.save(parent);
    }
    public Parent authenticate(ParentDTO input){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getUsername(),
                        input.getPassword()
                )
        );
        return userRepository.findByUsername(input.getUsername()).orElseThrow();
    }
}