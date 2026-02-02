package com.easy408.repository;

import com.easy408.entity.Chapter;
import com.easy408.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    Optional<Chapter> findByNameAndSubject(String name, Subject subject);
}