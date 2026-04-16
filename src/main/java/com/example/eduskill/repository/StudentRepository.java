package com.example.eduskill.repository;

import com.example.eduskill.entity.Student;
import com.example.eduskill.projection.DirectorDashboardProjection;
import com.example.eduskill.projection.StudentStatsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student,Long> {
    Optional<Student> findByEmail(String email);
    Long countByEnableTrue();

    Long countByReferralCode(String referralCode);

    Optional<Student> findByStudentCode(String studentCode);





    @Query("""
SELECT
    COUNT(s) AS totalStudents,
    COALESCE(SUM(CASE WHEN s.enable = true THEN 1 ELSE 0 END),0) AS activeStudents,
    COALESCE(SUM(CASE WHEN s.createdAt >= :startOfMonth THEN 1 ELSE 0 END),0) AS newStudents
FROM Student s
""")
    StudentStatsProjection getStudentStats(@Param("startOfMonth") LocalDateTime startOfMonth);

}