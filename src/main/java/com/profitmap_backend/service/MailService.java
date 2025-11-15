package com.profitmap_backend.service;

import com.profitmap_backend.config.MailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.lang.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;
    private final MailTemplateService mailTemplateService;
    private final MailProperties mailProperties;
    
    @Value("${spring.mail.username}")
    private String smtpUsername;

    @Autowired(required = false)
    @Qualifier("secondMailSender")
    private JavaMailSender secondMailSender;

    public MailService(
            JavaMailSender mailSender,
            MailTemplateService mailTemplateService,
            MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailTemplateService = mailTemplateService;
        this.mailProperties = mailProperties;
    }

    /**
     * Example usage:
     * <pre>
     * // From UserRegistrationService
     * mailService.sendActivationEmail(user.getEmail(), user.getFirstName(), token);
     *
     * // From InvoiceService
     * mailService.sendCustomerInvoiceEmail(
     *     customerEmail,
     *     customerName,
     *     issuerCompanyName,
     *     issuerEmail,
     *     invoiceNumber,
     *     pdfBytes
     * );
     * </pre>
     */

    @Async
    public void sendSystemHtmlMail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            
            // Use SMTP username as From address to match Zoho's requirements
            // Zoho requires the From address to match the authenticated SMTP username
            // The mail.smtp.from property in application.yml sets the envelope sender (MAIL FROM)
            String fromAddress = smtpUsername != null && !smtpUsername.isEmpty() 
                    ? smtpUsername 
                    : mailProperties.getSystemFromAddress();
            
            if (fromAddress == null || fromAddress.isEmpty()) {
                fromAddress = "hello@profitmap.app"; // Fallback to default
            }
            
            helper.setFrom(fromAddress, mailProperties.getAppName());
            log.debug("Sending system email from {} to {}", fromAddress, to);

            mailSender.send(message);
            log.debug("Successfully sent system email to {}", to);
        } catch (MessagingException | MailException | UnsupportedEncodingException ex) {
            log.error("Failed to send system email to {}", to, ex);
            throw new IllegalStateException("Failed to send system email", ex);
        }
    }

    @Async
    public void sendHtmlMailWithPdfAttachment(
            String to,
            String subject,
            String htmlBody,
            byte[] pdfBytes,
            String pdfFileName,
            @Nullable String replyTo,
            @Nullable String fromPersonalName
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            String personalName = (fromPersonalName != null && !fromPersonalName.isBlank())
                    ? fromPersonalName
                    : mailProperties.getAppName();
            helper.setFrom(mailProperties.getFromAddress(), personalName);

            if (replyTo != null && !replyTo.isBlank()) {
                helper.setReplyTo(replyTo);
            }

            helper.addAttachment(pdfFileName, new ByteArrayResource(pdfBytes));

            mailSender.send(message);
        } catch (MessagingException | MailException | UnsupportedEncodingException ex) {
            log.error("Failed to send email with attachment to {}", to, ex);
            throw new IllegalStateException("Failed to send email with attachment", ex);
        }
    }

    /**
     * Sends HTML email with PDF attachment using a template.
     * This is a convenience method that renders the template and then sends the email.
     * 
     * @param to Recipient email address
     * @param subject Email subject
     * @param templateName Name of the template (without .html extension)
     * @param templateVariables Variables to be used in the template
     * @param pdfBytes PDF file content as byte array
     * @param pdfFileName Name of the PDF file attachment
     * @param replyTo Optional reply-to email address
     * @param fromPersonalName Optional sender personal name
     */
    @Async
    public void sendHtmlMailWithPdfAttachment(
            String to,
            String subject,
            String templateName,
            Map<String, Object> templateVariables,
            byte[] pdfBytes,
            String pdfFileName,
            @Nullable String replyTo,
            @Nullable String fromPersonalName
    ) {
        String htmlBody = mailTemplateService.render(templateName, templateVariables);
        sendHtmlMailWithPdfAttachment(
                to,
                subject,
                htmlBody,
                pdfBytes,
                pdfFileName,
                replyTo,
                fromPersonalName
        );
    }

    @Async
    public void sendCustomerInvoiceEmail(
            String customerEmail,
            String customerName,
            String issuerCompanyName,
            String issuerEmail,
            String invoiceNumber,
            byte[] pdfBytes
    ) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", customerName);
        variables.put("issuerCompanyName", issuerCompanyName);
        variables.put("invoiceNumber", invoiceNumber);
        variables.put("appName", mailProperties.getAppName());

        String subject = String.format("Račun br. %s – %s", invoiceNumber, issuerCompanyName);
        String htmlBody = mailTemplateService.render("invoice-email", variables);
        String pdfFileName = "Racun-" + invoiceNumber + ".pdf";
        String personalName = issuerCompanyName + " (via " + mailProperties.getAppName() + ")";

        sendHtmlMailWithPdfAttachment(
                customerEmail,
                subject,
                htmlBody,
                pdfBytes,
                pdfFileName,
                issuerEmail,
                personalName
        );
    }

    @Async
    public void sendActivationEmail(
            String to,
            String userName,
            String activationToken
    ) {
        String activationLink = mailProperties.getActivationBaseUrl() + "?token=" + activationToken;

        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", userName);
        variables.put("activationLink", activationLink);
        variables.put("appName", mailProperties.getAppName());

        String subject = "Aktivirajte svoj račun – " + mailProperties.getAppName();
        String htmlBody = mailTemplateService.render("activation-email", variables);

        sendSystemHtmlMail(to, subject, htmlBody);
    }

    /**
     * Convenience helper for manual testing.
     * Sends a sample activation email to the configured system address.
     */
    @Async
    public void sendTestMail() {
        String subject = "Test email – " + mailProperties.getAppName();

        Map<String, Object> variables = Map.of(
                "userName", "Test korisnik",
                "activationLink", mailProperties.getActivationBaseUrl() + "?token=TEST_TOKEN",
                "appName", mailProperties.getAppName()
        );

        String htmlBody = mailTemplateService.render("activation-email", variables);
        sendSystemHtmlMail("dominikkniewald@gmail.com", subject, htmlBody);
    }

    /**
     * Sends HTML email using the second mail account (if configured).
     * Falls back to primary account if second account is not configured.
     */
    @Async
    public void sendHtmlMailWithSecondAccount(
            String to,
            String subject,
            String htmlBody,
            @Nullable String fromPersonalName
    ) {
        JavaMailSender sender = (secondMailSender != null) ? secondMailSender : mailSender;
        String fromAddress = (mailProperties.getSecondAccount() != null && 
                             mailProperties.getSecondAccount().getFromAddress() != null)
                ? mailProperties.getSecondAccount().getFromAddress()
                : mailProperties.getFromAddress();

        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            String personalName = (fromPersonalName != null && !fromPersonalName.isBlank())
                    ? fromPersonalName
                    : mailProperties.getAppName();
            helper.setFrom(fromAddress, personalName);

            sender.send(message);
        } catch (MessagingException | MailException | UnsupportedEncodingException ex) {
            log.error("Failed to send email with second account to {}", to, ex);
            throw new IllegalStateException("Failed to send email with second account", ex);
        }
    }

    /**
     * Sends HTML email with PDF attachment using the second mail account (if configured).
     * Falls back to primary account if second account is not configured.
     */
    @Async
    public void sendHtmlMailWithPdfAttachmentSecondAccount(
            String to,
            String subject,
            String htmlBody,
            byte[] pdfBytes,
            String pdfFileName,
            @Nullable String replyTo,
            @Nullable String fromPersonalName
    ) {
        JavaMailSender sender = (secondMailSender != null) ? secondMailSender : mailSender;
        String fromAddress = (mailProperties.getSecondAccount() != null && 
                             mailProperties.getSecondAccount().getFromAddress() != null)
                ? mailProperties.getSecondAccount().getFromAddress()
                : mailProperties.getFromAddress();

        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            String personalName = (fromPersonalName != null && !fromPersonalName.isBlank())
                    ? fromPersonalName
                    : mailProperties.getAppName();
            helper.setFrom(fromAddress, personalName);

            if (replyTo != null && !replyTo.isBlank()) {
                helper.setReplyTo(replyTo);
            }

            helper.addAttachment(pdfFileName, new ByteArrayResource(pdfBytes));

            sender.send(message);
        } catch (MessagingException | MailException | UnsupportedEncodingException ex) {
            log.error("Failed to send email with attachment using second account to {}", to, ex);
            throw new IllegalStateException("Failed to send email with attachment using second account", ex);
        }
    }

    /**
     * Sends HTML email with PDF attachment using the second mail account (if configured) with a template.
     * Falls back to primary account if second account is not configured.
     * 
     * @param to Recipient email address
     * @param subject Email subject
     * @param templateName Name of the template (without .html extension)
     * @param templateVariables Variables to be used in the template
     * @param pdfBytes PDF file content as byte array
     * @param pdfFileName Name of the PDF file attachment
     * @param replyTo Optional reply-to email address
     * @param fromPersonalName Optional sender personal name
     */
    @Async
    public void sendHtmlMailWithPdfAttachmentSecondAccount(
            String to,
            String subject,
            String templateName,
            Map<String, Object> templateVariables,
            byte[] pdfBytes,
            String pdfFileName,
            @Nullable String replyTo,
            @Nullable String fromPersonalName
    ) {
        String htmlBody = mailTemplateService.render(templateName, templateVariables);
        sendHtmlMailWithPdfAttachmentSecondAccount(
                to,
                subject,
                htmlBody,
                pdfBytes,
                pdfFileName,
                replyTo,
                fromPersonalName
        );
    }
}

