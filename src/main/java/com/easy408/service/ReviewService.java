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

    // 严苛的考研复习周期 (单位：天)
    // 对应熟练度 0 - 7
    private static final int[] INTERVALS = {1, 2, 4, 7, 15, 30, 90, 180};

    public List<Card> getDueCards(String subjectName) {
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
        
        int currentProficiency = card.getProficiency() == null ? 0 : card.getProficiency();
        card.setReviewCount((card.getReviewCount() == null ? 0 : card.getReviewCount()) + 1);

        // quality: 0=忘记, 1=模糊, 2=记得
        if (quality == 0) {
            // 忘记：重置进度，进入第一周期（1天后）
            // 对于考研人，忘记意味着需要立即强化，但算法上我们设为明天复习，
            // 实际上用户应该在当前Session结束前再看一眼（由前端队列逻辑决定是否当场重现，后端只管存库）
            card.setProficiency(0);
            card.setStatus(CardStatus.LEARNING);
            card.setNextReviewDate(LocalDateTime.now().plusDays(INTERVALS[0])); 
        } else if (quality == 1) {
            // 模糊：保持当前熟练度或倒退一级，这里采用保守策略：倒退一级(若不为0)
            // 这样能确保不熟悉的知识点多复习几次
            int newProficiency = Math.max(0, currentProficiency - 1);
            card.setProficiency(newProficiency);
            card.setNextReviewDate(LocalDateTime.now().plusDays(INTERVALS[newProficiency]));
        } else {
            // 记得：晋级
            int newProficiency = currentProficiency + 1;
            
            // 封顶处理
            if (newProficiency >= INTERVALS.length) {
                newProficiency = INTERVALS.length - 1;
                card.setStatus(CardStatus.MASTERED);
            }
            
            card.setProficiency(newProficiency);
            
            // 状态流转
            if (newProficiency >= 3 && card.getStatus() == CardStatus.NEW) {
                card.setStatus(CardStatus.LEARNING);
            }
            if (newProficiency >= 6) {
                card.setStatus(CardStatus.MASTERED);
            }

            // 计算下次复习时间
            int days = INTERVALS[newProficiency];
            card.setNextReviewDate(LocalDateTime.now().plusDays(days));
        }

        return cardRepository.save(card);
    }
}