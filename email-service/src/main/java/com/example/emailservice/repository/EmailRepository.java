package com.example.emailservice.repository;

import com.example.emailservice.entity.EmailMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepository extends JpaRepository<EmailMessage, Long> {
}
