package com.profitmap_backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDto {
    private Long id;
    private String companyName;
    private String oib;
    private String pdv;
    private Boolean isPdvActive;
    private Boolean isKpdActive;
    private Double percentagePdv;
    private String bank;
    private String iban;
    private String swiftBic;
    private String mbs;
    private String responsiblePerson;
    private String phone;
    private String email;
    private String logoPath;
    private String offerPrefix;
    private String offerYear;
    private String invoicePrefix;
    private String invoiceYear;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
