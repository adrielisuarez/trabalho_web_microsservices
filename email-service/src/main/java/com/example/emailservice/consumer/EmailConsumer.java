package com.example.emailservice.consumer;

import com.example.emailservice.dto.EmailDto;
import com.example.emailservice.dto.EmailRequestDto;
import com.example.emailservice.service.EmailService;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    private final EmailService emailService;

    public EmailConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "${broker.queue.email.name}")
    public void listenEmailQueue(EmailDto emailDto) {

        EmailRequestDto request =
                new EmailRequestDto(
                        emailDto.getEmailTo(),
                        emailDto.getSubject(),
                        emailDto.getText()
                );

        emailService.sendEmail(request);

        System.out.println(
                "Mensagem recebida para: "
                        + emailDto.getEmailTo());
    }
}