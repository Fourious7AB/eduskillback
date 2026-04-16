package com.example.eduskill.service;


import com.example.eduskill.dto.EmployeeSalesByNameDTO;
import com.example.eduskill.dto.SalesHistoryDTO;
import com.example.eduskill.dto.SalesmanListDTO;
import com.example.eduskill.dto.SalesmanStatsDTO;
import com.example.eduskill.dto.dashboard.DirectorDashboardResponse;
import com.example.eduskill.dto.dashboard.SalesDashboardResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DashboardService {

    DirectorDashboardResponse directorDashboard();

    DirectorDashboardResponse directorDashboardByDays(int days);

    SalesDashboardResponse salesmanDashboard(String employeeCode);

    List<SalesmanListDTO> getAllSalesmans();

    SalesmanStatsDTO getSalesmanStats(String referralCode);

    Page<SalesHistoryDTO> getSalesHistory(String referralCode, int page);
}