package com.profitmap_backend.controller;

import com.profitmap_backend.dto.DocumentDto;
import com.profitmap_backend.model.*;
import com.profitmap_backend.service.DocumentService;
import com.profitmap_backend.service.MailService;
import com.profitmap_backend.util.DocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {
    
    private final DocumentService documentService;
    private final MailService mailService;
    
    @PostMapping("/offers")
    @Transactional
    public ResponseEntity<DocumentDto> createOffer(@RequestBody CreateDocumentRequest request) {
        Document createdDocument = documentService.createDocument(
                request.getDocument(), 
                request.getCompanyId(),
                request.getName(),
                request.getContact(),
                request.getEmail(),
                request.getClientType(),
                request.getOib(),
                request.getAddress(),
                request.getSurname()
        );
        DocumentDto documentDto = DocumentMapper.toDto(createdDocument);
        return ResponseEntity.ok(documentDto);
    }
    
    @PostMapping("/invoices")
    @Transactional
    public ResponseEntity<DocumentDto> createInvoice(@RequestBody CreateDocumentRequest request) throws Exception {
        Document createdDocument = documentService.createDocument(
                request.getDocument(), 
                request.getCompanyId(),
                request.getName(),
                request.getContact(),
                request.getEmail(),
                request.getClientType(),
                request.getOib(),
                request.getAddress(),
                request.getSurname()
        );
/*
        if (createdDocument.getDocumentType() == DocumentType.INVOICE
                && createdDocument.getStatus() != DocumentStatus.DRAFT) {
            try {
                FiskalizacijaResult fiskRes = fiskalizacijaService.evidentiraj(createdDocument);
            } catch (Exception e) {
                throw new RuntimeException("Fiskalizacija failed for document ID: " + createdDocument.getId(), e);
            }
        }
*/
        DocumentDto documentDto = DocumentMapper.toDto(createdDocument);
        return ResponseEntity.ok(documentDto);
    }
    
    @GetMapping("/company/{companyId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DocumentDto>> getDocumentsByCompany(@PathVariable Long companyId) {
        List<Document> documents = documentService.getActiveDocumentsByCompany(companyId);
        List<DocumentDto> documentDtos = documents.stream()
                .map(DocumentMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(documentDtos);
    }
    
    @GetMapping("/company/{companyId}/type/{documentType}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DocumentDto>> getDocumentsByType(
            @PathVariable Long companyId, 
            @PathVariable DocumentType documentType) {
        List<Document> documents = documentService.getDocumentsByType(companyId, documentType);
        List<DocumentDto> documentDtos = documents.stream()
                .map(DocumentMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(documentDtos);
    }
    
    @GetMapping("/{documentNumber}/company/{companyId}")
    @Transactional(readOnly = true)
    public ResponseEntity<DocumentDto> getDocumentByNumber(
            @PathVariable String documentNumber, 
            @PathVariable Long companyId) {
        Optional<Document> document = documentService.getDocumentByNumber(documentNumber, companyId);
        return document.map(doc -> ResponseEntity.ok(DocumentMapper.toDto(doc)))
                      .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long documentId) {
        documentService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update document status
     */
    @PatchMapping("/{documentId}/status")
    @Transactional
    public ResponseEntity<DocumentDto> updateDocumentStatus(
            @PathVariable Long documentId,
            @RequestParam DocumentStatus status) {
        Document updatedDocument = documentService.updateDocumentStatus(documentId, status);
        DocumentDto documentDto = DocumentMapper.toDto(updatedDocument);

        if (updatedDocument.getDocumentType() == DocumentType.INVOICE
                && updatedDocument.getStatus() != DocumentStatus.DRAFT) {
            try {
                mailService.sendTestMail();
            } catch (Exception e) {
                throw new RuntimeException("Fiskalizacija failed for document ID: " + updatedDocument.getId(), e);
            }
        }

        return ResponseEntity.ok(documentDto);
    }

    // ========== DOCUMENT RELATIONSHIP ENDPOINTS ==========

    /**
     * Convert an offer to an invoice
     */
    @PostMapping("/{offerId}/convert-to-invoice")
    @Transactional
    public ResponseEntity<DocumentDto> convertOfferToInvoice(
            @PathVariable Long offerId,
            @RequestParam(required = false) String notes) {
        Document invoice = documentService.convertOfferToInvoice(offerId, notes);
        DocumentDto invoiceDto = DocumentMapper.toDto(invoice);
        return ResponseEntity.ok(invoiceDto);
    }

    /**
     * Create a relationship between two documents
     */
    @PostMapping("/relationships")
    public ResponseEntity<DocumentRelationship> createDocumentRelationship(
            @RequestParam Long sourceDocumentId,
            @RequestParam Long targetDocumentId,
            @RequestParam DocumentRelationshipType relationshipType,
            @RequestParam(required = false) String notes) {
        DocumentRelationship relationship = documentService.createDocumentRelationship(
                sourceDocumentId, targetDocumentId, relationshipType, notes);
        return ResponseEntity.ok(relationship);
    }

    /**
     * Get all offers related to an invoice
     */
    @GetMapping("/{invoiceId}/related-offers")
    public ResponseEntity<List<DocumentDto>> getOffersRelatedToInvoice(@PathVariable Long invoiceId) {
        List<Document> offers = documentService.getOffersRelatedToInvoice(invoiceId);
        List<DocumentDto> offerDtos = offers.stream()
                .map(DocumentMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(offerDtos);
    }

    /**
     * Get all invoices related to an offer
     */
    @GetMapping("/{offerId}/related-invoices")
    public ResponseEntity<List<DocumentDto>> getInvoicesRelatedToOffer(@PathVariable Long offerId) {
        List<Document> invoices = documentService.getInvoicesRelatedToOffer(offerId);
        List<DocumentDto> invoiceDtos = invoices.stream()
                .map(DocumentMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(invoiceDtos);
    }

    /**
     * Get all relationships for a document
     */
    @GetMapping("/{documentId}/relationships")
    public ResponseEntity<List<DocumentRelationship>> getDocumentRelationships(@PathVariable Long documentId) {
        List<DocumentRelationship> relationships = documentService.getDocumentRelationships(documentId);
        return ResponseEntity.ok(relationships);
    }

    /**
     * Remove a relationship between documents
     */
    @DeleteMapping("/relationships/{relationshipId}")
    public ResponseEntity<Void> removeDocumentRelationship(@PathVariable Long relationshipId) {
        documentService.removeDocumentRelationship(relationshipId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if two documents are related
     */
    @GetMapping("/{documentId1}/related-to/{documentId2}")
    public ResponseEntity<Boolean> areDocumentsRelated(
            @PathVariable Long documentId1,
            @PathVariable Long documentId2) {
        boolean areRelated = documentService.areDocumentsRelated(documentId1, documentId2);
        return ResponseEntity.ok(areRelated);
    }
}
