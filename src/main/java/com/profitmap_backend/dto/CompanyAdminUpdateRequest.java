package com.profitmap_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CompanyAdminUpdateRequest {
    private Boolean isActive;
    private Boolean isDemo;
    private LocalDateTime demoExpiration;
}

