package com.profitmap_backend.dto;

import com.profitmap_backend.model.Company;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CompanyCreationResponse {
    private final Company company;
    private final String token;
}

