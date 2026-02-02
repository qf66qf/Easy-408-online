package com.easy408.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String frontContent;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String backContent;

    @Enumerated(EnumType.STRING)
    private CardType cardType; // CHOICE, SHORT_ANSWER

    private String tags; // Comma separated

    @Enumerated(EnumType.STRING)
    private CardStatus status; // NEW, LEARNING, MASTERED

    private Integer reviewCount = 0;

    private LocalDateTime lastReviewDate;

    @Column(nullable = false)
    private LocalDateTime nextReviewDate;

    private Integer proficiency = 0; // 0-5

    // Helper for JPA pre-persist
    @PrePersist
    public void prePersist() {
        if (this.status == null) this.status = CardStatus.NEW;
        if (this.nextReviewDate == null) this.nextReviewDate = LocalDateTime.now();
    }
}

enum CardType { CHOICE, SHORT_ANSWER }
enum CardStatus { NEW, LEARNING, MASTERED }