package com.easy408.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.*;
import lombok.Data;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

@Data
@Entity
@Table(name = "cards")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Chapter chapter;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String frontContent;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String backContent;

    // 选择题字段
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;

    @Enumerated(EnumType.STRING)
    private CardType cardType;

    private String tags;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    private Integer reviewCount = 0;

    // 兼容多种时间格式的反序列化
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime lastReviewDate;

    @Column(nullable = false)
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime nextReviewDate;

    private Integer proficiency = 0;

    // 临时字段，不存数据库，仅用于 JSON 导入时接收 subjectName
    @Transient
    private String tempSubjectName;

    @PrePersist
    public void prePersist() {
        if (this.status == null) this.status = CardStatus.NEW;
        if (this.nextReviewDate == null) this.nextReviewDate = LocalDateTime.now();
    }
    
    // 专门的 setter 供 Jackson 反序列化 JSON 使用
    public void setSubjectName(String subjectName) {
        this.tempSubjectName = subjectName;
    }

    // 虚拟字段：提供给前端直接使用科目名称
    public String getSubjectName() {
        if (chapter != null && chapter.getSubject() != null) {
            return chapter.getSubject().getName();
        }
        return tempSubjectName != null ? tempSubjectName : "";
    }

    // 内部类：自定义反序列化器
    public static class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getText();
            try {
                // 1. 尝试作为 Instant 解析 (处理前端传来的带 'Z' 的 UTC 时间)
                // 如: 2023-10-27T10:00:00.000Z
                if (text.endsWith("Z")) {
                    return LocalDateTime.ofInstant(Instant.parse(text), ZoneId.of("UTC"));
                }
                // 2. 尝试作为标准 LocalDateTime 解析 (处理导入文件中的格式)
                // 如: 2026-02-02T21:20:43.760036
                return LocalDateTime.parse(text);
            } catch (DateTimeParseException e) {
                // 兜底：如果是空字符串，返回 null
                if (text == null || text.isEmpty()) return null;
                throw new IOException("无法解析时间格式: " + text, e);
            }
        }
    }
}