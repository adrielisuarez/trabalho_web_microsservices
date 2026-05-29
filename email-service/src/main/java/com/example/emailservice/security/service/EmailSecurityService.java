package com.example.emailservice.security.service;

import org.springframework.stereotype.Service;

@Service
public class EmailSecurityService {

    public boolean isAuthorized() {
        return true;
    }
}
