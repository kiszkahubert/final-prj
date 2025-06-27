package com.kiszka.prj.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tasks")
@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Integer taskId;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "description", nullable = false)
    private String description;
    @Column(name = "task_start", nullable = false)
    private LocalDateTime taskStart;
    @Column(name = "task_end", nullable = false)
    private LocalDateTime taskEnd;
    @Column(name = "status", nullable = false)
    private String status;
    @Column(name = "note")
    private String note;
    @Column(name = "parent_id", nullable = false)
    private Integer parentId;
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KidsTask> kidsAssignments;
}
