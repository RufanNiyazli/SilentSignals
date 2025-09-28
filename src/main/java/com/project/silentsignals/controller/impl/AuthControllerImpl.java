package com.project.silentsignals.controller.impl;

import com.project.silentsignals.controller.IAuthController;
import com.project.silentsignals.dto.AuthResponse;
import com.project.silentsignals.dto.LoginRequest;
import com.project.silentsignals.dto.RefreshTokenRequest;
import com.project.silentsignals.dto.RegisterRequest;
import com.project.silentsignals.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthControllerImpl implements IAuthController {
    private final IAuthService authService;

    @PostMapping("/public/register")
    @Override
    public void register(@RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);

    }

    @Override
    @PostMapping("/public/login")
    public AuthResponse login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @Override
    @PostMapping("/public/refresh-accessToken")
    public AuthResponse refreshAccessToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return authService.refreshAccessToken(refreshTokenRequest);
    }
}
