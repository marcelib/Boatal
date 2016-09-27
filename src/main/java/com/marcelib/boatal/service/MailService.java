package com.marcelib.boatal.service;

import com.marcelib.boatal.config.JHipsterProperties;
import com.marcelib.boatal.domain.User;

import org.apache.commons.lang3.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;


import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Locale;
import java.util.Properties;

@Service
public class MailService {

    private final Logger log = LoggerFactory.getLogger(MailService.class);

    private static final String USER = "user";
    private static final String BASE_URL = "baseUrl";
    private static final boolean IS_MULTIPART = false;
    private static final boolean IS_HTML = true;

    private final JHipsterProperties jHipsterProperties;

    private final JavaMailSenderImpl javaMailSender;

    private final MessageSource messageSource;

    private final SpringTemplateEngine templateEngine;

    @Inject
    public MailService(SpringTemplateEngine templateEngine, JavaMailSenderImpl javaMailSender,
                       JHipsterProperties jHipsterProperties, MessageSource messageSource) {

        this.templateEngine = templateEngine;
        this.javaMailSender = javaMailSender;
        this.jHipsterProperties = jHipsterProperties;
        this.messageSource = messageSource;
    }

    @Async
    public void sendActivationEmail(User user, String baseUrl) {
        log.debug("Sending activation e-mail to '{}'", user.getEmail()); //only when logging level is set to debug
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        sendEmail(
            user.getEmail(),
            messageSource.getMessage("email.reset.title", null, locale),
            templateEngine.process("activationEmail", generateContext(locale, user, baseUrl)),
            IS_MULTIPART,
            IS_HTML
        );
    }

    @Async
    public void sendCreationEmail(User user, String baseUrl) {
        log.debug("Sending creation e-mail to '{}'", user.getEmail()); //only when logging level is set to debug
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        sendEmail(
            user.getEmail(),
            messageSource.getMessage("email.reset.title", null, locale),
            templateEngine.process("creationEmail", generateContext(locale, user, baseUrl)),
            IS_MULTIPART,
            IS_HTML
        );
    }

    @Async
    public void sendPasswordResetMail(User user, String baseUrl) {
        log.debug("Sending password reset e-mail to '{}'", user.getEmail()); //only when logging level is set to debug
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        sendEmail(
            user.getEmail(),
            messageSource.getMessage("email.reset.title", null, locale),
            templateEngine.process("passwordResetEmail", generateContext(locale, user, baseUrl)),
            IS_MULTIPART,
            IS_HTML
        );
    }

    private Context generateContext(Locale locale, User user, String baseUrl) {
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, baseUrl);
        return context;
    }

    @Async
    private void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        try {
            javaMailSender.setJavaMailProperties(generateProperties());
            javaMailSender.send(generateMimeMessage(to, subject, content, isMultipart, isHtml));
            log.debug("Sent e-mail to User '{}'", to); //only when logging level is set to debug
        } catch (MessagingException e) {
            log.warn("E-mail could not be sent to user '{}', exception is: {}", to, e.getMessage());
        }
    }

    private MimeMessage generateMimeMessage(String to, String subject, String content,
                                            boolean isMultipart, boolean isHtml) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, CharEncoding.UTF_8);
        message.setTo(to);
        message.setFrom(jHipsterProperties.getMail().getFrom());
        message.setSubject(subject);
        message.setText(content, isHtml);
        return mimeMessage;
    }

    private Properties generateProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.quitwait", "true");
        return properties;
    }
}
