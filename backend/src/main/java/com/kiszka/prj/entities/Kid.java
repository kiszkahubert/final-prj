package com.kiszka.prj.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "kids")
@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class Kid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kid_id")
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "birth_date")
    private LocalDate birthDate;
    @ManyToMany(mappedBy = "kids")
    private Set<Parent> parents = new HashSet<>();
}
