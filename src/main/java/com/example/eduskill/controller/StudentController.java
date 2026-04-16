package com.example.eduskill.controller;


import com.example.eduskill.dto.request.StudentRegistrationRequest;
import com.example.eduskill.dto.response.OrderResponse;
import com.example.eduskill.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping("/register")
    public OrderResponse register(@RequestBody StudentRegistrationRequest request) throws Exception {

        return studentService.registerStudent(request);

    }

}