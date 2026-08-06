package com.erick.order_api.controller;

import com.erick.order_api.dto.AccessTokenResponse;
import com.erick.order_api.dto.LoginRequestDTO;
import com.erick.order_api.dto.RegisterRequest;
import com.erick.order_api.entity.User;
import com.erick.order_api.repository.UserRepository;
import com.erick.order_api.security.JwtUtil;
import com.erick.order_api.service.AuthService;
import com.erick.order_api.service.RefreshCookieService;
import com.erick.order_api.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticação")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager
            authenticationManager;

    private final UserRepository
            userRepository;

    private final JwtUtil jwtUtil;

    private final RefreshTokenService
            refreshTokenService;

    private final RefreshCookieService
            cookieService;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse>
    login(
            @Valid
            @RequestBody
            LoginRequestDTO request
    ) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.username(),
                                request.password()
                        )
                );

        User user = userRepository
                .findByUsername(request.username())
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Credenciais inválidas"
                        )
                );

        String accessToken =
                jwtUtil.generateToken(
                        authentication
                );

        var refreshToken =
                refreshTokenService.createSession(
                        user
                );

        ResponseCookie cookie =
                cookieService.create(
                        refreshToken.rawToken()
                );

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        cookie.toString()
                )
                .body(
                        new AccessTokenResponse(
                                accessToken
                        )
                );
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse>
    refresh(
            @CookieValue(
                    name =
                            RefreshCookieService.COOKIE_NAME,
                    required = false
            )
            String rawRefreshToken
    ) {
        var rotated =
                refreshTokenService.rotate(
                        rawRefreshToken
                );

        String accessToken =
                jwtUtil.generateToken(
                        rotated.user()
                                .getUsername()
                );

        ResponseCookie cookie =
                cookieService.create(
                        rotated.rawToken()
                );

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        cookie.toString()
                )
                .body(
                        new AccessTokenResponse(
                                accessToken
                        )
                );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(
                    name =
                            RefreshCookieService.COOKIE_NAME,
                    required = false
            )
            String rawRefreshToken
    ) {
        refreshTokenService.revoke(
                rawRefreshToken
        );

        return ResponseEntity
                .noContent()
                .header(
                        HttpHeaders.SET_COOKIE,
                        cookieService
                                .clear()
                                .toString()
                )
                .build();
    }
    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}
