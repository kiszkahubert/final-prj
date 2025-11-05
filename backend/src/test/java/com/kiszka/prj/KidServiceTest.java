package com.kiszka.prj;

import com.kiszka.prj.entities.Kid;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.repositories.KidRepository;
import com.kiszka.prj.repositories.ParentRepository;
import com.kiszka.prj.services.KidService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class KidServiceTest {
    @Mock
    KidRepository kidRepository;
    @Mock
    ParentRepository parentRepository;
    @InjectMocks
    KidService kidService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void addKid_shouldSaveKidAndAttachToParent() {
        // given
        Parent parent = new Parent();
        parent.setId(1);
        Kid kid = new Kid();
        kid.setName("Hubert");
        Kid savedKid = new Kid();
        savedKid.setId(10);
        savedKid.setName("Hubert");
        when(parentRepository.findById(1)).thenReturn(Optional.of(parent));
        when(kidRepository.save(kid)).thenReturn(savedKid);
        // when
        Kid result = kidService.addKid(kid, 1);
        // then
        assertEquals(savedKid, result);
        assertTrue(parent.getKids().contains(savedKid));
        assertTrue(savedKid.getParents().contains(parent));
        verify(parentRepository).save(parent);
        verify(kidRepository).save(kid);
    }

    @Test
    void addKid_shouldThrowWhenParentNotFound() {
        // given
        when(parentRepository.findById(1)).thenReturn(Optional.empty());
        Kid kid = new Kid();
        // then
        assertThrows(RuntimeException.class, () -> kidService.addKid(kid, 1));
        verify(kidRepository, never()).save(any());
    }
    @Test
    void deleteKid_shouldCallDeleteWhenExists() {
        // given
        when(kidRepository.existsById(1)).thenReturn(true);
        // when
        kidService.deleteKid(1);
        // then
        verify(kidRepository).deleteKidNative(1);
    }
    @Test
    void deleteKid_shouldNotDeleteWhenNotExists() {
        // given
        when(kidRepository.existsById(1)).thenReturn(false);
        // when
        kidService.deleteKid(1);
        // then
        verify(kidRepository, never()).deleteKidNative(anyInt());
    }
    @Test
    void updateKid_shouldUpdateFieldsAndSave() {
        // given
        Kid existing = new Kid();
        existing.setId(1);
        existing.setName("Old");
        existing.setBirthDate(LocalDate.of(2025, 11, 5));
        Kid updated = new Kid();
        updated.setName("New");
        updated.setBirthDate(LocalDate.of(2025, 11, 5));
        when(kidRepository.findById(1)).thenReturn(Optional.of(existing));
        when(kidRepository.save(existing)).thenReturn(existing);
        // when
        Kid result = kidService.updateKid(1, updated);
        // then
        assertEquals("New", result.getName());
        assertEquals(LocalDate.of(2025, 11, 5), result.getBirthDate());
        verify(kidRepository).save(existing);
    }

    @Test
    void updateKid_shouldThrowWhenKidNotFound() {
        // given
        when(kidRepository.findById(1)).thenReturn(Optional.empty());
        Kid updated = new Kid();
        // when
        assertThrows(RuntimeException.class, () -> kidService.updateKid(1, updated));
        verify(kidRepository, never()).save(any());
    }
}
