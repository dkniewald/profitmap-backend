package com.profitmap_backend.repository;

import com.profitmap_backend.model.Kpd;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KpdRepository extends JpaRepository<Kpd, Long> {
    Optional<Kpd> findByCode(String code);
}

