package com.example.secrest.dto;

import com.example.secrest.enums.RoleName;

public record CreateUserDto(
    String email,
    String password,
    RoleName role
) {}