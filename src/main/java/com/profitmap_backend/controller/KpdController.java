package com.profitmap_backend.controller;

import com.profitmap_backend.dto.KpdBatchResult;
import com.profitmap_backend.model.Kpd;
import com.profitmap_backend.service.KpdService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/kpds")
@RequiredArgsConstructor
public class KpdController {
    private final KpdService kpdService;

    @GetMapping
    public ResponseEntity<List<Kpd>> getAll() {
        return ResponseEntity.ok(kpdService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Kpd> getById(@PathVariable Long id) {
        return ResponseEntity.ok(kpdService.getById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Kpd> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(kpdService.getByCode(code));
    }

    @PostMapping
    public ResponseEntity<Kpd> create(@RequestBody Kpd kpd) {
        return ResponseEntity.ok(kpdService.create(kpd));
    }

    @PostMapping("/batch")
    public ResponseEntity<KpdBatchResult> createBatch(@RequestBody List<Kpd> kpds) {
        return ResponseEntity.ok(kpdService.createBatch(kpds));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Kpd> update(@PathVariable Long id, @RequestBody Kpd kpd) {
        return ResponseEntity.ok(kpdService.update(id, kpd));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        kpdService.delete(id);
        return ResponseEntity.ok().build();
    }
}

