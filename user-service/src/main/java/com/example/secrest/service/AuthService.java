package com.example.secrest.service;

import com.example.secrest.dto.EmailDto;
import com.example.secrest.entity.Role;
import com.example.secrest.entity.User;
import com.example.secrest.enums.RoleName;
import com.example.secrest.producer.UserProducer;
import com.example.secrest.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CodigoCacheService codigoCacheService;
    private final UserProducer userProducer;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            CodigoCacheService codigoCacheService,
            UserProducer userProducer,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.codigoCacheService = codigoCacheService;
        this.userProducer = userProducer;
        this.passwordEncoder = passwordEncoder;
    }

    public void requestCode(String email) {

        User user = userRepository
                .findByEmail(email)
                .orElseGet(() -> criarUsuarioTemporario(email));

        String codigo = String.format(
                "%06d",
                new Random().nextInt(999999)
        );

        codigoCacheService.salvarCodigo(email, codigo);

        EmailDto emailDto = new EmailDto();

        emailDto.setEmailTo(email);
        emailDto.setSubject("Seu código de acesso");
        emailDto.setText("Seu código é: " + codigo);

        userProducer.publishEmail(emailDto);
    }

    public boolean verifyCode(
            String email,
            String codigo) {

        return codigoCacheService
                .validarCodigo(email, codigo);
    }

    private User criarUsuarioTemporario(
            String email) {

        User user = User.builder()
                .email(email)
                .password(
                        passwordEncoder.encode(
                                "TEMP_" + System.currentTimeMillis()))
                .roles(
                        List.of(
                                Role.builder()
                                        .name(RoleName.ROLE_CUSTOMER)
                                        .build()))
                .build();

        return userRepository.save(user);
    }
}