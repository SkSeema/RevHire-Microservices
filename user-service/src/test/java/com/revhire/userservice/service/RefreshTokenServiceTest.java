package com.revhire.userservice.service;

import com.revhire.userservice.model.RefreshToken;
import com.revhire.userservice.model.User;
import com.revhire.userservice.repository.RefreshTokenRepository;
import com.revhire.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void verifyExpirationDeletesExpiredTokenAndThrows() {
        User user = new User();
        user.setId(7L);

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> refreshTokenService.verifyExpiration(token)
        );

        assertEquals("Refresh token was expired. Please make a new signin request", exception.getMessage());
        verify(refreshTokenRepository).delete(token);
    }

    @Test
    void verifyExpirationReturnsActiveToken() {
        User user = new User();
        user.setId(9L);

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        token.setRevoked(false);

        RefreshToken result = refreshTokenService.verifyExpiration(token);

        assertSame(token, result);
        verify(refreshTokenRepository, never()).delete(token);
    }
}
