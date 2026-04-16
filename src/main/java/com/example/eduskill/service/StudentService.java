package com.example.eduskill.service;

import com.example.eduskill.dto.request.StudentRegistrationRequest;
import com.example.eduskill.dto.response.OrderResponse;

public interface StudentService {

    OrderResponse registerStudent(StudentRegistrationRequest request) throws Exception;

}