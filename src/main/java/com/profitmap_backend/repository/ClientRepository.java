package com.profitmap_backend.repository;

import com.profitmap_backend.model.Client;
import com.profitmap_backend.model.ClientType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByCompanyId(Long companyId);
    List<Client> findByCompanyIdAndClientType(Long companyId, ClientType clientType);
    List<Client> findByClientType(ClientType clientType);
}
