package com.ntcruise.repository;

import com.ntcruise.model.Yacht;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for NCYACHT DDM (Adabas DBID 012, File 042).
 * Maps Natural FIND operations on NCYACHT to Spring Data JPA queries.
 */
@Repository
public interface YachtRepository extends JpaRepository<Yacht, Long> {

    /**
     * Count all yacht records for data migration validation.
     */
    long count();
}
