package com.example.secrest.controller;

import com.example.secrest.dto.RecoveryJwtTokenDto;
import com.example.secrest.dto.RequestCodeDto;
import com.example.secrest.dto.VerifyCodeDto;
import com.example.secrest.service.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/request-code")
    public ResponseEntity<String> requestCode(@RequestBody RequestCodeDto dto) {
        authService.requestCode(dto.getEmail());
        return ResponseEntity.ok("Código enviado para o e-mail informado.");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeDto dto) {
        // authService.verifyCode agora deve retornar o token JWT se o código for válido
        // e lançar uma exceção (ou retornar null) se inválido
        String token = authService.verifyCode(dto.getEmail(), dto.getCode());

        if (token != null) {
            return ResponseEntity.ok(new RecoveryJwtTokenDto(token));
        }

        return ResponseEntity.badRequest().body("Código inválido ou expirado");
    }
}
