package com.sapiens.innovate.vo;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
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

    public void setCcMailRecipients(Address[] allRecipients) {
        if(null != allRecipients) {
            for (Address address : allRecipients) {
                this.getCcMailAddress().add(((InternetAddress) address).getAddress());
            }
        }
    }


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
