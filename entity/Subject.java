package com.easy408.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "subjects")
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // e.g., "数据结构"

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    private List<Chapter> chapters;
}