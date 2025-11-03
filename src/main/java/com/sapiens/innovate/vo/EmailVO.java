package com.sapiens.innovate.vo;

import com.microsoft.graph.models.Message;

import java.io.File;
import java.util.List;

public class EmailVO {
    String subject;
    String mailBody;
    String senderEmailId;
    List<String> ccMailId;
    List<String> toMailId;
    String messageID;
    Message message;
    List<File> attachments;
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMailBody() {
        return mailBody;
    }

    public void setMailBody(String mailBody) {
        this.mailBody = mailBody;
    }

    public String getSenderEmailId() {
        return senderEmailId;
    }

    public void setSenderEmailId(String senderEmailId) {
        this.senderEmailId = senderEmailId;
    }

    public List<String> getCcMailId() {
        return ccMailId;
    }

    public void setCcMailId(List<String> ccMailId) {
        this.ccMailId = ccMailId;
    }

    public List<String> getToMailId() {
        return toMailId;
    }

    public void setToMailId(List<String> toMailId) {
        this.toMailId = toMailId;
    }

    public List<File> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<File> attachments) {
        this.attachments = attachments;
    }
    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
