package com.easy408.dto;

import com.easy408.entity.CardType;
import lombok.Data;

@Data
public class CreateCardRequest {
    private String subjectName;
    private String chapterName;
    private String frontContent;
    private String backContent;
    private CardType cardType;
    private String tags;
    
    // Optional Choice Fields
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
}