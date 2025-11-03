package com.sapiens.innovate.service.impl;

import com.sapiens.innovate.service.inf.EmailService;
import com.sapiens.innovate.service.inf.GPTProcessor;
import com.sapiens.innovate.vo.ClaimDataVO;
import com.sapiens.innovate.vo.EmailVO;

public class GPTProcessorImpl implements GPTProcessor {
    private static GPTProcessor instance;

    public static synchronized GPTProcessor getInstance() {
        if (instance == null) {
            instance = new GPTProcessorImpl();
        }
        return instance;
    }

    @Override
    public ClaimDataVO analyzeMessage(EmailVO message) throws Exception {
        return null;
    }
}
