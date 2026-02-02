package com.easy408.service;

import com.easy408.entity.*;
import com.easy408.repository.CardRepository;
import com.easy408.repository.ChapterRepository;
import com.easy408.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardService {

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private ChapterRepository chapterRepository;

    @Transactional
    public Card createCard(String subjectName, String chapterName, String front, String back, 
                          CardType type, String tags, 
                          String optA, String optB, String optC, String optD, String correct) {
        // 1. 查找或创建 Subject
        Subject subject = subjectRepository.findByName(subjectName)
                .orElseGet(() -> {
                    Subject s = new Subject();
                    s.setName(subjectName);
                    return subjectRepository.save(s);
                });

        // 2. 查找或创建 Chapter
        Chapter chapter = chapterRepository.findByNameAndSubject(chapterName, subject)
                .orElseGet(() -> {
                    Chapter c = new Chapter();
                    c.setName(chapterName);
                    c.setSubject(subject);
                    return chapterRepository.save(c);
                });

        // 3. 创建 Card
        Card card = new Card();
        card.setChapter(chapter);
        card.setFrontContent(front);
        card.setBackContent(back);
        card.setCardType(type);
        card.setTags(tags);
        
        // 设置选择题特有字段
        if (type == CardType.CHOICE) {
            card.setOptionA(optA);
            card.setOptionB(optB);
            card.setOptionC(optC);
            card.setOptionD(optD);
            card.setCorrectAnswer(correct);
        }
        
        return cardRepository.save(card);
    }
}