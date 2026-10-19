package com.babalegends.service;

import com.babalegends.dto.LoginRequestDto;
import com.babalegends.dto.RegisterRequestDto;
import com.babalegends.dto.UserResponseDto;
import com.babalegends.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public String login(LoginRequestDto  loginRequestDto) {
        String normalizedEmail = loginRequestDto.email().trim().toLowerCase(Locale.ROOT);
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(normalizedEmail, loginRequestDto.password()));
        return jwtTokenProvider.generateJwtToken(authentication);
    }

    public UserResponseDto register(RegisterRequestDto registerRequestDto) {
        return userService.userToUserResponseDto(userService.createUser(
                registerRequestDto.name(),
                registerRequestDto.email(),
                registerRequestDto.password()
        ));
    }
}
