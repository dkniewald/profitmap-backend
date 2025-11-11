package com.profitmap_backend.dto;

import com.profitmap_backend.model.Kpd;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpdBatchResult {
    private List<Kpd> successful;
    private List<KpdError> errors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class KpdError {
        private String code;
        private String name;
        private String errorMessage;
    }
}

