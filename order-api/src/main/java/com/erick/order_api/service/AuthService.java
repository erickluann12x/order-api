package com.erick.order_api.service;

import com.erick.order_api.dto.LoginRequestDTO;
import com.erick.order_api.dto.LoginResponseDTO;
import com.erick.order_api.entity.Roles;
import com.erick.order_api.entity.User;
import com.erick.order_api.repository.UserRepository;
import com.erick.order_api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDTO login(LoginRequestDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.username(),
                        dto.password()
                )
        );

        String token = jwtUtil.buildToken(dto.username());

        return new LoginResponseDTO(token);
    }
    //
    public String register(LoginRequestDTO dto) {
        User user = User.builder()
                .username(dto.username())
                .password(passwordEncoder.encode(dto.password()))
                .roles(Roles.SELLER)
                .build();

        userRepository.save(user);

        return "Usuário criado com sucesso";

    }
}
