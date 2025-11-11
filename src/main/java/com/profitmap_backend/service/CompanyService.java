package com.profitmap_backend.service;

import com.profitmap_backend.dto.CompanyAdminUpdateRequest;
import com.profitmap_backend.dto.CompanyCreationResponse;
import com.profitmap_backend.model.Company;
import com.profitmap_backend.model.User;
import com.profitmap_backend.model.UserRole;
import com.profitmap_backend.repository.CompanyRepository;
import com.profitmap_backend.repository.UserRepository;
import com.profitmap_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public List<Company> getAll() {
        return companyRepository.findAll();
    }

    public Company getById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    public List<User> getUsersByCompanyId(Long companyId) {
        return userRepository.findByCompanyId(companyId);
    }

    public Optional<Company> getByUserId(Long userId) {
        return companyRepository.findByUserId(userId);
    }

    public Company create(Company company) {
        // Check if OIB already exists
        if (companyRepository.existsByOib(company.getOib())) {
            throw new RuntimeException("Company with this OIB already exists");
        }
        
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Find the user by username
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        
        // Check if user is already assigned to a company
        if (currentUser.getCompany() != null) {
            throw new RuntimeException("User is already assigned to a company");
        }
        
        // Default toggles
        if (company.getIsPdvActive() == null) {
            company.setIsPdvActive(Boolean.FALSE);
        }
        if (company.getIsKpdActive() == null) {
            company.setIsKpdActive(Boolean.FALSE);
        }

        // Set default series configuration if not provided
        if (company.getOfferPrefix() == null) {
            company.setOfferPrefix("OFF");
        }
        if (company.getOfferYear() == null) {
            company.setOfferYear(String.valueOf(java.time.LocalDate.now().getYear()));
        }
        if (company.getInvoicePrefix() == null) {
            company.setInvoicePrefix("INV");
        }
        if (company.getInvoiceYear() == null) {
            company.setInvoiceYear(String.valueOf(java.time.LocalDate.now().getYear()));
        }

        company.setDemoExpiration(java.time.LocalDateTime.now().plusDays(30));
        
        // Save the company first
        Company savedCompany = companyRepository.save(company);
        
        // Assign current user to the company
        currentUser.setCompany(savedCompany);
        currentUser.getRoles().add(UserRole.OWNER);
        userRepository.save(currentUser);
        
        return savedCompany;
    }

    public Company update(Long id, Company company) {
        Company existingCompany = getById(id);
        
        // Check if OIB is being changed and if new OIB already exists
        if (!existingCompany.getOib().equals(company.getOib()) && 
            companyRepository.existsByOib(company.getOib())) {
            throw new RuntimeException("Company with this OIB already exists");
        }
        if (company.getIsPdvActive() == null) {
            company.setIsPdvActive(existingCompany.getIsPdvActive());
        }
        if (company.getIsKpdActive() == null) {
            company.setIsKpdActive(existingCompany.getIsKpdActive());
        }

        company.setId(id);
        return companyRepository.save(company);
    }

    public void delete(Long id) {
        companyRepository.deleteById(id);
    }

    public Company updateAdminFields(Long id, CompanyAdminUpdateRequest request) {
        Company company = getById(id);

        if (request.getIsActive() != null) {
            company.setIsActive(request.getIsActive());
        }

        if (request.getIsDemo() != null) {
            company.setIsDemo(request.getIsDemo());
        }

        if (request.getDemoExpiration() != null) {
            company.setDemoExpiration(request.getDemoExpiration());
        }

        return companyRepository.save(company);
    }
}
