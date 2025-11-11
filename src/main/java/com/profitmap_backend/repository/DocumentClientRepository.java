package com.profitmap_backend.repository;

import com.profitmap_backend.model.DocumentClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentClientRepository extends JpaRepository<DocumentClient, Long> {
    
    /**
     * Find all document client snapshots for a specific original client
     * @param originalClientId the ID of the original client
     * @return list of document client snapshots
     */
    List<DocumentClient> findByOriginalClientId(Long originalClientId);
}
