package com.example.secrest.dto;

public record LoginUserDto(
    String email,
    String password
) {}