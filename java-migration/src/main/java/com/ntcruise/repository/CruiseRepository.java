package com.ntcruise.repository;

import com.ntcruise.model.Cruise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository for NCCRUISE DDM (Adabas DBID 012, File 041).
 * Maps Natural FIND/READ operations to Spring Data JPA queries.
 */
@Repository
public interface CruiseRepository extends JpaRepository<Cruise, Long> {

    /**
     * Maps: READ (N) NCCRUISE — paginated read of all cruises.
     * Used by NCATENDP.NSP (line 36), NCATTOPP.NSP (line 29),
     * NCDEDISP.NSP (line 19), NCSYSVP.NSP (line 20).
     */
    @Query("SELECT c FROM Cruise c LEFT JOIN FETCH c.yacht")
    Page<Cruise> findAllWithYacht(Pageable pageable);

    /**
     * Count all cruise records for data migration validation.
     */
    long count();
}
