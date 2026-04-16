package com.example.eduskill.service;

import com.example.eduskill.entity.Course;
import com.example.eduskill.entity.Payment;
import com.example.eduskill.entity.Student;

public interface InvoiceService {

    byte[] generateInvoice(Student student,
                           Course course,
                           Payment payment);
}
