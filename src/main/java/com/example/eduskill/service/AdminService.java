package com.example.eduskill.service;

public interface AdminService {

    void disableUser(String employeeCode);

    void enableUser(String employeeCode);

    void deleteUser(String userId);

    void reactivateStudent(String studentCode);

}