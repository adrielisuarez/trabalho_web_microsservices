package com.example.emailservice.services;
import com.example.emailservice.dtos.EmailRecordDto;
import com.example.emailservice.models.EmailModel;
import com.example.emailservice.models.StatusEmail;
import com.example.emailservice.repositories.EmailRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
@Service
public class EmailService {
    @Autowired private EmailRepository emailRepository;
    @Autowired private JavaMailSender emailSender;
    @Value("${spring.mail.username}") private String emailFrom;
    public EmailModel sendEmail(EmailRecordDto emailRecordDto) {
        EmailModel emailModel = new EmailModel();
        BeanUtils.copyProperties(emailRecordDto, emailModel);
        emailModel.setSendDateEmail(LocalDateTime.now());
        emailModel.setEmailFrom(emailFrom);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailModel.getEmailTo());
            message.setSubject(emailModel.getSubject());
            message.setText(emailModel.getText());
            emailSender.send(message);
            emailModel.setStatus(StatusEmail.SENT);
        } catch (MailException e) {
            emailModel.setStatus(StatusEmail.ERROR);
        } finally {
            emailRepository.save(emailModel);
        }
        return emailModel;
    }
}
