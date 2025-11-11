package com.profitmap_backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Mail configuration properties.
 *
 * <p>Example {@code application.yml} snippet:
 *
 * <pre>
 * spring:
 *   mail:
 *     host: smtp.mycompany.com
 *     port: 587
 *     username: invoices@myapp.com
 *     password: ${MAIL_PASSWORD}
 *     properties:
 *       mail:
 *         smtp:
 *           auth: true
 *           starttls:
 *             enable: true
 *
 * mail:
 *   from-address: "invoices@myapp.com"
 *   system-from-address: "no-reply@myapp.com"
 *   app-name: "MyApp"
 *   activation-base-url: "https://app.myapp.com/auth/activate"
 * </pre>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "mail")
public class MailProperties {

    /**
     * Technical sender address used for customer-facing emails,
     * e.g. invoices and offers.
     */
    private String fromAddress;

    /**
     * System sender address used for activation and other system emails.
     */
    private String systemFromAddress;

    /**
     * Application display name (e.g. "MyApp" or "ProfitMap").
     */
    private String appName;

    /**
     * Base URL used when building activation links.
     */
    private String activationBaseUrl;
}

