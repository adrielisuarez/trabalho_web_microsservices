package com.example.secrest.controller;

import com.example.secrest.dto.CreateUserDto;
import com.example.secrest.dto.LoginUserDto;
import com.example.secrest.dto.RecoveryJwtTokenDto;
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
}