package com.sapiens.innovate.service.inf;

import com.sapiens.innovate.vo.ClaimDataVO;
import com.sapiens.innovate.vo.EmailVO;

import java.util.List;

public interface EmailService {
    public List<EmailVO> getAllEmails();
    public boolean markMessageRead(String messageId);
}
