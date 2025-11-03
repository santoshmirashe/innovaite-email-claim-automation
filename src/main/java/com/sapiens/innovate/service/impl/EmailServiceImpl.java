package com.sapiens.innovate.service.impl;


import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.requests.GraphServiceClient;
import com.sapiens.innovate.service.inf.EmailService;
import com.sapiens.innovate.vo.EmailVO;
import okhttp3.Request;
import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class EmailServiceImpl implements EmailService {

    private GraphServiceClient<Request> graphClient;

    @Autowired
    private final ClientSecretCredential clientSecretCredential;
    private final AtomicReference<String> cachedToken = new AtomicReference<>();
    private OffsetDateTime expiryTime = OffsetDateTime.MIN;
    @Autowired
    private JavaMailSender mailSender;


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

    public EmailServiceImpl(ClientSecretCredential clientSecretCredential) {
        this.clientSecretCredential = clientSecretCredential;
    }

    private String getValidToken() {
        if (OffsetDateTime.now().isBefore(expiryTime)) {
            return cachedToken.get();
        }
        var token = clientSecretCredential.getToken(
                        new TokenRequestContext().addScopes("https://graph.microsoft.com/.default"))
                .block();

        cachedToken.set(token.getToken());
        expiryTime = token.getExpiresAt();
        return cachedToken.get();
    }
    private List<EmailVO> getEmailsFromEmailServer(){
        List<EmailVO> returnAll = new ArrayList<>();
        graphClient.me().mailFolders("Inbox").messages()
                .buildRequest()
                .filter("isRead eq false")
                .top(100)
                .get()
                .getCurrentPage()
                .forEach(msg -> {
                    EmailVO email = new EmailVO();
                    email.setSubject(msg.subject);
                    email.setMailBody(msg.body.content);
                    email.setSenderEmailId(msg.from.emailAddress.address);
                    List<String> toEmails = msg.toRecipients == null ? List.of() :
                            msg.toRecipients.stream()
                                    .map(r -> r.emailAddress.address)
                                    .collect(Collectors.toList());
                    email.setToMailId(toEmails);
                    List<String> ccEmails = msg.ccRecipients == null ? List.of() :
                            msg.ccRecipients.stream()
                                    .map(r -> r.emailAddress.address)
                                    .collect(Collectors.toList());
                    email.setCcMailId(ccEmails);
                    email.setMessage(msg);
                    email.setMessageID(msg.id);

                    returnAll.add(email);
                });
        return returnAll;
    }
    private List<EmailVO> getEmailsForTest(){
        List<EmailVO> returnal = new ArrayList<>();
        EmailVO email = new EmailVO();
        email.setSubject("Create claim for below details");
        email.setMailBody("I was in a car accident on Oct 2nd and need to file a claim for my policy #123456.");
        email.setSenderEmailId("sant@gmail.com");
        List<String> toEmails = List.of(new String[]{"santosh@gmail.com"});
        email.setToMailId(toEmails);
        List<String> ccEmails = List.of(new String[]{"santosh1@gmail.com"});
        email.setCcMailId(ccEmails);

        returnal.add(email);
        return returnal;
    }

    public List<EmailVO> getAllEmails() {
        List<EmailVO> returnAll = new ArrayList<>();
        returnAll = getEmailsForTest();
        return returnAll;
    }
    @Override
    public boolean markMessageRead(String messageId){
        try {
            Message updateMessage = new Message();
            updateMessage.isRead = true;

            graphClient
                    .me()
                    .messages(messageId)
                    .buildRequest()
                    .patch(updateMessage);
        }catch(Exception e){
            return false;
        }
        return true;
    }
}
