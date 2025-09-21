package com.kiszka.prj.controllers;

import com.kiszka.prj.DTOs.LoginResponse;
import com.kiszka.prj.DTOs.ParentDTO;
import com.kiszka.prj.DTOs.PinLoginResponseDTO;
import com.kiszka.prj.DTOs.TaskDTO;
import com.kiszka.prj.components.KidMapper;
import com.kiszka.prj.components.TaskMapper;
import com.kiszka.prj.entities.Kid;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.services.AuthenticationService;
import com.kiszka.prj.services.ChildAccessTokenService;
import com.kiszka.prj.services.JWTService;
import com.kiszka.prj.services.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JWTService jwtService;
    private final AuthenticationService authenticationService;
    private final ChildAccessTokenService childAccessTokenService;
    private final TaskService taskService;
    public AuthenticationController(JWTService jwtService,
                                    AuthenticationService authenticationService,
                                    ChildAccessTokenService childAccessTokenService,
                                    TaskService taskService
    ) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.childAccessTokenService = childAccessTokenService;
        this.taskService = taskService;
    }
    @PostMapping("/signup")
    public ResponseEntity<?> register(@Valid @RequestBody ParentDTO parentDTO) {
        try {
            authenticationService.signup(parentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
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
    @PostMapping("/pin")
    public ResponseEntity<PinLoginResponseDTO> authenticatePin(@RequestBody String pin){
        var tokenOptional = childAccessTokenService.getTokenForPin(pin);
        if(tokenOptional.isPresent()){
            var token = tokenOptional.get();
            Parent parent = token.getParent();
            Kid kid = token.getKid();
            String jwtToken = jwtService.generateTokenForKid(kid, parent);
            List<TaskDTO> taskDTOs = taskService.getTasksForKid(kid.getId()).stream()
                    .map(TaskMapper::toDTO)
                    .toList();
            PinLoginResponseDTO response = new PinLoginResponseDTO(
                    jwtToken,
                    24L * 60 * 60 * 1000 * 365,
                    KidMapper.toDTO(kid),
                    taskDTOs
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
