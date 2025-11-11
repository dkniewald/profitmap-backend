package com.profitmap_backend.dto;

import com.profitmap_backend.model.DocumentStatus;
import com.profitmap_backend.model.DocumentType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDto {
    private Long id;
    private Long companyId;
    private LocalDate documentDate;
    private LocalDate expirationDate;
    private DocumentType documentType;
    private DocumentStatus status;
    private String documentNumber;
    private List<DocumentItemDto> documentItems;
    private BigDecimal totalPrice;
    private BigDecimal totalPDV;
    private DocumentClientDto documentClient;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
