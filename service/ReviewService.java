package com.easy408.service;

import com.easy408.entity.Card;
import com.easy408.entity.CardStatus;
import com.easy408.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private CardRepository cardRepository;

    public List<Card> getDueCards(String subjectName) {
        // 处理空字符串情况
        if (subjectName != null && subjectName.trim().isEmpty()) {
            subjectName = null;
        }
        return cardRepository.findDueCardsBySubject(subjectName, LocalDateTime.now());
    }

    @Transactional
    public Card processReview(Long cardId, int quality) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        card.setLastReviewDate(LocalDateTime.now());
        card.setReviewCount(card.getReviewCount() + 1);

        // 简单的按天数后推逻辑
        if (quality == 0) {
            // 忘记了 (Forgot)
            // 熟练度归零
            card.setProficiency(0);
            card.setStatus(CardStatus.LEARNING);
            // 策略：设为 1 分钟后复习，这样你如果还在刷题，它会马上再次出现，强迫你记下来
            // 如果你想直接推到明天，可以改为 plusDays(1)
            card.setNextReviewDate(LocalDateTime.now().plusMinutes(1)); 
        } else if (quality == 1) {
            // 模糊 (Hard)
            // 熟练度不变或微增
            // 间隔：2天
            card.setNextReviewDate(LocalDateTime.now().plusDays(2));
        } else {
            // 简单 (Easy/Mastered)
            // 熟练度 +1
            card.setProficiency(Math.min(5, card.getProficiency() + 1));
            
            if (card.getProficiency() >= 4) {
                card.setStatus(CardStatus.MASTERED);
            }
            
            // 间隔算法：基于熟练度的指数增长 (1, 2, 4, 8, 15, 30...)
            int days = (int) Math.pow(2, card.getProficiency());
            // 至少推后 1 天
            if (days < 1) days = 1;
            
            card.setNextReviewDate(LocalDateTime.now().plusDays(days));
        }

        return cardRepository.save(card);
    }
}