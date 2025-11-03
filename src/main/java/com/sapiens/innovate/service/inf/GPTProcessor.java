package com.sapiens.innovate.service.inf;

import com.sapiens.innovate.vo.ClaimDataVO;
import com.sapiens.innovate.vo.EmailVO;

public interface GPTProcessor {
    ClaimDataVO analyzeMessage(EmailVO message) throws Exception;
}
