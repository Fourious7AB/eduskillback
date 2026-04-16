package com.example.eduskill.service;

import com.example.eduskill.dto.EmployeeSalesByNameDTO;

public interface EmployeeSalesService {

    EmployeeSalesByNameDTO getSalesByEmployeeName(String name);
}