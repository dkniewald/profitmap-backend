package com.profitmap_backend.repository;

import com.profitmap_backend.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByOib(String oib);
    boolean existsByOib(String oib);
    
    @Query("SELECT c FROM Company c JOIN c.users u WHERE u.id = :userId")
    Optional<Company> findByUserId(@Param("userId") Long userId);
}
