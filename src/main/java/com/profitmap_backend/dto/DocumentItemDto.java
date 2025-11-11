package com.profitmap_backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentItemDto {
    private Long id;
    private Long documentId;
    private String name;
    private String comment;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal discountPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
