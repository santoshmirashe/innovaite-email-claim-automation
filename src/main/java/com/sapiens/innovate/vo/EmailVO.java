package com.sapiens.innovate.vo;

import jakarta.mail.Message;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class EmailVO {
    String mailSubject;
    String mailBody;
    String senderEmailAddress;
    List<String> ccMailAddress;
    List<String> toMailAddress;
    String messageID;
    Message message;
    List<EmailAttachment> attachments;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmailAttachment {
        private String filename;
        private String contentType;
        private byte[] content;
    }
}
