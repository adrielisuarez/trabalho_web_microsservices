package com.example.secrest.service;

import com.example.secrest.dto.EmailDto;
import com.example.secrest.entity.Role;
import com.example.secrest.entity.User;
import com.example.secrest.enums.RoleName;
import com.example.secrest.producer.UserProducer;
import com.example.secrest.repository.RoleRepository;
import com.example.secrest.repository.UserRepository;
import com.example.secrest.security.service.JwtTokenService;
import com.example.secrest.security.service.UserDetailsImpl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CodigoCacheService codigoCacheService;
    private final UserProducer userProducer;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            CodigoCacheService codigoCacheService,
            UserProducer userProducer,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.codigoCacheService = codigoCacheService;
        this.userProducer = userProducer;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    public void requestCode(String email) {
        // Busca usuário existente ou cria um temporário
        userRepository.findByEmail(email)
                .orElseGet(() -> criarUsuarioTemporario(email));

        // Gera código de 6 dígitos com zero à esquerda
        String codigo = String.format("%06d", new Random().nextInt(999999));

        codigoCacheService.salvarCodigo(email, codigo);

        EmailDto emailDto = new EmailDto();
        emailDto.setEmailTo(email);
        emailDto.setSubject("Seu código de acesso");
        emailDto.setText("Seu código de verificação é: " + codigo
                + "\n\nEle expira em 5 minutos.");

        userProducer.publishEmail(emailDto);
    }

    /**
     * Valida o código e, se correto, gera e retorna um token JWT.
     * Retorna null se o código for inválido ou expirado.
     */
    public String verifyCode(String email, String codigo) {
        boolean valido = codigoCacheService.validarCodigo(email, codigo);

        if (!valido) {
            return null;
        }

        // Remove o código após uso para evitar reutilização
        codigoCacheService.removerCodigo(email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + email));

        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        return jwtTokenService.generateToken(userDetails);
    }

    private User criarUsuarioTemporario(String email) {
        Role role = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_CUSTOMER).build()));

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode("TEMP_" + System.currentTimeMillis()))
                .roles(new java.util.ArrayList<>(List.of(role)))
                .build();

        return userRepository.save(user);
    }
}