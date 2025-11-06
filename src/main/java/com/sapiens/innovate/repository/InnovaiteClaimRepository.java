package com.sapiens.innovate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.sapiens.innovate.entity.InnovaiteClaim;
import java.util.List;

@Repository
public interface InnovaiteClaimRepository extends JpaRepository<InnovaiteClaim, Long> {

    List<InnovaiteClaim> findByStatus(String status);

    List<InnovaiteClaim> findBySuccess(boolean success);

    InnovaiteClaim findByClaimNumber(String claimNumber);

    @Query("SELECT COUNT(c) FROM InnovaiteClaim c")
    long getTotalClaims();

    // ✅ Total number of successful claims
    @Query("SELECT COUNT(c) FROM InnovaiteClaim c WHERE c.success = true")
    long getTotalSuccessClaims();

    // ✅ Total number of failed claims
    @Query("SELECT COUNT(c) FROM InnovaiteClaim c WHERE c.success = false")
    long getTotalFailedClaims();
}
