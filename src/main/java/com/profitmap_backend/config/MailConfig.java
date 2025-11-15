package com.profitmap_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties(MailProperties.class)
@RequiredArgsConstructor
public class MailConfig {

    private final MailProperties mailProperties;
    
    @Value("${spring.mail.host}")
    private String mailHost;
    
    @Value("${spring.mail.port}")
    private int mailPort;
    
    @Value("${spring.mail.username}")
    private String mailUsername;
    
    @Value("${spring.mail.password}")
    private String mailPassword;

    /**
     * Primary mail sender bean configured explicitly to ensure Zoho compatibility.
     * This ensures the envelope sender (MAIL FROM) matches the SMTP username.
     */
    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(mailUsername);
        mailSender.setPassword(mailPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug", "false");
        
        // CRITICAL: Set the envelope sender (MAIL FROM) to match SMTP username
        // This is required by Zoho to prevent "553 Sender is not allowed to relay emails" error
        props.put("mail.smtp.from", mailUsername);

        return mailSender;
    }

    /**
     * Second mail sender bean (optional, only created if second account is configured).
     */
    @Bean(name = "secondMailSender")
    @ConditionalOnProperty(prefix = "mail.second-account", name = "username")
    public JavaMailSender secondMailSender() {
        MailProperties.SecondAccount secondAccount = mailProperties.getSecondAccount();
        
        if (secondAccount == null || secondAccount.getUsername() == null || secondAccount.getUsername().isEmpty()) {
            throw new IllegalStateException("Second mail account is not properly configured");
        }

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(secondAccount.getHost() != null ? secondAccount.getHost() : "smtp.zoho.eu");
        mailSender.setPort(secondAccount.getPort() != null ? secondAccount.getPort() : 587);
        mailSender.setUsername(secondAccount.getUsername());
        mailSender.setPassword(secondAccount.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug", "false");
        
        // Set envelope sender for second account as well
        props.put("mail.smtp.from", secondAccount.getUsername());

        return mailSender;
    }
}

