package com.example.eduskill.service.impl;

import com.example.eduskill.dto.EmployeeSalesByNameDTO;
import com.example.eduskill.entity.User;
import com.example.eduskill.repository.EnrollmentRepository;
import com.example.eduskill.repository.UserRepository;
import com.example.eduskill.service.EmployeeSalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeSalesServiceImpl implements EmployeeSalesService {

    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public EmployeeSalesByNameDTO getSalesByEmployeeName(String name) {

        // 🔍 Find user by name (LIKE search)
        List<User> users = userRepository.searchByName(name);

        if (users.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        // 👉 Take first match (or you can improve later)
        User user = users.get(0);
        String employeeCode = user.getEmployeeCode();

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startOfWeek = now.minusDays(7);

        LocalDateTime startOfMonth = now.withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        // ✅ SALES
        Long totalSales = safe(
                enrollmentRepository.salesByEmployee(employeeCode)
        );

        Long weeklySales = safe(
                enrollmentRepository.weeklySalesByEmployee(employeeCode, startOfWeek)
        );

        Long monthlySales = safe(
                enrollmentRepository.monthlySalesByEmployee(employeeCode, startOfMonth, endOfMonth)
        );

        // ✅ REVENUE
        Long totalRevenue = safe(
                enrollmentRepository.totalAmountByEmployee(employeeCode)
        );

        Long weeklyRevenue = safe(
                enrollmentRepository.weeklyRevenueByEmployee(employeeCode, startOfWeek)
        );

        Long monthlyRevenue = safe(
                enrollmentRepository.monthlyRevenueByEmployee(employeeCode, startOfMonth, endOfMonth)
        );

        return EmployeeSalesByNameDTO.builder()
                .name(user.getName())
                .employeeCode(employeeCode)

                .totalSales(totalSales)
                .weeklySales(weeklySales)
                .monthlySales(monthlySales)

                .totalRevenue(totalRevenue)
                .weeklyRevenue(weeklyRevenue)
                .monthlyRevenue(monthlyRevenue)

                .build();
    }

    private Long safe(Long value) {
        return value == null ? 0L : value;
    }
}