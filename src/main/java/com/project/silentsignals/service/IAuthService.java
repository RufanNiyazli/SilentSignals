package com.project.silentsignals.service;

import com.project.silentsignals.dto.AuthResponse;
import com.project.silentsignals.dto.LoginRequest;
import com.project.silentsignals.dto.RefreshTokenRequest;
import com.project.silentsignals.dto.RegisterRequest;

public interface IAuthService {
    public void register(RegisterRequest registerRequest);

    public AuthResponse login(LoginRequest loginRequest);

    public AuthResponse refreshAccessToken(RefreshTokenRequest refreshTokenRequest);

}
