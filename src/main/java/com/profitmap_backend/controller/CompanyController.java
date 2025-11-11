package com.profitmap_backend.controller;

import com.profitmap_backend.dto.CompanyAdminUpdateRequest;
import com.profitmap_backend.dto.CompanyCreationResponse;
import com.profitmap_backend.model.Company;
import com.profitmap_backend.model.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import com.profitmap_backend.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<List<Company>> getAll() {
        return ResponseEntity.ok(companyService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getById(id));
    }

    @GetMapping("/{id}/users")
    public ResponseEntity<List<User>> getUsersByCompanyId(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getUsersByCompanyId(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Company> getByUserId(@PathVariable Long userId) {
        Optional<Company> company = companyService.getByUserId(userId);
        return company.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Company> create(@RequestBody Company company) {
        return ResponseEntity.ok(companyService.create(company));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Company> update(@PathVariable Long id, @RequestBody Company company) {
        return ResponseEntity.ok(companyService.update(id, company));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        companyService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/admin")
    public ResponseEntity<Company> updateAdminFlags(@PathVariable Long id,
                                                    @RequestBody CompanyAdminUpdateRequest request) {
        return ResponseEntity.ok(companyService.updateAdminFields(id, request));
    }

    /**
     * Get PDV (VAT) information for a specific company
     */
    @GetMapping("/{id}/toggle")
    public ResponseEntity<CompanyToggleInfo> getCompanyToggleInfo(@PathVariable Long id) {
        Company company = companyService.getById(id);
        CompanyToggleInfo toggleInfo = new CompanyToggleInfo(
            company.getIsPdvActive(),
            company.getPercentagePdv(),
            company.getIsKpdActive()
        );
        return ResponseEntity.ok(toggleInfo);
    }

    private void setJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Use HTTPS in production
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);
    }

    /**
     * DTO for company PDV information
     */
    public static class CompanyToggleInfo {
        private Boolean isPdvActive;
        private Double percentagePdv;
        private  Boolean isKpdActive;

        public CompanyToggleInfo(Boolean isPdvActive, Double percentagePdv, Boolean isKpdActive) {
            this.isPdvActive = isPdvActive;
            this.percentagePdv = percentagePdv;
            this.isKpdActive = isKpdActive;
        }

        public Boolean getIsPdvActive() {
            return isPdvActive;
        }

        public void setIsPdvActive(Boolean isPdvActive) {
            this.isPdvActive = isPdvActive;
        }

        public Double getPercentagePdv() {
            return percentagePdv;
        }

        public void setPercentagePdv(Double percentagePdv) {
            this.percentagePdv = percentagePdv;
        }

        public Boolean getIsKpdActive() {
            return isKpdActive;
        }

        public void setIsKpdActive(Boolean isKpdActive) {
            this.isKpdActive = isKpdActive;
        }
    }
}
