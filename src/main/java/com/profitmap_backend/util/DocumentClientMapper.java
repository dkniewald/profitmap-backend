package com.profitmap_backend.util;

import com.profitmap_backend.dto.DocumentClientDto;
import com.profitmap_backend.model.DocumentClient;

/**
 * Utility class for mapping between DocumentClient entity and DTO
 */
public class DocumentClientMapper {
    
    /**
     * Maps DocumentClient entity to DocumentClientDto
     */
    public static DocumentClientDto toDto(DocumentClient documentClient) {
        if (documentClient == null) {
            return null;
        }
        
        return DocumentClientDto.builder()
                .id(documentClient.getId())
                .name(documentClient.getName())
                .contact(documentClient.getContact())
                .email(documentClient.getEmail())
                .clientType(documentClient.getClientType())
                .oib(documentClient.getOib())
                .address(documentClient.getAddress())
                .surname(documentClient.getSurname())
                .originalClientId(documentClient.getOriginalClientId())
                .snapshotDate(documentClient.getSnapshotDate())
                .build();
    }
    
    /**
     * Maps DocumentClientDto to DocumentClient entity
     */
    public static DocumentClient toEntity(DocumentClientDto dto) {
        if (dto == null) {
            return null;
        }
        
        return DocumentClient.builder()
                .id(dto.getId())
                .name(dto.getName())
                .contact(dto.getContact())
                .email(dto.getEmail())
                .clientType(dto.getClientType())
                .oib(dto.getOib())
                .address(dto.getAddress())
                .surname(dto.getSurname())
                .originalClientId(dto.getOriginalClientId())
                .snapshotDate(dto.getSnapshotDate())
                .build();
    }
}
