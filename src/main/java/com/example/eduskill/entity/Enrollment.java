package com.example.eduskill.entity;

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
@Table(
        name = "enrollment",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_id","course_id"})
        }
)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    private LocalDateTime enrolledDate;

    private String employeeCode;

    private boolean active;

    @Column(nullable = false)
    @Builder.Default
    private boolean courseAccess =true;

    private Integer enrollmentPrice;

    @Builder.Default
    private boolean firstPaymentDone = false;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subscription> subscriptions;



}