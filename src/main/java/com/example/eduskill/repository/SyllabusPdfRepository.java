package com.example.eduskill.repository;

import com.example.eduskill.entity.SyllabusPdf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SyllabusPdfRepository extends JpaRepository<SyllabusPdf, Long> {
    List<SyllabusPdf> findByCourseId(Long courseId);
}
