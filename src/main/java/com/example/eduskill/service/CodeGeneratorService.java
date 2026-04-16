package com.example.eduskill.service;

public interface CodeGeneratorService {

    String generateStudentCode();
    String generateEmployeeCode(String rolePrefix);

}