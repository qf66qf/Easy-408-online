package com.easy408.controller;

import com.easy408.entity.Card;
import com.easy408.service.BackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/backup")
@CrossOrigin(origins = "*")
public class BackupController {

    private static final Logger logger = LoggerFactory.getLogger(BackupController.class);

    @Autowired
    private BackupService backupService;

    // 导出接口：下载 JSON 文件
    @GetMapping("/export")
    public ResponseEntity<List<Card>> exportData() {
        List<Card> cards = backupService.exportAllCards();
        
        String filename = "easy408_data_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".json";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(cards);
    }

    // 导入接口：接收 JSON 数据
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importData(@RequestBody List<Card> cards) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (cards == null || cards.isEmpty()) {
                response.put("message", "导入数据为空");
                return ResponseEntity.badRequest().body(response);
            }
            String resultMsg = backupService.importCards(cards);
            response.put("message", resultMsg);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Import failed", e);
            response.put("message", "导入失败: " + e.getMessage());
            response.put("success", false);
            return ResponseEntity.internalServerError().body(response);
        }
    }
}