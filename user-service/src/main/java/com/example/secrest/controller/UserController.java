package com.example.secrest.controller;

import com.example.secrest.dto.CreateUserDto;
import com.example.secrest.dto.LoginUserDto;
import com.example.secrest.dto.RecoveryJwtTokenDto;
import com.example.secrest.dto.UpdateProfileDto;
import com.example.secrest.dto.UserProfileDto;
import com.example.secrest.entity.User;
import org.springframework.security.core.Authentication;
import com.example.secrest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping
    public ResponseEntity<Void> createUser(@RequestBody CreateUserDto dto) {
        userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @PostMapping("/login")
    public ResponseEntity<RecoveryJwtTokenDto> login(
        @RequestBody LoginUserDto dto) {
            RecoveryJwtTokenDto token = userService.authenticateUser(dto);
            return ResponseEntity.ok(token);
        }
        
        @GetMapping("/test")
        public ResponseEntity<String> test() {
            return ResponseEntity.ok("Autenticado com sucesso!");
        }
        
        @GetMapping("/test/customer")
        @PreAuthorize("hasRole('CUSTOMER')")
        public ResponseEntity<String> customerTest() {
            return ResponseEntity.ok("Acesso de CUSTOMER autorizado!");
        }
        @GetMapping("/test/administrator")
        @PreAuthorize("hasRole('ADMINISTRATOR')")
        public ResponseEntity<String> adminTest() {
            return ResponseEntity.ok("Acesso de ADMINISTRATOR autorizado!");
        }

        @PostMapping("/update-profile")
        public ResponseEntity<UserProfileDto> updateProfile(Authentication authentication, @RequestBody UpdateProfileDto dto) {
            String email = authentication.getName();
            User updated = userService.updateProfile(email, dto);
            var roles = updated.getRoles().stream().map(r -> r.getName()).toList();
            UserProfileDto profile = new UserProfileDto(updated.getId(), updated.getEmail(), updated.getName(), roles);
            return ResponseEntity.ok(profile);
        }

        @GetMapping("/me")
        public ResponseEntity<UserProfileDto> me(Authentication authentication) {
            String email = authentication.getName();
            User user = userService.getByEmail(email);
            var roles = user.getRoles().stream().map(r -> r.getName()).toList();
            UserProfileDto profile = new UserProfileDto(user.getId(), user.getEmail(), user.getName(), roles);
            return ResponseEntity.ok(profile);
        }
}