package com.profitmap_backend.dto;

import com.profitmap_backend.model.ClientType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentClientDto {
    private Long id;
    private String name;
    private String contact;
    private String email;
    private ClientType clientType;
    
    // Company-specific fields (nullable)
    private String oib;
    private String address;
    
    // Person-specific fields (nullable)
    private String surname;
    
    // Reference to original client
    private Long originalClientId;
    
    // Timestamp when this snapshot was taken
    private LocalDateTime snapshotDate;
}
