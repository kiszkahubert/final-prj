package com.kiszka.prj.controllers;

import com.kiszka.prj.DTOs.UserDTO;
import com.kiszka.prj.entities.User;
import com.kiszka.prj.services.AuthenticationService;
import com.kiszka.prj.services.JWTService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JWTService jwtService;
    private final AuthenticationService authenticationService;
    public AuthenticationController(JWTService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }
    @PostMapping("/signup")
    public ResponseEntity<User> register(@Valid @RequestBody UserDTO userDTO) {
        User registeredUser = authenticationService.signup(userDTO);
        return ResponseEntity.ok(registeredUser);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@Valid @RequestBody UserDTO userDTO) {
        User authenticatedUser = authenticationService.authenticate(userDTO);
        String jwtToken = jwtService.generateToken(authenticatedUser);
        LoginResponse loginResponse = new LoginResponse()
                .setToken(jwtToken)
                .setExpiresIn(jwtService.getJwtExpiration());
        return ResponseEntity.ok(loginResponse);
    }
}

@Getter
@Setter
@Accessors(chain = true)
class LoginResponse {
    private String token;
    private long expiresIn;
}