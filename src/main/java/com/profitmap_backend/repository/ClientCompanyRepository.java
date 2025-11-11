package com.profitmap_backend.repository;

import com.profitmap_backend.model.ClientCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientCompanyRepository extends JpaRepository<ClientCompany, Long> {
    ClientCompany findByClientId(Long clientId);
}
