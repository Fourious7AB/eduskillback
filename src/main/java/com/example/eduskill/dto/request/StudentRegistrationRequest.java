package com.example.eduskill.dto.request;

import lombok.Data;
@Data
public class StudentRegistrationRequest {

    private String studentName;
    private String phone;
    private String whatsapp;
    private String email;
    private String courseName;
    private String studentClass;
    private String referralCode;

}