package com.easy408.controller;

import com.easy408.dto.CreateCardRequest;
import com.easy408.dto.ReviewRequest;
import com.easy408.entity.Card;
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

    // 获取待复习卡片
    @GetMapping("/due")
    public List<Card> getDueCards(@RequestParam(required = false) String subjectName) {
        try {
            return reviewService.getDueCards(subjectName);
        } catch (Exception e) {
            logger.error("Failed to fetch due cards", e);
            throw new RuntimeException("Error fetching cards: " + e.getMessage());
        }
    }

    // 提交复习结果
    @PostMapping("/{id}/review")
    public ResponseEntity<?> reviewCard(@PathVariable Long id, @RequestBody ReviewRequest request) {
        try {
            // 打印日志以便调试
            logger.info("Reviewing card ID: {}, Quality: {}", id, request.getQuality());
            Card updatedCard = reviewService.processReview(id, request.getQuality());
            return ResponseEntity.ok(updatedCard);
        } catch (Exception e) {
            logger.error("Review failed for card " + id, e);
            // 返回明确的错误信息给前端
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
// 已删除底部的 ReviewRequest 类，防止与 DTO 冲突