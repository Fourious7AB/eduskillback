package com.example.eduskill.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesHistoryDTO {

    private String studentName;
    private String studentEmail;
    private String courseName;

    private Integer amount;
    private LocalDateTime saleDate;
}
