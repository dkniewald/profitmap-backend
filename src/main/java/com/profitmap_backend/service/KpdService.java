package com.profitmap_backend.service;

import com.profitmap_backend.dto.KpdBatchResult;
import com.profitmap_backend.model.Kpd;
import com.profitmap_backend.repository.KpdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class KpdService {
    private final KpdRepository kpdRepository;

    public List<Kpd> getAll() {
        return kpdRepository.findAll();
    }

    public Kpd getById(Long id) {
        return kpdRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KPD not found"));
    }

    public Kpd getByCode(String code) {
        return kpdRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("KPD not found with code: " + code));
    }

    public Kpd create(Kpd kpd) {
        // Check if code already exists
        if (kpdRepository.findByCode(kpd.getCode()).isPresent()) {
            throw new RuntimeException("KPD with code '" + kpd.getCode() + "' already exists");
        }
        return kpdRepository.save(kpd);
    }

    public KpdBatchResult createBatch(List<Kpd> kpds) {
        List<Kpd> successful = new ArrayList<>();
        List<KpdBatchResult.KpdError> errors = new ArrayList<>();
        Set<String> processedCodes = new HashSet<>(); // Track codes in this batch
        
        for (Kpd kpd : kpds) {
            try {
                // Validate code
                if (kpd.getCode() == null || kpd.getCode().trim().isEmpty()) {
                    errors.add(KpdBatchResult.KpdError.builder()
                            .code(kpd.getCode())
                            .name(kpd.getName())
                            .errorMessage("KPD code cannot be null or empty")
                            .build());
                    continue;
                }
                
                // Check for duplicate codes within the batch
                if (processedCodes.contains(kpd.getCode())) {
                    errors.add(KpdBatchResult.KpdError.builder()
                            .code(kpd.getCode())
                            .name(kpd.getName())
                            .errorMessage("Duplicate code found in the batch")
                            .build());
                    continue;
                }
                
                // Check if code already exists in database
                if (kpdRepository.findByCode(kpd.getCode()).isPresent()) {
                    errors.add(KpdBatchResult.KpdError.builder()
                            .code(kpd.getCode())
                            .name(kpd.getName())
                            .errorMessage("KPD with code '" + kpd.getCode() + "' already exists")
                            .build());
                    continue;
                }
                
                // Save valid KPD
                Kpd saved = kpdRepository.save(kpd);
                successful.add(saved);
                processedCodes.add(kpd.getCode());
                
            } catch (Exception e) {
                errors.add(KpdBatchResult.KpdError.builder()
                        .code(kpd.getCode())
                        .name(kpd.getName())
                        .errorMessage(e.getMessage())
                        .build());
            }
        }
        
        return KpdBatchResult.builder()
                .successful(successful)
                .errors(errors)
                .build();
    }

    public Kpd update(Long id, Kpd kpd) {
        if (!kpdRepository.existsById(id)) {
            throw new RuntimeException("KPD not found");
        }
        
        // Check if code is being changed and if new code already exists
        Kpd existing = kpdRepository.findById(id).orElseThrow();
        if (!existing.getCode().equals(kpd.getCode())) {
            if (kpdRepository.findByCode(kpd.getCode()).isPresent()) {
                throw new RuntimeException("KPD with code '" + kpd.getCode() + "' already exists");
            }
        }
        
        kpd.setId(id);
        return kpdRepository.save(kpd);
    }

    public void delete(Long id) {
        if (!kpdRepository.existsById(id)) {
            throw new RuntimeException("KPD not found");
        }
        kpdRepository.deleteById(id);
    }
}

