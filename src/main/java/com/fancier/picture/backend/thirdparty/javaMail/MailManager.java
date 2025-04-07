package com.fancier.picture.backend.thirdparty.javaMail;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * 邮件服务
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Component
@Slf4j
@RequiredArgsConstructor
public class MailManager {
    private final MailProperties mailProperties;

    private final JavaMailSender mailSender;

    @Async
    public void sendMail(String to, String subject, String message) {

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(mailProperties.getUsername()); // Replace with your own email address
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(message, true); // Set second parameter mailAddress 'true' for HTML content
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        mailSender.send(mimeMessage);
    }
}
