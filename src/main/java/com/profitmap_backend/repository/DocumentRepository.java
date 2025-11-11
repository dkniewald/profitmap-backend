package com.profitmap_backend.repository;

import com.profitmap_backend.model.Document;
import com.profitmap_backend.model.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByCompanyIdAndDeletedAtIsNull(Long companyId);
    
    List<Document> findByCompanyIdAndDocumentTypeAndDeletedAtIsNull(Long companyId, DocumentType documentType);
    
    Optional<Document> findByDocumentNumberAndCompanyIdAndDeletedAtIsNull(String documentNumber, Long companyId);
    
    @Query("SELECT d FROM Document d WHERE d.company.id = :companyId AND d.deletedAt IS NULL ORDER BY d.documentDate DESC")
    List<Document> findActiveDocumentsByCompanyOrderByDateDesc(@Param("companyId") Long companyId);
    
    @Query("SELECT COUNT(d) FROM Document d WHERE d.company.id = :companyId AND d.documentType = :documentType AND d.deletedAt IS NULL")
    Long countActiveDocumentsByCompanyAndType(@Param("companyId") Long companyId, @Param("documentType") DocumentType documentType);
    
    @Query("SELECT d FROM Document d JOIN FETCH d.company WHERE d.id = :documentId AND d.deletedAt IS NULL")
    Optional<Document> findByIdWithCompany(@Param("documentId") Long documentId);
    
    @Query("SELECT d FROM Document d JOIN FETCH d.company WHERE d.company.id = :companyId AND d.deletedAt IS NULL ORDER BY d.documentDate DESC")
    List<Document> findActiveDocumentsByCompanyWithCompany(@Param("companyId") Long companyId);
    
    @Query("SELECT d FROM Document d JOIN FETCH d.company WHERE d.company.id = :companyId AND d.documentType = :documentType AND d.deletedAt IS NULL")
    List<Document> findByCompanyIdAndDocumentTypeWithCompany(@Param("companyId") Long companyId, @Param("documentType") DocumentType documentType);
    
    @Query("SELECT d FROM Document d JOIN FETCH d.company WHERE d.documentNumber = :documentNumber AND d.company.id = :companyId AND d.deletedAt IS NULL")
    Optional<Document> findByDocumentNumberAndCompanyIdWithCompany(@Param("documentNumber") String documentNumber, @Param("companyId") Long companyId);
}
