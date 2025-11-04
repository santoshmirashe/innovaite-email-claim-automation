package com.sapiens.innovate.vo;

import jakarta.mail.Message;
import lombok.*;
import java.io.File;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVO {
    String subject;
    String mailBody;
    String senderEmailId;
    List<String> ccMailId;
    List<String> toMailId;
    String messageID;
    Message message;
    List<File> attachments;
}
