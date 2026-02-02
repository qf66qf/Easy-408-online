package com.easy408.controller;

import com.easy408.entity.Chapter;
import com.easy408.entity.Subject;
import com.easy408.repository.ChapterRepository;
import com.easy408.repository.SubjectRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chapters")
@CrossOrigin(origins = "*")
public class ChapterController {

    @Autowired
    private ChapterRepository chapterRepository;
    
    @Autowired
    private SubjectRepository subjectRepository;

    // 获取某科目的所有章节
    @GetMapping
    public List<ChapterDTO> getChaptersBySubject(@RequestParam String subjectName) {
        Subject subject = subjectRepository.findByName(subjectName).orElse(null);
        if (subject == null) return List.of();
        
        return subject.getChapters().stream()
                .map(c -> new ChapterDTO(c.getId(), c.getName()))
                .collect(Collectors.toList());
    }

    // 创建新章节
    @PostMapping("/create")
    public ChapterDTO createChapter(@RequestBody CreateChapterRequest request) {
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
                
        return new ChapterDTO(chapter.getId(), chapter.getName());
    }

    @Data
    @lombok.AllArgsConstructor
    static class ChapterDTO {
        private Long id;
        private String name;
    }

    @Data
    static class CreateChapterRequest {
        private String subjectName;
        private String chapterName;
    }
}