package com.erick.order_api.controller;

import com.erick.order_api.dto.LoginRequestDTO;
import com.erick.order_api.dto.LoginResponseDTO;
import com.erick.order_api.entity.Roles;
import com.erick.order_api.entity.User;
import com.erick.order_api.repository.UserRepository;
import com.erick.order_api.security.JwtUtil;
import com.erick.order_api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto){
        return ResponseEntity.ok(authService.login(dto));
    }


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody LoginRequestDTO dto){
        return ResponseEntity.ok(authService.register(dto));
    }
}
