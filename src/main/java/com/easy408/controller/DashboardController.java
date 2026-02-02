package com.easy408.controller;

import com.easy408.dto.SubjectStatDTO;
import com.easy408.entity.CardStatus;
import com.easy408.entity.Subject;
import com.easy408.repository.CardRepository;
import com.easy408.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private CardRepository cardRepository;

    @GetMapping("/stats")
    public List<SubjectStatDTO> getDashboardStats() {
        List<Subject> subjects = subjectRepository.findAll();
        List<SubjectStatDTO> stats = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 如果数据库还没初始化科目，先不返回或由其他逻辑处理初始化
        // 这里假设 SubjectRepository 里已经有了基础数据（如果没有，第一次添加卡片时会创建）
        // 为了前端显示好看，我们可以硬编码确保返回4个科目的结构，或者依赖数据库
        
        String[] defaultSubjects = {"数据结构", "计算机组成原理", "操作系统", "计算机网络"};
        
        for (String name : defaultSubjects) {
            // 简单处理：如果数据库没这个科目，也要返回全0数据
            long total = cardRepository.countBySubject(name);
            long newCards = cardRepository.countBySubjectAndStatus(name, CardStatus.NEW);
            long learned = cardRepository.countBySubjectAndStatusNot(name, CardStatus.NEW); // 已学 = 总数 - NEW
            long due = cardRepository.countDueBySubject(name, now);
            
            // 查找ID，找不到就设为null，前端主要靠 name 匹配
            Long id = subjectRepository.findByName(name).map(Subject::getId).orElse(null);
            
            stats.add(new SubjectStatDTO(id, name, total, learned, newCards, due));
        }

        return stats;
    }
}