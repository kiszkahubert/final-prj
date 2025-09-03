package com.kiszka.prj.controllers;

import com.kiszka.prj.DTOs.ParentDTO;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.services.AuthenticationService;
import com.kiszka.prj.services.JWTService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<Parent> register(@Valid @RequestBody ParentDTO parentDTO) {
        Parent registeredParent = authenticationService.signup(parentDTO);
        return ResponseEntity.ok(registeredParent);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@Valid @RequestBody ParentDTO parentDTO) {
        Parent authenticatedParent = authenticationService.authenticate(parentDTO);
        String jwtToken = jwtService.generateToken(authenticatedParent);
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