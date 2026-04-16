package com.example.eduskill.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "completion_processed")
    private LocalDateTime completionProcessedAt;

    private String courseName;

    private String courseClass;

    private Integer joiningFee;

    private Integer discountPrice;

    private Integer subscriptionFee;

    private LocalDateTime deletedAt;

    @Column(name = "image_url")
    private String image;

    private String type;

    @Column(name = "title", length = 250)
    private String title;

    // ✅ NEW FIELDS (FIX)
    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    // FLAGS
    @Builder.Default
    private Boolean deleted = false;

    @Builder.Default
    private Boolean enabled = true;

    @Builder.Default
    private Boolean completed = false;



    @JsonIgnore
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Enrollment> enrollments;
}