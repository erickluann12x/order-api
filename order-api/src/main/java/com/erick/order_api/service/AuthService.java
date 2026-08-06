package com.erick.order_api.service;

import com.erick.order_api.dto.LoginRequestDTO;
import com.erick.order_api.dto.LoginResponseDTO;
import com.erick.order_api.dto.RegisterRequest;
import com.erick.order_api.entity.Roles;
import com.erick.order_api.entity.User;
import com.erick.order_api.repository.UserRepository;
import com.erick.order_api.security.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

        String token = jwtUtil.generateToken(dto.username());

        return new LoginResponseDTO(token);
    }
    //
    @Transactional
    public void register(RegisterRequest request) {
        String username = request
                .getUsername()
                .trim();

        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esse usuário já está cadastrado"
            );
        }

        User user = new User();

        user.setUsername(username);

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        userRepository.save(user);
    }
}
