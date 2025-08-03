package com.learn.splitwise.service;

import com.learn.splitwise.dto.JwtResponse;
import com.learn.splitwise.dto.LoginRequest;
import com.learn.splitwise.dto.SignupRequest;
import com.learn.splitwise.exception.CustomException;
import com.learn.splitwise.model.User;
import com.learn.splitwise.repository.UserRepository;
import com.learn.splitwise.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public JwtResponse register(SignupRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException("Email already registered", HttpStatus.BAD_REQUEST);
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(user.getEmail());
        return JwtResponse.builder()
                .token(token)
                .userId(savedUser.getId())
                .build();
    }

    public JwtResponse login(LoginRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (Exception ex) {
            throw new CustomException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new CustomException("User not found", HttpStatus.NOT_FOUND));

        String token = jwtService.generateToken(request.getEmail());
        return JwtResponse.builder()
                .userId(user.getId())
                .token(token)
                .build();
    }
}
