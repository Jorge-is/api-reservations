package com.edteam.reservations.controller;

import com.edteam.reservations.dto.LoginRequest;
import com.edteam.reservations.enums.APIError;
import com.edteam.reservations.exception.ReservationException;
import com.edteam.reservations.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Operations to get JWT tokens")
public class AuthController {

    private final JwtUtil jwtUtil;

    @Value("${auth.demo.username:admin}")
    private String demoUsername;

    @Value("${auth.demo.password:admin123}")
    private String demoPassword;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/token")
    @Operation(summary = "Get JWT token", description = "Authenticate with username and password to obtain a Bearer token.")
    public ResponseEntity<Map<String, String>> getToken(@RequestBody @Valid LoginRequest request) {
        if (!demoUsername.equals(request.getUsername()) || !demoPassword.equals(request.getPassword())) {
            throw new ReservationException(APIError.INVALID_CREDENTIALS);
        }
        String token = jwtUtil.generateToken(request.getUsername());
        return ResponseEntity.ok(Map.of("token", token));
    }
}
