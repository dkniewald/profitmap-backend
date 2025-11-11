package com.profitmap_backend.dto;

import com.profitmap_backend.model.ClientType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDto {
    private Long id;
    private Long companyId;
    private ClientType clientType;
    private String name;
    private String contact;
    private String email;
    private LocalDateTime createdAt;
    
    // Company-specific fields (only populated when clientType = COMPANY)
    private String oib;
    private String address;
    
    // Person-specific fields (only populated when clientType = PERSON)
    private String surname;
}
