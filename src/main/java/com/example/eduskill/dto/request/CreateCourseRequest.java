package com.example.eduskill.dto.request;

import lombok.Data;

@Data
public class CreateCourseRequest {

    private String courseName;

    private String courseClass;

    private String title;

    private Integer joiningFee;

    private Integer subscriptionFee;

    private String image;   // ✅ CRITICAL FIX

    private Boolean enabled;

    private Boolean completed;
}