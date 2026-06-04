package com.example.emailservice.dto;

import lombok.Data;

@Data
public class EmailDto {

    private String emailTo;
    private String subject;
    private String text;
}