package com.example.emailservice.controller;

import com.example.emailservice.dto.EmailRequestDto;
import com.example.emailservice.dto.EmailResponseDto;
import com.example.emailservice.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/emails")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping
    public ResponseEntity<EmailResponseDto> sendEmail(@RequestBody EmailRequestDto request) {
        var emailMessage = emailService.sendEmail(request);
        EmailResponseDto response = new EmailResponseDto(emailMessage.getId(), emailMessage.getStatus().name());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailResponseDto> getStatus(@PathVariable Long id) {
        var status = emailService.getEmailStatus(id);
        return ResponseEntity.ok(new EmailResponseDto(id, status.name()));
    }
}
