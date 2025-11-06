package com.sapiens.innovate.service;

import com.sapiens.innovate.vo.EmailVO;
import jakarta.mail.*;
import jakarta.mail.Flags.Flag;
import jakarta.mail.search.FlagTerm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class GmailService {

    @Value("${mail.imap.host}")
    private String imapHost;

    @Value("${mail.imap.port}")
    private String imapPort;

    @Value("${mail.imap.user}")
    private String username;

    @Value("${mail.imap.password}")
    private String password;

    @Value("${mail.imap.folder:INBOX}")
    private String folderName;

    @Value("${mail.imap.ssl.enable}")
    private String sslEnabled;

    @Autowired
    private JavaMailSender mailSender;

    private Session createSession() {
        Properties props = new Properties();
        props.put("mail.imap.host", imapHost);
        props.put("mail.imap.port", imapPort);
        props.put("mail.imap.ssl.enable", sslEnabled);
        // If using OAuth2 token instead of password, you may need additional configuration.
        return Session.getInstance(props);
    }

    public List<EmailVO> fetchUnreadEmails(int maxMessages) throws MessagingException, IOException {
        Session session = createSession();
        try (Store store = session.getStore("imap")) {
            store.connect(imapHost, Integer.parseInt(imapPort), username, password);
            try (Folder folder = store.getFolder(folderName)) {
                folder.open(Folder.READ_WRITE);
                // search unread messages
                Message[] messages = folder.search(new FlagTerm(new Flags(Flag.SEEN), false));
                List<EmailVO> result = new ArrayList<>();
                extractDataFromEmail(maxMessages, messages, result);
                return result;
            }
        }
    }

    private void extractDataFromEmail(int maxMessages, Message[] messages, List<EmailVO> result) throws MessagingException, IOException {
        int count = Math.min(messages.length, maxMessages);
        for (int i = 0; i < count; i++) {
            Message message = messages[i];
            String from = (message.getFrom() != null && message.getFrom().length > 0)
                    ? message.getFrom()[0].toString()
                    : "Unknown Sender";
            String subject = message.getSubject();
            String snippet = extractTextFromMail(message);
            EmailVO emailVO = new EmailVO();
            emailVO.setMailBody(snippet == null ? "": snippet);
            emailVO.setMailSubject(subject);
            emailVO.setSenderEmailAddress(from);
            emailVO.setMessage(message);
            extractAttachmentsFromMail(emailVO,message);
            result.add(emailVO);
        }
    }

    private String extractTextFromMail(Part p) throws MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            Object content = p.getContent();
            return content == null ? "" : content.toString();
        }
        if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                String text = extractTextFromMail(bp);
                if (text != null && !text.isBlank()) return text;
            }
        } else if (p.isMimeType("message/rfc822")) {
            return extractTextFromMail((Part) p.getContent());
        }
        return "";
    }

    private void extractAttachmentsFromMail(EmailVO email, Message message) throws MessagingException, IOException {
        List<EmailVO.EmailAttachment> attachments = new ArrayList<>();

        Object content = message.getContent();
        if (content instanceof Multipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart part = multipart.getBodyPart(i);

                // Check if this part is an attachment
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) || part.getFileName() != null) {
                    EmailVO.EmailAttachment attachment = new EmailVO.EmailAttachment();
                    attachment.setFilename(part.getFileName());
                    attachment.setContentType(part.getContentType());

                    // Convert attachment content to byte[]
                    try (InputStream is = part.getInputStream();
                         ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                        byte[] data = new byte[4096];
                        int nRead;
                        while ((nRead = is.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, nRead);
                        }
                        attachment.setContent(buffer.toByteArray());
                    }

                    attachments.add(attachment);
                }
            }
        }
        email.setAttachments(attachments);
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("Email sent successfully to " + to);
        } catch (Exception e) {
            System.out.println("Error sending email: " + e.getMessage());
        }
    }

    public void markMessageRead(Message message) throws MessagingException {
        message.setFlag(Flag.SEEN, true);
    }
}