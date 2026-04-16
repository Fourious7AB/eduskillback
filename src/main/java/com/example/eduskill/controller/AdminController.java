package com.example.eduskill.controller;

import com.example.eduskill.dto.EmployeeSalesByNameDTO;
import com.example.eduskill.dto.SalesHistoryDTO;
import com.example.eduskill.dto.SalesmanListDTO;
import com.example.eduskill.dto.SalesmanStatsDTO;
import com.example.eduskill.dto.dashboard.DirectorDashboardResponse;
import com.example.eduskill.service.AdminService;
import com.example.eduskill.service.DashboardService;
import com.example.eduskill.service.EmployeeSalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DashboardService dashboardService;
    private final AdminService adminService;
    private final EmployeeSalesService employeeSalesService;

    // ALL
    @GetMapping("/director")
    public DirectorDashboardResponse directorDashboard() {
        return dashboardService.directorDashboard();
    }

    // 7 DAYS
    @GetMapping("/director/7days")
    public DirectorDashboardResponse last7Days() {
        return dashboardService.directorDashboardByDays(7);
    }

    // 30 DAYS
    @GetMapping("/director/30days")
    public DirectorDashboardResponse last30Days() {
        return dashboardService.directorDashboardByDays(30);
    }

    // 1 YEAR
    @GetMapping("/director/1year")
    public DirectorDashboardResponse last1Year() {
        return dashboardService.directorDashboardByDays(365);
    }

    @PutMapping("/disable-user/{employeeCode}")
    public String disableUser(@PathVariable String employeeCode){
        adminService.disableUser(employeeCode);
        return "User disabled successfully";
    }

    @PutMapping("/enable-user/{employeeCode}")
    public String enableUser(@PathVariable String employeeCode){
        adminService.enableUser(employeeCode);
        return "User enabled successfully";
    }

    @PutMapping("/reactivate-student/{studentCode}")
    public String reactivateStudent(@PathVariable String studentCode){
        adminService.reactivateStudent(studentCode);
        return "Student reactivated successfully";
    }

    @GetMapping("/salesmans")
    public List<SalesmanListDTO> getSalesmans() {
        return dashboardService.getAllSalesmans();
    }

    @GetMapping("/salesman/{referralCode}")
    public SalesmanStatsDTO getSalesmanStats(@PathVariable String referralCode) {
        return dashboardService.getSalesmanStats(referralCode);
    }

    @GetMapping("/salesman/{referralCode}/history")
    public Page<SalesHistoryDTO> getSalesHistory(
            @PathVariable String referralCode,
            @RequestParam(defaultValue = "0") int page
    ) {
        return dashboardService.getSalesHistory(referralCode, page);
    }



    @GetMapping("/employee-sales")
    public EmployeeSalesByNameDTO getEmployeeSales(@RequestParam String name) {
        return employeeSalesService.getSalesByEmployeeName(name);
    }


    @DeleteMapping("/{userCode}")
    public void deleteUser(@PathVariable String userCode){
        adminService.deleteUser(userCode);
    }
}