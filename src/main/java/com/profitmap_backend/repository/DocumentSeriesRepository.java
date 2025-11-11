package com.profitmap_backend.repository;

import com.profitmap_backend.model.DocumentSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface DocumentSeriesRepository extends JpaRepository<DocumentSeries, Long> {
    
    Optional<DocumentSeries> findByCompanyIdAndPrefixAndYear(Long companyId, String prefix, String year);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ds FROM DocumentSeries ds WHERE ds.companyId = :companyId AND ds.prefix = :prefix AND ds.year = :year")
    Optional<DocumentSeries> findByCompanyIdAndPrefixAndYearWithLock(@Param("companyId") Long companyId, 
                                                                    @Param("prefix") String prefix, 
                                                                    @Param("year") String year);
}
