package com.easy408.controller;

import com.easy408.dto.CreateCardRequest;
import com.easy408.dto.ReviewRequest;
import com.easy408.entity.Card;
import com.easy408.entity.Chapter;
import com.easy408.entity.Subject;
import com.easy408.repository.CardRepository;
import com.easy408.repository.ChapterRepository;
import com.easy408.repository.SubjectRepository;
import com.easy408.service.CardService;
import com.easy408.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = "*")
public class CardController {

    private static final Logger logger = LoggerFactory.getLogger(CardController.class);

    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private CardService cardService;

    @Autowired
    private CardRepository cardRepository;
    
    @Autowired
    private SubjectRepository subjectRepository;
    
    @Autowired
    private ChapterRepository chapterRepository;

    // 获取待复习卡片
    @GetMapping("/due")
    public List<Card> getDueCards(@RequestParam(required = false) String subjectName) {
        return reviewService.getDueCards(subjectName);
    }

    // 搜索卡片 (管理页面用)
    @GetMapping("/search")
    public List<Card> searchCards(
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) String chapterName,
            @RequestParam(required = false) String keyword) {
        if (subjectName != null && subjectName.isEmpty()) subjectName = null;
        if (chapterName != null && chapterName.isEmpty()) chapterName = null;
        if (keyword != null && keyword.isEmpty()) keyword = null;
        return cardRepository.searchCards(subjectName, chapterName, keyword);
    }

    // 删除卡片
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCard(@PathVariable Long id) {
        try {
            cardRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("删除失败");
        }
    }

    // 更新卡片
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCard(@PathVariable Long id, @RequestBody CreateCardRequest request) {
        try {
            Card card = cardRepository.findById(id).orElseThrow(() -> new RuntimeException("Card not found"));
            
            // 更新基础信息
            card.setFrontContent(request.getFrontContent());
            card.setBackContent(request.getBackContent());
            card.setTags(request.getTags());
            card.setCardType(request.getCardType());
            card.setOptionA(request.getOptionA());
            card.setOptionB(request.getOptionB());
            card.setOptionC(request.getOptionC());
            card.setOptionD(request.getOptionD());
            card.setCorrectAnswer(request.getCorrectAnswer());

            // 只有当科目/章节改变时才查找数据库关联
            if (!card.getSubjectName().equals(request.getSubjectName()) || 
                !card.getChapter().getName().equals(request.getChapterName())) {
                
                Subject subject = subjectRepository.findByName(request.getSubjectName())
                    .orElseGet(() -> {
                        Subject s = new Subject();
                        s.setName(request.getSubjectName());
                        return subjectRepository.save(s);
                    });

                Chapter chapter = chapterRepository.findByNameAndSubject(request.getChapterName(), subject)
                    .orElseGet(() -> {
                        Chapter c = new Chapter();
                        c.setName(request.getChapterName());
                        c.setSubject(subject);
                        return chapterRepository.save(c);
                    });
                card.setChapter(chapter);
            }

            cardRepository.save(card);
            return ResponseEntity.ok(card);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新失败: " + e.getMessage());
        }
    }

    // 提交复习结果
    @PostMapping("/{id}/review")
    public ResponseEntity<?> reviewCard(@PathVariable Long id, @RequestBody ReviewRequest request) {
        try {
            Card updatedCard = reviewService.processReview(id, request.getQuality());
            return ResponseEntity.ok(updatedCard);
        } catch (Exception e) {
            logger.error("Review failed for card " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("保存进度失败: " + e.getMessage());
        }
    }
    
    // 创建新卡片
    @PostMapping("/create")
    public ResponseEntity<?> createCard(@RequestBody CreateCardRequest request) {
        try {
            Card card = cardService.createCard(
                request.getSubjectName(),
                request.getChapterName(),
                request.getFrontContent(),
                request.getBackContent(),
                request.getCardType(),
                request.getTags(),
                request.getOptionA(),
                request.getOptionB(),
                request.getOptionC(),
                request.getOptionD(),
                request.getCorrectAnswer()
            );
            return ResponseEntity.ok(card);
        } catch (Exception e) {
            logger.error("Error creating card: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("创建失败: " + e.getMessage());
        }
    }

    // 图片上传接口
    @PostMapping("/upload")
    public String uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String projectRoot = System.getProperty("user.dir");
        String uploadDir = projectRoot + "/uploads/";
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File dest = new File(uploadDir + fileName);
        file.transferTo(dest);
        
        return "/uploads/" + fileName; 
    }
}