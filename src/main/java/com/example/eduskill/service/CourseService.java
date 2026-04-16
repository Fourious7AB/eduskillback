package com.example.eduskill.service;

import com.example.eduskill.entity.Course;

import java.util.List;

public interface CourseService {

    List<Course> getAllCourses();

    Course createCourse(Course course);

    Course updateCourse(Long id, Course course);

    void deleteCourse(Long id);

    Course getCourseById(Long id);

    void updateCourseStatus(Long id, boolean enabled, boolean completed);
}