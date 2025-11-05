package com.kiszka.prj;

import com.kiszka.prj.DTOs.ChildAccessTokenDTO;
import com.kiszka.prj.entities.ChildAccessToken;
import com.kiszka.prj.entities.Kid;
import com.kiszka.prj.entities.Parent;
import com.kiszka.prj.repositories.ChildAccessTokenRepository;
import com.kiszka.prj.services.ChildAccessTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ChildAccessTokenServiceTest {
    @Mock
    private ChildAccessTokenRepository childAccessTokenRepository;
    @InjectMocks
    private ChildAccessTokenService childAccessTokenService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generateTokenForParent_shouldSaveToken() {
        // given
        Parent parent = new Parent().setId(1);
        Kid kid = new Kid();
        kid.setId(10);
        // when
        childAccessTokenService.generateTokenForParent(parent, kid);
        // then
        ArgumentCaptor<ChildAccessToken> captor = ArgumentCaptor.forClass(ChildAccessToken.class);
        verify(childAccessTokenRepository).save(captor.capture());
        ChildAccessToken saved = captor.getValue();
        assertNotNull(saved.getPin());
        assertEquals(8, saved.getPin().length());
        assertNotNull(saved.getQrHash());
        assertFalse(saved.getQrHash().isBlank());
        assertEquals(parent, saved.getParent());
        assertEquals(kid, saved.getKid());
    }
    @Test
    void getTokenForPin_shouldCallRepoAndReturnToken() {
        // given
        ChildAccessToken token = new ChildAccessToken();
        when(childAccessTokenRepository.findByPin("1234")).thenReturn(Optional.of(token));
        // when
        Optional<ChildAccessToken> result = childAccessTokenService.getTokenForPin("1234");
        // then
        verify(childAccessTokenRepository).findByPin("1234");
        assertTrue(result.isPresent());
        assertEquals(token, result.get());
    }
    @Test
    void getTokenForQrHash_shouldCallRepoAndReturnToken() {
        // given
        ChildAccessToken token = new ChildAccessToken();
        when(childAccessTokenRepository.findByQrHash("abcd")).thenReturn(Optional.of(token));
        // when
        Optional<ChildAccessToken> result = childAccessTokenService.getTokenForQrHash("abcd");
        // then
        verify(childAccessTokenRepository).findByQrHash("abcd");
        assertTrue(result.isPresent());
        assertEquals(token, result.get());
    }
    @Test
    void getTokensForParent_shouldMapToDtoList() {
        // given
        Parent p = new Parent().setId(1);
        Kid kid = new Kid();
        kid.setId(10);
        ChildAccessToken token = new ChildAccessToken()
                .setPin("PIN123")
                .setQrHash("HASH123")
                .setParent(p)
                .setKid(kid);

        when(childAccessTokenRepository.findAllByParent_Id(1)).thenReturn(List.of(token));
        // when
        List<ChildAccessTokenDTO> result = childAccessTokenService.getTokensForParent(1);
        // then
        assertEquals(1, result.size());
        assertEquals("PIN123", result.get(0).getPin());
        assertEquals("HASH123", result.get(0).getQrHash());
        assertEquals(10, result.get(0).getKidId());
    }
}
