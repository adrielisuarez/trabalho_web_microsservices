package com.example.secrest.service;

import com.example.secrest.dto.CreateUserDto;
import com.example.secrest.dto.LoginUserDto;
import com.example.secrest.dto.RecoveryJwtTokenDto;
import com.example.secrest.dto.UpdateProfileDto;
import com.example.secrest.entity.Role;
import com.example.secrest.entity.User;
import com.example.secrest.repository.RoleRepository;
import com.example.secrest.repository.UserRepository;
import com.example.secrest.security.service.JwtTokenService;
import com.example.secrest.security.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service 
public class UserService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public RecoveryJwtTokenDto authenticateUser(LoginUserDto loginDto) {
        var authToken = new UsernamePasswordAuthenticationToken(loginDto.email(),loginDto.password());
        Authentication authentication =authenticationManager.authenticate(authToken);
        UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();
        String token = jwtTokenService.generateToken(userDetails);
        return new RecoveryJwtTokenDto(token);
    }
    
    public void createUser(CreateUserDto createDto) {
        Role role = getOrCreateRole(createDto.role());
        User newUser = User.builder()
            .email(createDto.email())
            .password(passwordEncoder.encode(createDto.password()))
            .roles(new ArrayList<>(List.of(role)))
            .build();
        userRepository.save(newUser);
    }

    public User updateProfile(String email, UpdateProfileDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Role role = getOrCreateRole(dto.role());
        user.setName(dto.name());
        user.setRoles(new ArrayList<>(List.of(role)));
        return userRepository.save(user);
    }

    /**
     * Busca uma Role existente pelo nome ou cria uma nova caso não exista.
     * Evita criar registros duplicados de Role na tabela "roles".
     */
    private Role getOrCreateRole(com.example.secrest.enums.RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}