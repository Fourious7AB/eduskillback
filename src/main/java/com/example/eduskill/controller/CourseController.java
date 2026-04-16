package com.example.eduskill.controller;

import com.example.eduskill.entity.Course;
import com.example.eduskill.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // ✅ CREATE COURSE
    @PostMapping
    public Course createCourse(@RequestBody Course course) {
        return courseService.createCourse(course);
    }

    // ✅ GET ALL COURSES
    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    // ✅ GET COURSE BY ID
    @GetMapping("/{id}")
    public Course getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id);
    }

    // ✅ UPDATE COURSE
    @PutMapping("/{id}")
    public Course updateCourse(@PathVariable Long id,
                               @RequestBody Course course) {
        return courseService.updateCourse(id, course);
    }

    // ✅ DELETE COURSE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {

        courseService.deleteCourse(id);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Course deleted successfully",
                        "success", true
                )
        );
    }

    // ✅ ENABLE / DISABLE / COMPLETE COURSE
    @PutMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam boolean enabled,
                               @RequestParam boolean completed) {

        courseService.updateCourseStatus(id, enabled, completed);
        return "Course status updated successfully";
    }
}