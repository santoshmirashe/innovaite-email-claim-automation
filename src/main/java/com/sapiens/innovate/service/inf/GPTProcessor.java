package com.sapiens.innovate.service.inf;

import com.sapiens.innovate.vo.ClaimDataVO;
import jakarta.jms.Message;

public interface GPTProcessor {
    ClaimDataVO analyzeMessage(Message message) throws Exception;
}
