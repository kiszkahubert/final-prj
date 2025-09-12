package com.kiszka.prj.controllers;

import com.kiszka.prj.DTOs.ParentDTO;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.services.AuthenticationService;
import com.kiszka.prj.services.ChildAccessTokenService;
import com.kiszka.prj.services.JWTService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;
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
    private final ChildAccessTokenService childAccessTokenService;
    public AuthenticationController(JWTService jwtService,
                                    AuthenticationService authenticationService,
                                    ChildAccessTokenService childAccessTokenService
    ) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.childAccessTokenService = childAccessTokenService;
    }
    @PostMapping("/signup")
    public ResponseEntity<Parent> register(@Valid @RequestBody ParentDTO parentDTO) {
        authenticationService.signup(parentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
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
    //TODO HERE I SHOULD RETURN A LOT OF INFORMATIONS TO INIT THE VIEW IN ANDROID APP
    @PostMapping("/pin")
    public ResponseEntity<LoginResponse> authenticatePin(@RequestBody String pin){
        System.out.println("RECIEVED REQUEST: " + pin);
        var tokenOptional = childAccessTokenService.getTokenForPin(pin);
        if(tokenOptional.isPresent()){
            Parent parent = tokenOptional.get().getParent();
            String jwtToken = jwtService.generatePermanentToken(parent);
            LoginResponse loginResponse = new LoginResponse()
                    .setToken(jwtToken)
                    .setExpiresIn(Long.MAX_VALUE);
            return ResponseEntity.ok(loginResponse);
        } else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}

@Getter
@Setter
@Accessors(chain = true)
class LoginResponse {
    private String token;
    private long expiresIn;
}