package com.profitmap_backend.service;

import com.profitmap_backend.model.*;
import com.profitmap_backend.repository.DocumentRepository;
import com.profitmap_backend.repository.DocumentSeriesRepository;
import com.profitmap_backend.repository.DocumentClientRepository;
import com.profitmap_backend.repository.DocumentRelationshipRepository;
import com.profitmap_backend.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {
    
    private final DocumentRepository documentRepository;
    private final DocumentSeriesRepository documentSeriesRepository;
    private final DocumentClientRepository documentClientRepository;
    private final DocumentRelationshipRepository documentRelationshipRepository;
    private final CompanyRepository companyRepository;
    
    /**
     * Generates the next document number for a given company and document type.
     * Uses the company's configured prefix and year settings.
     * Uses pessimistic locking to ensure thread safety in concurrent environments.
     * 
     * @param companyId The company ID
     * @param documentType The document type (OFFER or INVOICE)
     * @return The generated document number
     */
    @Transactional
    public String generateNextDocumentNumber(Long companyId, DocumentType documentType) {
        // Get company to access series configuration
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        // Get prefix and year based on document type
        String prefix;
        String year;
        if (documentType == DocumentType.OFFER) {
            prefix = company.getOfferPrefix();
            year = company.getOfferYear();
        } else if (documentType == DocumentType.INVOICE) {
            prefix = company.getInvoicePrefix();
            year = company.getInvoiceYear();
        } else {
            throw new IllegalArgumentException("Unsupported document type: " + documentType);
        }
        // Use pessimistic lock to prevent concurrent access
        Optional<DocumentSeries> seriesOpt = documentSeriesRepository
                .findByCompanyIdAndPrefixAndYearWithLock(companyId, prefix, year);
        
        DocumentSeries series;
        if (seriesOpt.isPresent()) {
            series = seriesOpt.get();
            // Increment the next number
            series.setNextNumber(series.getNextNumber() + 1);
        } else {
            // Create new series starting from 1
            series = DocumentSeries.builder()
                    .companyId(companyId)
                    .prefix(prefix)
                    .year(year)
                    .nextNumber(1L)
                    .build();
        }
        
        // Save the series (this will either update existing or create new)
        series = documentSeriesRepository.save(series);
        
        // Generate the document number
        String documentNumber = String.format("%s-%s-%04d", prefix, year, series.getNextNumber());
        
        log.info("Generated document number: {} for company: {}", documentNumber, companyId);
        return documentNumber;
    }
    
    /**
     * Creates a new document with auto-generated document number and client snapshot
     * Always creates snapshot from provided client data
     */
    @Transactional
    public Document createDocument(Document document, Long companyId,
                                 String name, String contact, String email, ClientType clientType,
                                 String oib, String address, String surname) {
        
        // Validate that the company exists and set it on the document
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + companyId));
        
        document.setCompany(company);
        
        // Create snapshot from provided client data
        DocumentClient documentClient = DocumentClient.builder()
                .name(name)
                .contact(contact)
                .email(email)
                .clientType(clientType)
                .oib(oib)
                .address(address)
                .surname(surname)
                .originalClientId(null) // No original client since it's manually entered
                .build();
        
        documentClient = documentClientRepository.save(documentClient);
        
        // Set the document client snapshot
        document.setDocumentClient(documentClient);
        
        // Generate document number
        String documentNumber = generateNextDocumentNumber(
                document.getCompany().getId(), 
                document.getDocumentType()
        );
        
        document.setDocumentNumber(documentNumber);
        
        // Ensure all document items have their document reference set
        if (document.getDocumentItems() != null) {
            document.getDocumentItems().forEach(item -> item.setDocument(document));
        }
        
        // Calculate total price from items
        if (document.getDocumentItems() != null && !document.getDocumentItems().isEmpty()) {
            BigDecimal totalPrice = document.getDocumentItems().stream()
                    .map(item -> {
                        BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                        if (item.getDiscountPercentage() != null && item.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal discount = itemTotal.multiply(item.getDiscountPercentage().divide(BigDecimal.valueOf(100)));
                            itemTotal = itemTotal.subtract(discount);
                        }
                        return itemTotal;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            document.setTotalPrice(totalPrice);
        }
        
        return documentRepository.save(document);
    }
    
    /**
     * Creates a new document with auto-generated document number (legacy method for backward compatibility)
     * @deprecated Use createDocument(Document document, String prefix, Long clientId) instead
     */
    @Deprecated
    @Transactional
    public Document createDocument(Document document, String prefix) {
        // This method is deprecated and should not be used
        // It's kept for backward compatibility but will throw an exception
        throw new UnsupportedOperationException("This method is deprecated. Use createDocument(Document document, String prefix, Long clientId) instead");
    }
    
    /**
     * Soft deletes a document
     */
    @Transactional
    public void deleteDocument(Long documentId) {
        Document document = getDocumentByIdWithCompany(documentId);
        
        document.softDelete();
        documentRepository.save(document);
    }
    
    /**
     * Updates document status
     */
    @Transactional
    public Document updateDocumentStatus(Long documentId, DocumentStatus newStatus) {
        Document document = getDocumentByIdWithCompany(documentId);
        /*

        validateStatusTransition(document.getStatus(), newStatus);
        if(newStatus == DocumentStatus.PENDING) {
            invoicePostingService.postAndReportInvoice(document);
        }*/
        document.setStatus(newStatus);
        return documentRepository.save(document);
    }
    
    /**
     * Validates if a status transition is allowed
     */
    private void validateStatusTransition(DocumentStatus currentStatus, DocumentStatus newStatus) {
        // Add business logic here for valid status transitions
        // For example, you might not allow going from CANCELED back to OUTSTANDING
        if (currentStatus == DocumentStatus.CANCELED && newStatus == DocumentStatus.OUTSTANDING) {
            throw new RuntimeException("Cannot change status from CANCELED to OUTSTANDING");
        }
        
        // Add more validation rules as needed
        log.info("Status transition: {} -> {} for document", currentStatus, newStatus);
    }
    
    /**
     * Gets all active documents for a company
     */
    @Transactional(readOnly = true)
    public List<Document> getActiveDocumentsByCompany(Long companyId) {
        return documentRepository.findActiveDocumentsByCompanyWithCompany(companyId);
    }
    
    /**
     * Gets documents by type for a company
     */
    @Transactional(readOnly = true)
    public List<Document> getDocumentsByType(Long companyId, DocumentType documentType) {
        return documentRepository.findByCompanyIdAndDocumentTypeWithCompany(companyId, documentType);
    }
    
    /**
     * Gets a document by document number and company
     */
    @Transactional(readOnly = true)
    public Optional<Document> getDocumentByNumber(String documentNumber, Long companyId) {
        return documentRepository.findByDocumentNumberAndCompanyIdWithCompany(documentNumber, companyId);
    }
    
    /**
     * Gets a document by ID with proper transaction handling for lazy loading
     */
    @Transactional(readOnly = true)
    public Document getDocumentById(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
    }
    
    /**
     * Gets a document by ID with company eagerly fetched (avoids lazy loading issues)
     */
    @Transactional(readOnly = true)
    public Document getDocumentByIdWithCompany(Long documentId) {
        return documentRepository.findByIdWithCompany(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
    }

    // ========== DOCUMENT RELATIONSHIP METHODS ==========

    /**
     * Creates a relationship between two documents
     */
    @Transactional
    public DocumentRelationship createDocumentRelationship(Long sourceDocumentId, Long targetDocumentId, 
                                                         DocumentRelationshipType relationshipType, String notes) {
        Document sourceDocument = getDocumentByIdWithCompany(sourceDocumentId);
        Document targetDocument = getDocumentByIdWithCompany(targetDocumentId);

        // Check if relationship already exists
        Optional<DocumentRelationship> existingRelationship = documentRelationshipRepository
                .findBySourceDocumentAndTargetDocumentAndRelationshipType(sourceDocument, targetDocument, relationshipType);
        
        if (existingRelationship.isPresent()) {
            throw new RuntimeException("Relationship already exists between these documents");
        }

        DocumentRelationship relationship = DocumentRelationship.builder()
                .sourceDocument(sourceDocument)
                .targetDocument(targetDocument)
                .relationshipType(relationshipType)
                .notes(notes)
                .build();

        return documentRelationshipRepository.save(relationship);
    }

    /**
     * Converts an offer to an invoice with relationship
     */
    @Transactional
    public Document convertOfferToInvoice(Long offerId, String notes) {
        Document offer = getDocumentByIdWithCompany(offerId);

        if (!offer.isOffer()) {
            throw new RuntimeException("Document is not an offer");
        }

        // Create new invoice based on offer
        Document invoice = Document.builder()
                .company(offer.getCompany())
                .documentDate(offer.getDocumentDate())
                .expirationDate(offer.getExpirationDate())
                .documentType(DocumentType.INVOICE)
                .status(DocumentStatus.PENDING)
                .documentItems(offer.getDocumentItems())
                .totalPrice(offer.getTotalPrice())
                .documentClient(offer.getDocumentClient())
                .build();

        // Generate new document number for invoice
        String invoiceNumber = generateNextDocumentNumber(
                offer.getCompany().getId(), 
                DocumentType.INVOICE
        );
        invoice.setDocumentNumber(invoiceNumber);

        invoice = documentRepository.save(invoice);

        // Create relationship
        createDocumentRelationship(offerId, invoice.getId(), DocumentRelationshipType.OFFER_TO_INVOICE, notes);

        log.info("Converted offer {} to invoice {}", offer.getDocumentNumber(), invoice.getDocumentNumber());
        return invoice;
    }

    /**
     * Gets all offers related to an invoice
     */
    @Transactional(readOnly = true)
    public List<Document> getOffersRelatedToInvoice(Long invoiceId) {
        Document invoice = getDocumentByIdWithCompany(invoiceId);
        
        return documentRelationshipRepository.findOffersRelatedToInvoice(invoice);
    }

    /**
     * Gets all invoices related to an offer
     */
    @Transactional(readOnly = true)
    public List<Document> getInvoicesRelatedToOffer(Long offerId) {
        Document offer = getDocumentByIdWithCompany(offerId);
        
        return documentRelationshipRepository.findInvoicesRelatedToOffer(offer);
    }

    /**
     * Gets all relationships for a document
     */
    @Transactional(readOnly = true)
    public List<DocumentRelationship> getDocumentRelationships(Long documentId) {
        Document document = getDocumentByIdWithCompany(documentId);
        
        return documentRelationshipRepository.findAllRelationshipsInvolvingDocument(document);
    }

    /**
     * Removes a relationship between documents
     */
    @Transactional
    public void removeDocumentRelationship(Long relationshipId) {
        DocumentRelationship relationship = documentRelationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new RuntimeException("Relationship not found with id: " + relationshipId));
        
        documentRelationshipRepository.delete(relationship);
        log.info("Removed relationship between documents {} and {}", 
                relationship.getSourceDocument().getDocumentNumber(),
                relationship.getTargetDocument().getDocumentNumber());
    }

    /**
     * Checks if two documents are related
     */
    @Transactional(readOnly = true)
    public boolean areDocumentsRelated(Long documentId1, Long documentId2) {
        Document doc1 = getDocumentByIdWithCompany(documentId1);
        Document doc2 = getDocumentByIdWithCompany(documentId2);
        
        return documentRelationshipRepository.areDocumentsRelated(doc1, doc2);
    }
}
