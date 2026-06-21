package com.example.secrest.dto;

import com.example.secrest.enums.RoleName;

public record UpdateProfileDto(
    String name,
    RoleName role
) {}
