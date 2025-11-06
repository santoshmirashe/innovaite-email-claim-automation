package com.sapiens.innovate.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.sapiens.innovate.entity.InnovaiteClaim;
import com.sapiens.innovate.repository.InnovaiteClaimRepository;

@Service
@Transactional
public class InnovaiteClaimService {

    private final InnovaiteClaimRepository repository;

    public InnovaiteClaimService(InnovaiteClaimRepository repository) {
        this.repository = repository;
    }

    public List<InnovaiteClaim> getAllClaims() {
        return repository.findAll();
    }

    public InnovaiteClaim getClaimByNumber(String claimNumber) {
        return repository.findByClaimNumber(claimNumber);
    }

    public List<InnovaiteClaim> getSuccessfulClaims() {
        return repository.findBySuccess(true);
    }

    public List<InnovaiteClaim> getFailedClaims() {
        return repository.findBySuccess(false);
    }

    public InnovaiteClaim saveClaim(InnovaiteClaim claim) {
        claim.setUpdateDate(java.time.LocalDateTime.now());
        return repository.save(claim);
    }

    public void deleteClaim(Long id) {
        repository.deleteById(id);
    }
}
