package com.sapiens.innovate.service.impl;

import com.sapiens.innovate.service.inf.GPTProcessor;
import com.sapiens.innovate.vo.ClaimDataVO;
import jakarta.jms.Message;

public class GPTProcessorImpl implements GPTProcessor {

    @Override
    public ClaimDataVO analyzeMessage(Message message) throws Exception {
        return null;
    }
}
