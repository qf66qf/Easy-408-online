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
import java.util.Optional;

@Service
public class BackupService {

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private ChapterRepository chapterRepository;

    // 导出
    public List<Card> exportAllCards() {
        return cardRepository.findAll();
    }

    // 导入：智能合并逻辑
    @Transactional
    public String importCards(List<Card> importedCards) {
        int added = 0;
        int updated = 0;
        int skipped = 0;

        for (Card source : importedCards) {
            // 1. 获取关联信息 (优先从 JSON 反序列化得到的 tempSubjectName 获取)
            String subjectName = source.getSubjectName();
            if (subjectName == null || subjectName.trim().isEmpty()) {
                // 尝试从嵌套对象获取（兼容旧版数据格式）
                if (source.getChapter() != null && source.getChapter().getSubject() != null) {
                    subjectName = source.getChapter().getSubject().getName();
                } else {
                    subjectName = "未分类";
                }
            }

            String chapterName = "默认章节";
            if (source.getChapter() != null && source.getChapter().getName() != null) {
                chapterName = source.getChapter().getName();
            }

            // 2. 查找或创建 Subject/Chapter 基础数据
            String finalSubjectName = subjectName;
            Subject subject = subjectRepository.findByName(subjectName)
                    .orElseGet(() -> {
                        Subject s = new Subject();
                        s.setName(finalSubjectName);
                        return subjectRepository.save(s);
                    });

            String finalChapterName = chapterName;
            Chapter chapter = chapterRepository.findByNameAndSubject(chapterName, subject)
                    .orElseGet(() -> {
                        Chapter c = new Chapter();
                        c.setName(finalChapterName);
                        c.setSubject(subject);
                        return chapterRepository.save(c);
                    });

            // 3. 查重逻辑：同一个科目下，如果问题内容(frontContent)完全一样，视为同一题
            Optional<Card> existingOpt = cardRepository.findDuplicate(subjectName, source.getFrontContent());

            if (existingOpt.isPresent()) {
                Card existing = existingOpt.get();
                // 检查内容是否有变更 (解析、选项、答案)
                boolean contentChanged = !compareStrings(existing.getBackContent(), source.getBackContent()) ||
                                         !compareStrings(existing.getOptionA(), source.getOptionA()) ||
                                         !compareStrings(existing.getCorrectAnswer(), source.getCorrectAnswer());

                if (contentChanged) {
                    // 更新内容，但保留复习进度 (Status, NextReviewDate, Proficiency, ReviewCount)
                    existing.setBackContent(source.getBackContent());
                    existing.setTags(source.getTags());
                    existing.setCardType(source.getCardType());
                    existing.setOptionA(source.getOptionA());
                    existing.setOptionB(source.getOptionB());
                    existing.setOptionC(source.getOptionC());
                    existing.setOptionD(source.getOptionD());
                    existing.setCorrectAnswer(source.getCorrectAnswer());
                    // 确保关联更新
                    existing.setChapter(chapter);
                    
                    cardRepository.save(existing);
                    updated++;
                } else {
                    skipped++;
                }
            } else {
                // 新增卡片
                Card newCard = new Card();
                newCard.setChapter(chapter);
                newCard.setFrontContent(source.getFrontContent());
                newCard.setBackContent(source.getBackContent());
                newCard.setCardType(source.getCardType());
                newCard.setTags(source.getTags());
                
                // 保留导入的进度状态
                if (source.getStatus() != null) newCard.setStatus(source.getStatus());
                if (source.getNextReviewDate() != null) newCard.setNextReviewDate(source.getNextReviewDate());
                if (source.getLastReviewDate() != null) newCard.setLastReviewDate(source.getLastReviewDate());
                if (source.getProficiency() != null) newCard.setProficiency(source.getProficiency());
                if (source.getReviewCount() != null) newCard.setReviewCount(source.getReviewCount());

                newCard.setOptionA(source.getOptionA());
                newCard.setOptionB(source.getOptionB());
                newCard.setOptionC(source.getOptionC());
                newCard.setOptionD(source.getOptionD());
                newCard.setCorrectAnswer(source.getCorrectAnswer());

                cardRepository.save(newCard);
                added++;
            }
        }
        return String.format("导入完成：新增 %d 条，更新内容 %d 条，跳过相同 %d 条。", added, updated, skipped);
    }

    private boolean compareStrings(String s1, String s2) {
        if (s1 == null && s2 == null) return true;
        if (s1 == null) return s2.isEmpty();
        if (s2 == null) return s1.isEmpty();
        return s1.equals(s2);
    }
}