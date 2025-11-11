package com.profitmap_backend.repository;

import com.profitmap_backend.model.ClientPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientPersonRepository extends JpaRepository<ClientPerson, Long> {
    ClientPerson findByClientId(Long clientId);
}
