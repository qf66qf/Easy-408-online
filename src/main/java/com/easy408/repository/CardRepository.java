package com.easy408.repository;

import com.easy408.entity.Card;
import com.easy408.entity.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    @Query("SELECT c FROM Card c JOIN FETCH c.chapter ch JOIN FETCH ch.subject s " +
           "WHERE (:subjectName IS NULL OR s.name = :subjectName) " +
           "AND c.nextReviewDate <= :now " +
           "ORDER BY c.nextReviewDate ASC")
    List<Card> findDueCardsBySubject(@Param("subjectName") String subjectName, @Param("now") LocalDateTime now);

    // 管理页面查询：支持科目、章节、关键字搜索
    @Query("SELECT c FROM Card c JOIN FETCH c.chapter ch JOIN FETCH ch.subject s " +
           "WHERE (:subjectName IS NULL OR s.name = :subjectName) " +
           "AND (:chapterName IS NULL OR ch.name = :chapterName) " +
           "AND (:keyword IS NULL OR c.frontContent LIKE %:keyword% OR c.tags LIKE %:keyword%) " +
           "ORDER BY c.id DESC")
    List<Card> searchCards(@Param("subjectName") String subjectName, 
                          @Param("chapterName") String chapterName, 
                          @Param("keyword") String keyword);

    @Query("SELECT COUNT(c) FROM Card c JOIN c.chapter ch JOIN ch.subject s WHERE s.name = :subjectName AND c.status = :status")
    long countBySubjectAndStatus(@Param("subjectName") String subjectName, @Param("status") CardStatus status);
    
    @Query("SELECT COUNT(c) FROM Card c JOIN c.chapter ch JOIN ch.subject s WHERE s.name = :subjectName AND c.status <> :status")
    long countBySubjectAndStatusNot(@Param("subjectName") String subjectName, @Param("status") CardStatus status);

    @Query("SELECT COUNT(c) FROM Card c JOIN c.chapter ch JOIN ch.subject s WHERE s.name = :subjectName")
    long countBySubject(@Param("subjectName") String subjectName);
    
    @Query("SELECT COUNT(c) FROM Card c JOIN c.chapter ch JOIN ch.subject s WHERE s.name = :subjectName AND c.nextReviewDate <= :now")
    long countDueBySubject(@Param("subjectName") String subjectName, @Param("now") LocalDateTime now);

    // 智能导入用：根据 科目名 + 题目内容 查找存在的卡片
    @Query("SELECT c FROM Card c JOIN c.chapter ch JOIN ch.subject s WHERE s.name = :subjectName AND c.frontContent = :frontContent")
    Optional<Card> findDuplicate(@Param("subjectName") String subjectName, @Param("frontContent") String frontContent);
}