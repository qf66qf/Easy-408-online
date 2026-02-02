package com.easy408.service;

import com.easy408.entity.Card;
import com.easy408.entity.Chapter;
import com.easy408.entity.Subject;
import com.easy408.repository.CardRepository;
import com.easy408.repository.ChapterRepository;
import com.easy408.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BackupService {

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private ChapterRepository chapterRepository;

    // 导出：直接获取所有卡片，JPA 会自动级联查询章节和科目信息
    public List<Card> exportAllCards() {
        return cardRepository.findAll();
    }

    // 导入：智能合并逻辑
    @Transactional
    public void importCards(List<Card> importedCards) {
        // 策略：为了防止 ID 冲突，我们忽略导入数据的 ID，将其视为新数据
        // 如果想做去重，可以根据 (frontContent + subject + chapter) 生成 hash 校验，这里简化为追加模式
        // 或者先清空库：cardRepository.deleteAll(); (根据需求，这里采用追加/合并模式)
        
        for (Card source : importedCards) {
            // 1. 提取关联信息
            String subjectName = "未分类";
            String chapterName = "默认章节";

            if (source.getChapter() != null) {
                chapterName = source.getChapter().getName();
                if (source.getChapter().getSubject() != null) {
                    subjectName = source.getChapter().getSubject().getName();
                }
            }

            // 2. 查找或创建 Subject
            String finalSubjectName = subjectName;
            Subject subject = subjectRepository.findByName(subjectName)
                    .orElseGet(() -> {
                        Subject s = new Subject();
                        s.setName(finalSubjectName);
                        return subjectRepository.save(s);
                    });

            // 3. 查找或创建 Chapter
            String finalChapterName = chapterName;
            Chapter chapter = chapterRepository.findByNameAndSubject(chapterName, subject)
                    .orElseGet(() -> {
                        Chapter c = new Chapter();
                        c.setName(finalChapterName);
                        c.setSubject(subject);
                        return chapterRepository.save(c);
                    });

            // 4. 构建并保存新卡片 (重置 ID)
            Card newCard = new Card();
            newCard.setChapter(chapter);
            newCard.setFrontContent(source.getFrontContent());
            newCard.setBackContent(source.getBackContent());
            newCard.setCardType(source.getCardType());
            newCard.setTags(source.getTags());
            newCard.setStatus(source.getStatus());
            newCard.setNextReviewDate(source.getNextReviewDate());
            newCard.setLastReviewDate(source.getLastReviewDate());
            newCard.setProficiency(source.getProficiency());
            newCard.setReviewCount(source.getReviewCount());
            
            // 选择题字段
            newCard.setOptionA(source.getOptionA());
            newCard.setOptionB(source.getOptionB());
            newCard.setOptionC(source.getOptionC());
            newCard.setOptionD(source.getOptionD());
            newCard.setCorrectAnswer(source.getCorrectAnswer());

            cardRepository.save(newCard);
        }
    }
}