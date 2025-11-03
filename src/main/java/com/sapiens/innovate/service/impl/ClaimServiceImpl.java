package com.sapiens.innovate.service.impl;

import com.sapiens.innovate.service.inf.ClaimService;
import com.sapiens.innovate.service.inf.EmailService;
import com.sapiens.innovate.vo.ClaimDataVO;

public class ClaimServiceImpl implements ClaimService {

    private static ClaimService instance;

    public static synchronized ClaimService getInstance() {
        if (instance == null) {
            instance = new ClaimServiceImpl();
        }
        return instance;
    }
    @Override
    public void raiseClaim(ClaimDataVO claimDataVO) {

    }

    @Override
    public String processClaims() {
        return null;
    }
}
