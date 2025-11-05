package com.kiszka.prj;

import com.kiszka.prj.entities.Kid;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.repositories.ParentRepository;
import com.kiszka.prj.services.ParentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ParentServiceTest {
    @Mock
    private ParentRepository parentRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private ParentService parentService;
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getKidsByParent_shouldReturnKidsIncludingFromOtherParent() {
        // given
        Parent p1 = new Parent();
        p1.setId(1);
        Parent p2 = new Parent();
        p2.setId(2);
        Kid k1 = new Kid();
        k1.setId(10);
        Kid k2 = new Kid();
        k2.setId(11);
        p1.setKids(Set.of(k1));
        k1.setParents(Set.of(p1, p2));
        p2.setKids(Set.of(k1, k2));
        when(parentRepository.findById(1)).thenReturn(Optional.of(p1));
        // when
        Set<Kid> result = parentService.getKidsByParent(1);
        // then
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Set.of(k1, k2)));
    }
    @Test
    void updateUsername_shouldUpdateWhenValid() {
        // given
        Parent p = new Parent();
        p.setId(1);
        p.setUsername("oldname");
        when(parentRepository.findById(1)).thenReturn(Optional.of(p));
        when(parentRepository.findByUsername("newname")).thenReturn(Optional.empty());
        when(parentRepository.save(any())).thenReturn(p);
        // when
        Parent result = parentService.updateUsername(1, "newname");
        // then
        assertEquals("newname", result.getUsername());
        verify(parentRepository).save(p);
    }
    @Test
    void updateUsername_shouldThrowWhenTooShort() {
        // given
        Parent p = new Parent();
        p.setId(1);
        when(parentRepository.findById(1)).thenReturn(Optional.of(p));
        // then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> parentService.updateUsername(1, "abc"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }
    @Test
    void updateUsername_shouldThrowWhenAlreadyExists() {
        // given
        Parent p = new Parent();
        p.setId(1);
        Parent existing = new Parent();
        existing.setId(2);
        when(parentRepository.findById(1)).thenReturn(Optional.of(p));
        when(parentRepository.findByUsername("takenName")).thenReturn(Optional.of(existing));
        // then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> parentService.updateUsername(1, "takenName"));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }
    @Test
    void updatePassword_shouldUpdateWhenValid() {
        // given
        Parent p = new Parent(); p.setId(1);
        p.setPassword("encodedOldPassword");
        when(parentRepository.findById(1)).thenReturn(Optional.of(p));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(parentRepository.save(any())).thenReturn(p);
        // when
        Parent updated = parentService.updatePassword(1, "oldPassword", "newPassword");
        // then
        assertEquals("encodedNewPassword", updated.getPassword());
        verify(parentRepository).save(p);
    }
    @Test
    void updatePassword_shouldThrowWhenOldIncorrect() {
        // given
        Parent p = new Parent(); p.setId(1);
        p.setPassword("encodedOldPassword");
        when(parentRepository.findById(1)).thenReturn(Optional.of(p));
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);
        // then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> parentService.updatePassword(1, "wrongPassword", "newPassword"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
    @Test
    void deleteParent_shouldThrowWhenNotFound() {
        // given
        when(parentRepository.existsById(1)).thenReturn(false);
        // then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> parentService.deleteParent(1));
        assertEquals("Parent not found", ex.getMessage());
    }
}
