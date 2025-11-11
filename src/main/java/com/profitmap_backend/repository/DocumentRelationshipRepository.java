package com.profitmap_backend.repository;

import com.profitmap_backend.model.Document;
import com.profitmap_backend.model.DocumentRelationship;
import com.profitmap_backend.model.DocumentRelationshipType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRelationshipRepository extends JpaRepository<DocumentRelationship, Long> {
    
    // Find all relationships where a document is the source
    List<DocumentRelationship> findBySourceDocument(Document sourceDocument);
    
    // Find all relationships where a document is the target
    List<DocumentRelationship> findByTargetDocument(Document targetDocument);
    
    // Find relationships by type
    List<DocumentRelationship> findByRelationshipType(DocumentRelationshipType relationshipType);
    
    // Find specific relationship between two documents
    Optional<DocumentRelationship> findBySourceDocumentAndTargetDocumentAndRelationshipType(
        Document sourceDocument, 
        Document targetDocument, 
        DocumentRelationshipType relationshipType
    );
    
    // Find all relationships involving a specific document (as source or target)
    @Query("SELECT dr FROM DocumentRelationship dr WHERE dr.sourceDocument = :document OR dr.targetDocument = :document")
    List<DocumentRelationship> findAllRelationshipsInvolvingDocument(@Param("document") Document document);
    
    // Find all offers related to an invoice
    @Query("SELECT dr.sourceDocument FROM DocumentRelationship dr " +
           "WHERE dr.targetDocument = :invoice AND dr.sourceDocument.documentType = 'OFFER'")
    List<Document> findOffersRelatedToInvoice(@Param("invoice") Document invoice);
    
    // Find all invoices related to an offer
    @Query("SELECT dr.targetDocument FROM DocumentRelationship dr " +
           "WHERE dr.sourceDocument = :offer AND dr.targetDocument.documentType = 'INVOICE'")
    List<Document> findInvoicesRelatedToOffer(@Param("offer") Document offer);
    
    // Check if two documents are related
    @Query("SELECT COUNT(dr) > 0 FROM DocumentRelationship dr " +
           "WHERE (dr.sourceDocument = :doc1 AND dr.targetDocument = :doc2) " +
           "OR (dr.sourceDocument = :doc2 AND dr.targetDocument = :doc1)")
    boolean areDocumentsRelated(@Param("doc1") Document doc1, @Param("doc2") Document doc2);
    
    // Find relationships by company (useful for business queries)
    @Query("SELECT dr FROM DocumentRelationship dr " +
           "WHERE dr.sourceDocument.company = :company OR dr.targetDocument.company = :company")
    List<DocumentRelationship> findByCompany(@Param("company") com.profitmap_backend.model.Company company);
}
