package com.kiszka.prj.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

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
    @Column(name = "proposed_date", nullable = false)
    private Date proposedDate;
    @Column(nullable = false)
    private String status;
    @Column(name = "created_at", nullable = false)
    private Date createdAt;
    @Column(name = "reviewed_at")
    private Date reviewedAt;
    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private Parent reviewedBy;
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private Kid createdBy;
}
