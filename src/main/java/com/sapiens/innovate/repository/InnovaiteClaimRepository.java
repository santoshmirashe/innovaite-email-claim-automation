package com.sapiens.innovate.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.sapiens.innovate.entity.InnovaiteClaim;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InnovaiteClaimRepository extends JpaRepository<InnovaiteClaim, Long> {

    List<InnovaiteClaim> findByStatus(String status);

    List<InnovaiteClaim> findBySuccess(boolean success);

    InnovaiteClaim findByClaimNumber(String claimNumber);

    @Query("SELECT COUNT(c) FROM InnovaiteClaim c " +
            "WHERE c.createdDate BETWEEN :from AND :to")
    long getTotalClaims(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // ✅ Total number of successful claims
    @Query("""
       SELECT COUNT(c)
       FROM InnovaiteClaim c
       WHERE c.success = true
         AND c.createdDate BETWEEN :from AND :to
       """)
    long getTotalSuccessClaims(@Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to);


    // ✅ Total number of failed claims
    @Query("""
       SELECT COUNT(c)
       FROM InnovaiteClaim c
       WHERE c.success = false
         AND c.createdDate BETWEEN :from AND :to
       """)
    long getTotalFailedClaims(@Param("from") LocalDateTime from,
                              @Param("to") LocalDateTime to);

    @Query("""
        SELECT c FROM InnovaiteClaim c
        WHERE (:from IS NULL OR c.createdDate >= :from)
          AND (:to IS NULL OR c.createdDate <= :to)
        ORDER BY c.createdDate DESC
    """)
    Page<InnovaiteClaim> findClaims(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

}
