package com.kiszka.prj.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "kids_suggestions")
public class KidSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "suggestion_id")
    private Integer id;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private String title;
    @Column(name = "proposed_start", nullable = false)
    private LocalDateTime proposedStart;
    @Column(name = "proposed_end", nullable = false)
    private LocalDateTime proposedEnd;
    @Column(nullable = false)
    private String status;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private Parent reviewedBy;
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private Kid createdBy;
}
