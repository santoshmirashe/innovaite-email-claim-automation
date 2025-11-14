package com.sapiens.innovate.service;

import com.sapiens.innovate.vo.ClaimDataVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.sapiens.innovate.entity.InnovaiteClaim;
import com.sapiens.innovate.repository.InnovaiteClaimRepository;

@Service
@Transactional
public class InnovaiteClaimService {

    private final InnovaiteClaimRepository claimRepository;

    public InnovaiteClaimService(InnovaiteClaimRepository repository) {
        this.claimRepository = repository;
    }

    public List<InnovaiteClaim> getAllClaims() {
        return claimRepository.findAll();
    }

    public InnovaiteClaim getClaimByNumber(String claimNumber) {
        return claimRepository.findByClaimNumber(claimNumber).orElseThrow(()->new IllegalArgumentException("No such claim found by claim Nr" + claimNumber));
    }

    public List<InnovaiteClaim> getSuccessfulClaims() {
        return claimRepository.findBySuccess(true);
    }

    public List<InnovaiteClaim> getFailedClaims() {
        return claimRepository.findBySuccess(false);
    }

    public InnovaiteClaim saveClaim(InnovaiteClaim claim) {
        claim.setUpdateDate(java.time.LocalDateTime.now());
        return claimRepository.save(claim);
    }

    public void deleteClaim(Long id) {
        claimRepository.deleteById(id);
    }

    public InnovaiteClaim saveDetailsToDB(ClaimDataVO claimDataVO, String content){
        InnovaiteClaim innovaiteClaim = new InnovaiteClaim();
        innovaiteClaim.setEmailContent(content);
        innovaiteClaim.setSuccess(false);
        innovaiteClaim.setIsEmail(false);
        innovaiteClaim.setFraudAnalysis(claimDataVO.getAnalysisResult().toString());;
        innovaiteClaim.setStatus("PENDING");
        innovaiteClaim.setProcessed("PENDING");
        innovaiteClaim.setEventDate(claimDataVO.getIncidentDate());
        innovaiteClaim.setCreatedDate(LocalDateTime.now());
        innovaiteClaim.setUpdateDate(LocalDateTime.now());
        innovaiteClaim.setPolicyNumber(claimDataVO.getPolicyNumber());
        innovaiteClaim.setClaimAmount(null != claimDataVO.getClaimAmount() ? claimDataVO.getClaimAmount(): BigDecimal.ZERO);
        innovaiteClaim.setPhone(claimDataVO.getContactPhone());
        innovaiteClaim.setCustomerName(claimDataVO.getContactName());
        innovaiteClaim.setRequest(claimDataVO.toString());
        claimRepository.save(innovaiteClaim);
        return innovaiteClaim;
    }
}
