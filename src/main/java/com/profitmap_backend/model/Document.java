package com.profitmap_backend.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL") // Soft delete filter
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "document_date", nullable = false)
    private LocalDate documentDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    @Column(name = "document_number", nullable = false)
    private String documentNumber;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DocumentItem> documentItems;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "total_pdv", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPDV;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "document_client_id", nullable = false)
    private DocumentClient documentClient;

    // Many-to-many relationships with other documents
    @OneToMany(mappedBy = "sourceDocument", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DocumentRelationship> sourceRelationships;

    @OneToMany(mappedBy = "targetDocument", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DocumentRelationship> targetRelationships;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // For soft delete

    // Soft delete method
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    // Check if document is deleted
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    // Helper methods for document relationships
    public boolean isOffer() {
        return this.documentType == DocumentType.OFFER;
    }

    public boolean isInvoice() {
        return this.documentType == DocumentType.INVOICE;
    }

    // Get all related documents (both as source and target)
    public Set<Document> getAllRelatedDocuments() {
        Set<Document> related = new java.util.HashSet<>();
        if (sourceRelationships != null) {
            sourceRelationships.forEach(rel -> related.add(rel.getTargetDocument()));
        }
        if (targetRelationships != null) {
            targetRelationships.forEach(rel -> related.add(rel.getSourceDocument()));
        }
        return related;
    }
}
