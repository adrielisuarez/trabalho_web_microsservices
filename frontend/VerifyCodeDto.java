package com.example.secrest.dto;

public class VerifyCodeDto {

    private String email;
    private String code;  // era "codigo" - corrigido para bater com o frontend

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
