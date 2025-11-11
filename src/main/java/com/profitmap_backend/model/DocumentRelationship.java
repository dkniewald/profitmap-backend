package com.profitmap_backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "document_relationships",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"source_document_id", "target_document_id", "relationship_type"},
            name = "uk_document_relationship"
        )
    },
    indexes = {
        @Index(name = "idx_doc_rel_source", columnList = "source_document_id"),
        @Index(name = "idx_doc_rel_target", columnList = "target_document_id"),
        @Index(name = "idx_doc_rel_type", columnList = "relationship_type")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRelationship {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_document_id", nullable = false)
    private Document sourceDocument;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_document_id", nullable = false)
    private Document targetDocument;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false)
    private DocumentRelationshipType relationshipType;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // Business rule: source and target cannot be the same document
    @PrePersist
    @PreUpdate
    private void validateRelationship() {
        if (sourceDocument != null && targetDocument != null && 
            sourceDocument.getId().equals(targetDocument.getId())) {
            throw new IllegalArgumentException("Source and target documents cannot be the same");
        }
    }
}
