package com.example.emailservice.consumer;
import com.example.emailservice.dto.EmailRecordDto;
import com.example.emailservice.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
@Component
public class EmailConsumer {
    private final EmailService emailService;
    public EmailConsumer(EmailService emailService) { this.emailService = emailService; }
    @RabbitListener(queues = "${broker.queue.email.name}")
    public void listenEmailQueue(EmailRecordDto emailRecordDto) {
        var result = emailService.sendEmail(emailRecordDto);
        System.out.println("E-mail processado para: " + emailRecordDto.emailTo() + " | Status: " + result.getStatus());
    }
}

