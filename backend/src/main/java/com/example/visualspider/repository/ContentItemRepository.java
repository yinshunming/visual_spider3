package com.example.visualspider.repository;

import com.example.visualspider.entity.ContentItem;
import com.example.visualspider.entity.ContentItem.ContentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentItemRepository extends JpaRepository<ContentItem, Long> {
    // Find by task ID
    List<ContentItem> findByTaskId(Long taskId);

    // Find by task ID with pagination
    Page<ContentItem> findByTaskId(Long taskId, Pageable pageable);

    // Find by status
    List<ContentItem> findByStatus(ContentStatus status);

    // Find by status with pagination
    Page<ContentItem> findByStatus(ContentStatus status, Pageable pageable);

    // Find by task ID and status
    List<ContentItem> findByTaskIdAndStatus(Long taskId, ContentStatus status);

    // Delete by task ID
    void deleteByTaskId(Long taskId);
}