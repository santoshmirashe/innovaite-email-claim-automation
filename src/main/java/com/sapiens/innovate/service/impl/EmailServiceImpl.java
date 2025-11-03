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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class EmailServiceImpl implements EmailService {

    private final GraphServiceClient<Request> graphClient;
    private final ClientSecretCredential credential;
    private final AtomicReference<String> cachedToken = new AtomicReference<>();
    private OffsetDateTime expiryTime = OffsetDateTime.MIN;
    private static EmailService instance;
    @Autowired
    private JavaMailSender mailSender;

    public static synchronized EmailService getInstance() {
        if (instance == null) {
            instance = new EmailServiceImpl();
        }
        return instance;
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


    private EmailServiceImpl() {
        credential = new ClientSecretCredentialBuilder()
                .clientId("YOUR_CLIENT_ID")
                .clientSecret("YOUR_CLIENT_SECRET")
                .tenantId("YOUR_TENANT_ID")
                .build();

        IAuthenticationProvider authProvider = request -> {
            String token = credential
                    .getToken(new TokenRequestContext()
                            .addScopes("https://graph.microsoft.com/.default"))
                    .block()
                    .getToken();

            //request.addHeader("Authorization", "Bearer " + token);
            return CompletableFuture.completedFuture(null);
        };

        this.graphClient = GraphServiceClient.builder()
                .authenticationProvider(authProvider)
                .buildClient();
    }

    private String getValidToken() {
        if (OffsetDateTime.now().isBefore(expiryTime)) {
            return cachedToken.get();
        }
        var token = credential.getToken(
                        new TokenRequestContext().addScopes("https://graph.microsoft.com/.default"))
                .block();

        cachedToken.set(token.getToken());
        expiryTime = token.getExpiresAt();
        return cachedToken.get();
    }

    public List<EmailVO> getAllEmails() {
        List<EmailVO> returnal = new ArrayList<>();
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

                    returnal.add(email);
                });
        return returnal;
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
