package com.profitmap_backend.controller;

import com.profitmap_backend.model.ClientType;
import com.profitmap_backend.model.Document;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDocumentRequest {
    private Document document;
    
    // Company ID - required to specify which company the document belongs to
    private Long companyId;
    
    // Client data - always provided directly
    private String name;
    private String contact;
    private String email;
    private ClientType clientType;
    
    // Company-specific fields (nullable)
    private String oib;
    private String address;
    
    // Person-specific fields (nullable)
    private String surname;
}
