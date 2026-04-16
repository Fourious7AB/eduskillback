package com.example.eduskill.repository;

import com.example.eduskill.entity.Course;
import com.example.eduskill.entity.Enrollment;
import com.example.eduskill.entity.Student;
import com.example.eduskill.projection.SalesByReferralProjection;
import com.example.eduskill.projection.SalesLeaderboardProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // ================= BASIC =================
    List<Enrollment> findByCourse(Course course);

    Optional<Enrollment> findByStudentAndCourse(Student student, Course course);

    Optional<Enrollment> findTopByStudentOrderByEnrolledDateDesc(Student student);

    Optional<Enrollment> findByStudent_Email(String email);

    List<Enrollment> findByStudent(Student student);

    // ================= SALES =================
    @Query("SELECT COUNT(e) FROM Enrollment e")
    Long totalSales();

    @Query("""
        SELECT COUNT(e) FROM Enrollment e
        WHERE e.enrolledDate >= :start
    """)
    Long salesFromDate(@Param("start") LocalDateTime start);

    @Query("""
        SELECT COUNT(e) FROM Enrollment e
        WHERE e.enrolledDate >= :startOfMonth
        AND e.enrolledDate < :startOfNextMonth
    """)
    Long monthlySales(@Param("startOfMonth") LocalDateTime startOfMonth,
                      @Param("startOfNextMonth") LocalDateTime startOfNextMonth);

    // ================= TOP COURSES =================
    @Query("""
        SELECT e.course.courseName AS courseName, COUNT(e) AS totalSales
        FROM Enrollment e
        GROUP BY e.course.courseName
        ORDER BY COUNT(e) DESC
    """)
    List<Object[]> topCourses(Pageable pageable);

    // ================= OLD EMPLOYEE (OPTIONAL - REMOVE IF NOT USED) =================
    @Query("""
        SELECT e.employeeCode AS employeeCode, COUNT(e) AS totalSales
        FROM Enrollment e
        GROUP BY e.employeeCode
        ORDER BY COUNT(e) DESC
    """)
    List<SalesLeaderboardProjection> topSalesman(Pageable pageable);

    // ================= ✅ NEW CORRECT LEADERBOARD =================


    @Query("""
    SELECT COALESCE(SUM(e.course.joiningFee),0)
    FROM Enrollment e
    JOIN e.student s
    WHERE s.referralCode = :referralCode
""")
    Long totalAmountByReferral(@Param("referralCode") String referralCode);

    // 🔥 MONTHLY TOP REFERRAL SALES
    @Query("""
        SELECT s.referralCode AS referralCode, COUNT(e) AS totalSales
        FROM Enrollment e
        JOIN e.student s
        WHERE e.enrolledDate BETWEEN :start AND :end
        GROUP BY s.referralCode
        ORDER BY COUNT(e) DESC
    """)
    List<SalesByReferralProjection> topMonthlySalesByReferral(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    // 🔥 SINGLE REFERRAL MONTHLY SALES (for mapping)
    @Query("""
        SELECT COUNT(e)
        FROM Enrollment e
        JOIN e.student s
        WHERE s.referralCode = :referralCode
        AND e.enrolledDate BETWEEN :start AND :end
    """)
    Long monthlySalesByReferral(
            @Param("referralCode") String referralCode,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
    SELECT COUNT(e)
    FROM Enrollment e
    JOIN e.student s
    WHERE s.referralCode = :referralCode
    AND e.enrolledDate >= :start
""")
    Long salesFromDateByReferral(@Param("referralCode") String referralCode,
                                 @Param("start") LocalDateTime start);

    @Query("""
    SELECT e
    FROM Enrollment e
    JOIN e.student s
    WHERE s.referralCode = :referralCode
    ORDER BY e.enrolledDate DESC
""")
    Page<Enrollment> findSalesByReferral(
            @Param("referralCode") String referralCode,
            Pageable pageable
    );



    @Query("""
SELECT s.referralCode AS referralCode, COUNT(e) AS totalSales
FROM Enrollment e
JOIN e.student s
GROUP BY s.referralCode
ORDER BY COUNT(e) DESC
""")
    List<SalesByReferralProjection> topSalesByReferral(Pageable pageable);

    @Query("""
    SELECT COALESCE(SUM(e.course.joiningFee),0)
    FROM Enrollment e
    WHERE e.employeeCode = :employeeCode
    AND e.enrolledDate >= :start
""")
    Long amountFromDate(@Param("employeeCode") String employeeCode,
                        @Param("start") LocalDateTime start);


    @Query("""
    SELECT e
    FROM Enrollment e
    WHERE e.employeeCode = :employeeCode
    ORDER BY e.enrolledDate DESC
""")
    Page<Enrollment> findSalesByEmployee(
            @Param("employeeCode") String employeeCode,
            Pageable pageable
    );


    @Query("""
    SELECT COUNT(e)
    FROM Enrollment e
    WHERE e.employeeCode = :employeeCode
""")
    Long salesByEmployee(@Param("employeeCode") String employeeCode);

    @Query("""
    SELECT COALESCE(SUM(e.course.joiningFee),0)
    FROM Enrollment e
    WHERE e.employeeCode = :employeeCode
""")
    Long totalAmountByEmployee(@Param("employeeCode") String employeeCode);



    // WEEKLY SALES
    @Query("""
    SELECT COUNT(e)
    FROM Enrollment e
    WHERE e.employeeCode = :employeeCode
    AND e.enrolledDate >= :start
""")
    Long weeklySalesByEmployee(@Param("employeeCode") String employeeCode,
                               @Param("start") LocalDateTime start);


    // MONTHLY SALES
    @Query("""
    SELECT COUNT(e)
    FROM Enrollment e
    WHERE e.employeeCode = :employeeCode
    AND e.enrolledDate BETWEEN :start AND :end
""")
    Long monthlySalesByEmployee(@Param("employeeCode") String employeeCode,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);


    // WEEKLY REVENUE
    @Query("""
    SELECT COALESCE(SUM(e.course.joiningFee),0)
    FROM Enrollment e
    WHERE e.employeeCode = :employeeCode
    AND e.enrolledDate >= :start
""")
    Long weeklyRevenueByEmployee(@Param("employeeCode") String employeeCode,
                                 @Param("start") LocalDateTime start);


    // MONTHLY REVENUE
    @Query("""
    SELECT COALESCE(SUM(e.course.joiningFee),0)
    FROM Enrollment e
    WHERE e.employeeCode = :employeeCode
    AND e.enrolledDate BETWEEN :start AND :end
""")
    Long monthlyRevenueByEmployee(@Param("employeeCode") String employeeCode,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);




}