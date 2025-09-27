package com.project.silentsignals.service.impl;

import com.project.silentsignals.dto.RefreshTokenRequest;
import com.project.silentsignals.entity.RefreshToken;
import com.project.silentsignals.entity.User;
import com.project.silentsignals.repository.RefreshTokenRepository;
import com.project.silentsignals.service.IRefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements IRefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Override

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshToken.setExpiredAt(LocalDateTime.now().plusHours(1));
        refreshToken.setUser(user);

        return refreshToken;
    }

    @Override
    public RefreshToken validateRefreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getToken());
        if (refreshToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new RuntimeException("Refresh Token expired:");
        }


        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token is revoked.");
        }

        return refreshToken;
    }
}
