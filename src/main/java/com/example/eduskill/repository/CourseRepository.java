package com.example.eduskill.repository;

import com.example.eduskill.entity.Course;
import com.example.eduskill.entity.DemoVideo;
import com.example.eduskill.entity.SyllabusPdf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseName(String courseName);
    long count();


    // ✅ Return only non-deleted courses
    List<Course> findByDeletedFalse();
}