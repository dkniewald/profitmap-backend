package com.profitmap_backend.util;

import com.profitmap_backend.dto.DocumentDto;
import com.profitmap_backend.dto.DocumentItemDto;
import com.profitmap_backend.model.Document;
import com.profitmap_backend.model.DocumentItem;
import com.profitmap_backend.model.Company;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between Document entity and DTO
 */
public class DocumentMapper {
    
    /**
     * Maps Document entity to DocumentDto
     */
    public static DocumentDto toDto(Document document) {
        if (document == null) {
            return null;
        }
        
        return DocumentDto.builder()
                .id(document.getId())
                .companyId(document.getCompany() != null ? getCompanyId(document.getCompany()) : null)
                .documentDate(document.getDocumentDate())
                .expirationDate(document.getExpirationDate())
                .documentType(document.getDocumentType())
                .status(document.getStatus())
                .documentNumber(document.getDocumentNumber())
                .documentItems(mapDocumentItems(document.getDocumentItems()))
                .totalPrice(document.getTotalPrice())
                .totalPDV(document.getTotalPDV())
                .documentClient(DocumentClientMapper.toDto(document.getDocumentClient()))
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
    
    /**
     * Maps DocumentDto to Document entity
     */
    public static Document toEntity(DocumentDto dto) {
        if (dto == null) {
            return null;
        }
        
        return Document.builder()
                .id(dto.getId())
                .documentDate(dto.getDocumentDate())
                .expirationDate(dto.getExpirationDate())
                .documentType(dto.getDocumentType())
                .status(dto.getStatus())
                .documentNumber(dto.getDocumentNumber())
                .documentItems(mapDocumentItemEntities(dto.getDocumentItems()))
                .totalPrice(dto.getTotalPrice())
                .totalPDV(dto.getTotalPDV())
                .documentClient(DocumentClientMapper.toEntity(dto.getDocumentClient()))
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
    
    /**
     * Maps list of DocumentItem entities to DocumentItemDto list
     */
    private static List<DocumentItemDto> mapDocumentItems(List<DocumentItem> items) {
        if (items == null) {
            return null;
        }
        
        return items.stream()
                .map(DocumentMapper::mapDocumentItem)
                .collect(Collectors.toList());
    }
    
    /**
     * Maps list of DocumentItemDto to DocumentItem entity list
     */
    private static List<DocumentItem> mapDocumentItemEntities(List<DocumentItemDto> items) {
        if (items == null) {
            return null;
        }
        
        return items.stream()
                .map(DocumentMapper::mapDocumentItemEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Maps DocumentItem entity to DocumentItemDto
     */
    private static DocumentItemDto mapDocumentItem(DocumentItem item) {
        if (item == null) {
            return null;
        }
        
        return DocumentItemDto.builder()
                .id(item.getId())
                .documentId(item.getDocument() != null ? item.getDocument().getId() : null)
                .name(item.getName())
                .comment(item.getComment())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .discountPercentage(item.getDiscountPercentage())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
    
    /**
     * Maps DocumentItemDto to DocumentItem entity
     */
    private static DocumentItem mapDocumentItemEntity(DocumentItemDto dto) {
        if (dto == null) {
            return null;
        }
        
        return DocumentItem.builder()
                .id(dto.getId())
                .name(dto.getName())
                .comment(dto.getComment())
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .discountPercentage(dto.getDiscountPercentage())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
    
    /**
     * Helper method to safely get company ID, forcing lazy loading if needed
     */
    private static Long getCompanyId(Company company) {
        if (company == null) {
            return null;
        }
        try {
            // Access a non-ID field to trigger lazy loading
            company.getCompanyName(); // This will force the proxy to load
            return company.getId();
        } catch (Exception e) {
            // If lazy loading fails, try to get ID directly
            return company.getId();
        }
    }
}
