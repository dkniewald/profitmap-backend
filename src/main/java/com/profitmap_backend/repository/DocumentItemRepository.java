package com.profitmap_backend.repository;

import com.profitmap_backend.model.DocumentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentItemRepository extends JpaRepository<DocumentItem, Long> {
    
    List<DocumentItem> findByDocumentId(Long documentId);
    
    void deleteByDocumentId(Long documentId);
}
