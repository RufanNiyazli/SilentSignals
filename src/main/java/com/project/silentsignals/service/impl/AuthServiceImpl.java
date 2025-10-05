package com.project.silentsignals.service.impl;

import com.project.silentsignals.dto.AuthResponse;
import com.project.silentsignals.dto.LoginRequest;
import com.project.silentsignals.dto.RefreshTokenRequest;
import com.project.silentsignals.dto.RegisterRequest;
import com.project.silentsignals.entity.RefreshToken;
import com.project.silentsignals.entity.User;
import com.project.silentsignals.repository.RefreshTokenRepository;
import com.project.silentsignals.repository.UserRepository;
import com.project.silentsignals.security.JwtService;
import com.project.silentsignals.service.IAuthService;
import com.project.silentsignals.service.IRefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor

public class AuthServiceImpl implements IAuthService {
    private final PasswordEncoder passwordEncoder;
    private final IRefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public void register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Register another email . This email exist:" + registerRequest.getEmail());
        }
        User user = User.builder()
                .email(registerRequest.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))

                .build();
        userRepository.save(user);


    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        } catch (AuthenticationException e) {
            throw new RuntimeException(e);
        }
        User user = userRepository.findUserByEmail(loginRequest.getEmail()).orElseThrow(() -> new RuntimeException("EMail not found!"));
        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        refreshTokenRepository.save(refreshToken);


        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    @Override
    public AuthResponse refreshAccessToken(RefreshTokenRequest refreshTokenRequest) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenRequest);
        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateToken(user);

        return new AuthResponse(newAccessToken, refreshToken.getToken());
    }
}
