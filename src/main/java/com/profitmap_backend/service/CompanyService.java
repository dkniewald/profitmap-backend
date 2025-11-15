package com.profitmap_backend.service;

import com.profitmap_backend.dto.CompanyAdminUpdateRequest;
import com.profitmap_backend.model.Company;
import com.profitmap_backend.model.DocumentSeries;
import com.profitmap_backend.model.User;
import com.profitmap_backend.model.UserRole;
import com.profitmap_backend.repository.CompanyRepository;
import com.profitmap_backend.repository.DocumentSeriesRepository;
import com.profitmap_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final DocumentSeriesRepository documentSeriesRepository;

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

        // Validate document series configuration changes
        validateDocumentSeriesChanges(id, existingCompany, company);

        company.setId(id);
        return companyRepository.save(company);
    }

    /**
     * Validates that changes to prefix/year/startNumber won't cause conflicts.
     * - Warns if changing prefix/year to a combination that already has a series
     * - Prevents changing startNumber if series already exist (to avoid confusion)
     */
    private void validateDocumentSeriesChanges(Long companyId, Company existing, Company updated) {
        // Check invoice prefix/year changes
        boolean invoicePrefixChanged = !existing.getInvoicePrefix().equals(updated.getInvoicePrefix());
        boolean invoiceYearChanged = !existing.getInvoiceYear().equals(updated.getInvoiceYear());
        
        if (invoicePrefixChanged || invoiceYearChanged) {
            // Check if new combination already has a series
            Optional<DocumentSeries> existingSeries = 
                documentSeriesRepository.findByCompanyIdAndPrefixAndYear(
                    companyId, updated.getInvoicePrefix(), updated.getInvoiceYear());
            
            if (existingSeries.isPresent()) {
                throw new RuntimeException(
                    String.format("A document series already exists for prefix '%s' and year '%s'. " +
                        "Changing to this combination will continue from the existing series (next number: %d), " +
                        "not from the configured start number.",
                        updated.getInvoicePrefix(), updated.getInvoiceYear(), 
                        existingSeries.get().getNextNumber()));
            }
        }

        // Check offer prefix/year changes
        boolean offerPrefixChanged = !existing.getOfferPrefix().equals(updated.getOfferPrefix());
        boolean offerYearChanged = !existing.getOfferYear().equals(updated.getOfferYear());
        
        if (offerPrefixChanged || offerYearChanged) {
            Optional<DocumentSeries> existingSeries = 
                documentSeriesRepository.findByCompanyIdAndPrefixAndYear(
                    companyId, updated.getOfferPrefix(), updated.getOfferYear());
            
            if (existingSeries.isPresent()) {
                throw new RuntimeException(
                    String.format("A document series already exists for prefix '%s' and year '%s'. " +
                        "Changing to this combination will continue from the existing series (next number: %d), " +
                        "not from the configured start number.",
                        updated.getOfferPrefix(), updated.getOfferYear(), 
                        existingSeries.get().getNextNumber()));
            }
        }

        // Prevent changing start numbers if any series have documents created (counter > 0)
        Long existingInvoiceStart = existing.getInvoiceStartNumber() != null ? existing.getInvoiceStartNumber() : 1L;
        Long updatedInvoiceStart = updated.getInvoiceStartNumber() != null ? updated.getInvoiceStartNumber() : 1L;
        boolean invoiceStartChanged = !existingInvoiceStart.equals(updatedInvoiceStart);
        if (invoiceStartChanged) {
            // Check if any invoice series have documents created (documentCount > 0)
            boolean hasUsedInvoiceSeries = documentSeriesRepository.findAll().stream()
                .anyMatch(series -> series.getCompanyId().equals(companyId) && 
                    series.getPrefix().equals(existing.getInvoicePrefix()) &&
                    series.getDocumentCount() != null && series.getDocumentCount() > 0);
            
            if (hasUsedInvoiceSeries) {
                throw new RuntimeException(
                    "Cannot change invoice start number because documents have already been created for this prefix. " +
                    "The start number only applies to new series. Existing series with documents cannot be modified.");
            }
        }

        Long existingOfferStart = existing.getOfferStartNumber() != null ? existing.getOfferStartNumber() : 1L;
        Long updatedOfferStart = updated.getOfferStartNumber() != null ? updated.getOfferStartNumber() : 1L;
        boolean offerStartChanged = !existingOfferStart.equals(updatedOfferStart);
        if (offerStartChanged) {
            // Check if any offer series have documents created (documentCount > 0)
            boolean hasUsedOfferSeries = documentSeriesRepository.findAll().stream()
                .anyMatch(series -> series.getCompanyId().equals(companyId) && 
                    series.getPrefix().equals(existing.getOfferPrefix()) &&
                    series.getDocumentCount() != null && series.getDocumentCount() > 0);
            
            if (hasUsedOfferSeries) {
                throw new RuntimeException(
                    "Cannot change offer start number because documents have already been created for this prefix. " +
                    "The start number only applies to new series. Existing series with documents cannot be modified.");
            }
        }
    }

    public void delete(Long id) {
        companyRepository.deleteById(id);
    }

    public Company updateAdminFields(Long id, CompanyAdminUpdateRequest request) {
        Company company = getById(id);

        if (request.getStatus() != null) {
            company.setStatus(request.getStatus());
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
