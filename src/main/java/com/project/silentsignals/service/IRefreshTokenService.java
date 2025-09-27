package com.project.silentsignals.service;

import com.project.silentsignals.dto.RefreshTokenRequest;
import com.project.silentsignals.entity.RefreshToken;
import com.project.silentsignals.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface IRefreshTokenService {
    public RefreshToken createRefreshToken(User user);

    public RefreshToken validateRefreshToken(RefreshTokenRequest request);
}
