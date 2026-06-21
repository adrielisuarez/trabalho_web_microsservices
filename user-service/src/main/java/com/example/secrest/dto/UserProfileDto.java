package com.example.secrest.dto;

import com.example.secrest.enums.RoleName;
import java.util.List;

public record UserProfileDto(
    Long id,
    String email,
    String name,
    List<RoleName> roles
) {}
