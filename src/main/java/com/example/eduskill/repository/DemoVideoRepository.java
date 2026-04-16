package com.example.eduskill.repository;

import com.example.eduskill.entity.DemoVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DemoVideoRepository extends JpaRepository<DemoVideo, Long> {
    List<DemoVideo> findByCourseId(Long courseId);
}
