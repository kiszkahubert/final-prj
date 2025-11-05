package com.kiszka.prj;

import com.kiszka.prj.DTOs.ParentDTO;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.repositories.ParentRepository;
import com.kiszka.prj.services.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class AuthenticationServiceTest {
    @Mock
    private ParentRepository parentRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private AuthenticationService authenticationService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void signup_shouldSaveNewUser_whenUserDoesNotExist() {
        // given
        ParentDTO input = new ParentDTO("user","password");
        when(parentRepository.findByUsername("user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        // when
        authenticationService.signup(input);
        // then
        ArgumentCaptor<Parent> parentCaptor = ArgumentCaptor.forClass(Parent.class);
        verify(parentRepository).save(parentCaptor.capture());
        Parent saved = parentCaptor.getValue();
        assertEquals("user", saved.getUsername());
        assertEquals("encodedPassword", saved.getPassword());
    }
    @Test
    void signup_shouldThrow_whenUserAlreadyExists() {
        // given
        ParentDTO input = new ParentDTO("existingUser","password");
        when(parentRepository.findByUsername("existingUser")).thenReturn(Optional.of(new Parent()));
        // then
        assertThrows(RuntimeException.class, () -> authenticationService.signup(input));
        verify(parentRepository, never()).save(any());
    }
    @Test
    void authenticate_shouldReturnUser_whenCredentialsAreValid() {
        // given
        ParentDTO input = new ParentDTO("user","password");
        Parent parent = new Parent().setUsername("user").setPassword("encodedPassword");
        when(parentRepository.findByUsername("user")).thenReturn(Optional.of(parent));
        // when
        Parent result = authenticationService.authenticate(input);
        // then
        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken("user", "password"));
        assertEquals("user", result.getUsername());
    }

    @Test
    void authenticate_shouldThrow_whenUserNotFound() {
        // given
        ParentDTO input = new ParentDTO("user","password");
        when(parentRepository.findByUsername("user")).thenReturn(Optional.empty());
        // then
        assertThrows(RuntimeException.class, () -> authenticationService.authenticate(input));
    }
}
