package com.example.emailservice.service;

import com.example.emailservice.dto.EmailRequestDto;
import com.example.emailservice.enums.EmailStatus;
import com.example.emailservice.entity.EmailMessage;
import com.example.emailservice.repository.EmailRepository;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final EmailRepository emailRepository;

    public EmailService(EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    public EmailMessage sendEmail(EmailRequestDto request) {
        EmailMessage message = EmailMessage.builder()
                .toAddress(request.to())
                .subject(request.subject())
                .body(request.body())
                .status(EmailStatus.QUEUED)
                .build();
        return emailRepository.save(message);
    }

    public EmailStatus getEmailStatus(Long id) {
        return emailRepository.findById(id)
                .map(EmailMessage::getStatus)
                .orElse(EmailStatus.FAILED);
    }
}
