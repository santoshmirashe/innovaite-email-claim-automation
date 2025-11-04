package com.sapiens.innovate.service;

import com.sapiens.innovate.vo.EmailVO;
import jakarta.mail.*;
import jakarta.mail.Flags.Flag;
import jakarta.mail.search.FlagTerm;
import java.io.IOException;
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

    @Autowired
    private JavaMailSender mailSender;

    private Session createSession() {
        Properties props = new Properties();
        props.put("mail.imap.host", imapHost);
        props.put("mail.imap.port", imapPort);
        props.put("mail.imap.ssl.enable", "true");
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
                int count = Math.min(messages.length, maxMessages);
                for (int i = 0; i < count; i++) {
                    Message msg = messages[i];
                    String from = (msg.getFrom() != null && msg.getFrom().length > 0)
                            ? msg.getFrom()[0].toString()
                            : "unknown";
                    String subject = msg.getSubject();
                    String snippet = extractText(msg);
                    EmailVO emailVO = new EmailVO();
                    emailVO.setMailBody(snippet == null ? "": snippet);
                    emailVO.setSubject(subject);
                    emailVO.setSenderEmailId(from);
                    emailVO.setMessage(msg);
                    result.add(emailVO);
                }
                return result;
            }
        }
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

    private String extractText(Part p) throws MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            Object content = p.getContent();
            return content == null ? "" : content.toString();
        }

        if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                String text = extractText(bp);
                if (text != null && !text.isBlank()) return text;
            }
        } else if (p.isMimeType("message/rfc822")) {
            return extractText((Part) p.getContent());
        }
        return "";
    }

    public void markMessageRead(Message message) throws MessagingException {
        message.setFlag(Flag.SEEN, true);
    }
}
