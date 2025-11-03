package com.sapiens.innovate.service.inf;

import com.sapiens.innovate.vo.ClaimDataVO;

public interface ClaimService {
    void raiseClaim(ClaimDataVO claimDataVO);

    String processClaims();
}
