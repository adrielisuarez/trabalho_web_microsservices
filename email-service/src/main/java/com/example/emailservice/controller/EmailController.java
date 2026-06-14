package com.example.emailservice.controller;

import com.example.emailservice.models.EmailModel;
import com.example.emailservice.repository.EmailRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/emails")
public class EmailController {

    private final EmailRepository emailRepository;

    public EmailController(EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    @GetMapping("/{emailId}")
    public ResponseEntity<?> getEmailStatus(@PathVariable UUID emailId) {
        Optional<EmailModel> email = emailRepository.findById(emailId);
        return email.isPresent() 
            ? ResponseEntity.ok(email.get()) 
            : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<?> getAllEmails() {
        return ResponseEntity.ok(emailRepository.findAll());
    }
}
