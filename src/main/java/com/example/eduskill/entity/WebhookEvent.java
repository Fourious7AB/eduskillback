package com.example.eduskill.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="webhook_event",
        uniqueConstraints = @UniqueConstraint(columnNames="eventId"))
@Getter
@Setter
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;

    private String eventType;

    private boolean processed;

    private LocalDateTime createdAt = LocalDateTime.now();
}