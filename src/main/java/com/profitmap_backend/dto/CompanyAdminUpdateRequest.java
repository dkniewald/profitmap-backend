package com.profitmap_backend.dto;

import com.profitmap_backend.model.CompanyStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CompanyAdminUpdateRequest {
    private CompanyStatus status;
    private Boolean isDemo;
    private LocalDateTime demoExpiration;
}

