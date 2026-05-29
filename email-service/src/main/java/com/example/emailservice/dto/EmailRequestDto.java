package com.example.emailservice.dto;

public record EmailRequestDto(
        String to,
        String subject,
        String body
) {}