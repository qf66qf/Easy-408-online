package com.easy408.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class SubjectStatDTO {
    private Long subjectId;
    private String subjectName;
    private long totalCards;
    private long learnedCards; // 非 NEW 状态
    private long newCards;     // NEW 状态
    private long dueCards;     // 今日待复习
}