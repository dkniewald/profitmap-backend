package com.profitmap_backend.service;

import com.profitmap_backend.config.MailProperties;
import com.profitmap_backend.model.Token;
import com.profitmap_backend.model.TokenType;
import com.profitmap_backend.model.User;
import com.profitmap_backend.model.UserStatus;
import com.profitmap_backend.repository.TokenRepository;
import com.profitmap_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final MailTemplateService mailTemplateService;
    private final MailProperties mailProperties;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int TOKEN_BYTES = 32; // 256 bits
    private static final int DEFAULT_EXPIRATION_HOURS = 24;
    private static final String FRONTEND_BASE_URL = "http://localhost:3000/";
    
    /**
     * Creates a token for a user with the specified type.
     * Generates a random token, hashes it, and saves it to the database.
     * 
     * @param user The user for whom the token is created
     * @param type The type of token (ACCOUNT_ACTIVATION, PASSWORD_RESET, etc.)
     * @return The raw token string (to be sent to user via email)
     */
    @Transactional
    public String createToken(User user, TokenType type) {
        return createToken(user, type, DEFAULT_EXPIRATION_HOURS);
    }
    
    /**
     * Creates a token for a user with the specified type and expiration time.
     * 
     * @param user The user for whom the token is created
     * @param type The type of token
     * @param expirationHours Number of hours until the token expires
     * @return The raw token string (to be sent to user via email)
     */
    @Transactional
    public String createToken(User user, TokenType type, int expirationHours) {
        // Check for existing valid tokens of the same type for this user
        LocalDateTime now = LocalDateTime.now();
        java.util.List<Token> existingValidTokens = tokenRepository.findValidTokensByUserAndType(user, type, now);
        
        // Mark all existing valid tokens as used (without date) before creating a new one
        if (!existingValidTokens.isEmpty()) {
            for (Token existingToken : existingValidTokens) {
                existingToken.markAsUsedWithoutDate();
                tokenRepository.save(existingToken);
                log.debug("Marked existing {} token as used for user {}", type, user.getEmail());
            }
            log.info("Invalidated {} existing {} token(s) for user {}", existingValidTokens.size(), type, user.getEmail());
        }
        
        // Generate random token
        byte[] tokenBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        
        // Hash the token before storing
        String tokenHash = passwordEncoder.encode(rawToken);
        
        // Create expiration date
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(expirationHours);
        
        // Create and save token entity
        Token token = Token.builder()
                .user(user)
                .tokenHash(tokenHash)
                .type(type)
                .expiresAt(expiresAt)
                .used(false)
                .build();
        
        tokenRepository.save(token);
        
        log.info("Created {} token for user {} (expires at {})", type, user.getEmail(), expiresAt);
        
        // Return the raw token (this is what gets sent to the user)
        return rawToken;
    }
    
    /**
     * Validates a token by checking if it exists, is not used, and not expired.
     * 
     * @param rawToken The raw token string provided by the user
     * @param type The expected token type
     * @return The Token entity if valid, empty otherwise
     */
    @Transactional(readOnly = true)
    public java.util.Optional<Token> validateToken(String rawToken, TokenType type) {
        // We need to check all tokens of this type and compare hashes
        // Since we can't query by hash directly (it's hashed), we need to check all valid tokens
        LocalDateTime now = LocalDateTime.now();
        
        // Get all valid tokens of this type
        java.util.List<Token> validTokens = tokenRepository.findAll().stream()
                .filter(t -> t.getType() == type)
                .filter(t -> !t.getUsed())
                .filter(t -> t.getExpiresAt().isAfter(now))
                .toList();
        
        // Check each token's hash against the provided raw token
        for (Token token : validTokens) {
            if (passwordEncoder.matches(rawToken, token.getTokenHash())) {
                return java.util.Optional.of(token);
            }
        }
        
        return java.util.Optional.empty();
    }
    
    /**
     * Sends activation email with token link.
     * 
     * @param user The user to send the email to
     * @param tokenType The type of token
     * @param path The path part of the URL (e.g., "activation?token=")
     * @param rawToken The raw token to include in the URL
     */
    public void sendTokenEmail(User user, TokenType tokenType, String path, String rawToken) {
        // Build activation link
        String activationLink = FRONTEND_BASE_URL + path + rawToken;
        
        // Prepare template variables
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("userName", user.getFirstName());
        templateVariables.put("activationLink", activationLink);
        templateVariables.put("appName", mailProperties.getAppName());
        
        // Render template
        String htmlBody = mailTemplateService.render("account-activation-email", templateVariables);
        
        // Send email based on token type
        String subject;
        if (tokenType == TokenType.ACCOUNT_ACTIVATION) {
            subject = "Aktivirajte svoj račun – " + mailProperties.getAppName();
        } else if (tokenType == TokenType.PASSWORD_RESET) {
            subject = "Resetirajte lozinku – " + mailProperties.getAppName();
        } else if (tokenType == TokenType.EMAIL_CHANGE) {
            subject = "Potvrdite promjenu email adrese – " + mailProperties.getAppName();
        } else {
            subject = "Važna obavijest – " + mailProperties.getAppName();
        }
        
        mailService.sendSystemHtmlMail(user.getEmail(), subject, htmlBody);
        log.info("Sent {} email to user {}", tokenType, user.getEmail());
    }
    
    /**
     * Activates a user account using an activation token.
     * Validates the token, marks it as used, and sets the user status to ACTIVE.
     * 
     * @param rawToken The raw activation token from the email link
     * @throws IllegalArgumentException if the token is invalid, expired, or already used
     */
    @Transactional
    public void activateUser(String rawToken) {
        // Validate the token
        java.util.Optional<Token> tokenOpt = validateToken(rawToken, TokenType.ACCOUNT_ACTIVATION);
        
        if (tokenOpt.isEmpty()) {
            log.warn("Activation attempt with invalid or expired token");
            throw new IllegalArgumentException("Invalid or expired activation token");
        }
        
        Token token = tokenOpt.get();
        User user = token.getUser();
        
        // Check if user is already active
        if (user.getStatus() == UserStatus.ACTIVE) {
            log.info("User {} is already active, marking token as used anyway", user.getEmail());
            token.markAsUsed();
            tokenRepository.save(token);
            return;
        }
        
        // Mark token as used
        token.markAsUsed();
        tokenRepository.save(token);
        
        // Activate the user
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        
        log.info("Successfully activated user account: {}", user.getEmail());
    }
    
    /**
     * Resends activation email to a user.
     * Only allows resend if user status is PENDING.
     * 
     * @param email The email address of the user
     * @throws IllegalArgumentException if user doesn't exist, is already active, or status is not PENDING
     */
    @Transactional
    public void resendActivationEmail(String email) {
        // Find user by email
        java.util.Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            log.warn("Resend activation attempt for non-existent email: {}", email);
            throw new IllegalArgumentException("User with this email does not exist");
        }
        
        User user = userOpt.get();
        
        // Check if user status is PENDING - only PENDING users can request resend
        if (user.getStatus() != UserStatus.PENDING) {
            if (user.getStatus() == UserStatus.ACTIVE) {
                log.info("Resend activation attempt for already active user: {}", email);
                throw new IllegalArgumentException("User account is already active");
            } else {
                log.info("Resend activation attempt for user with status {}: {}", user.getStatus(), email);
                throw new IllegalArgumentException("Activation email can only be resent for pending accounts");
            }
        }
        
        // Create new activation token
        String activationToken = createToken(user, TokenType.ACCOUNT_ACTIVATION);
        
        // Send activation email
        sendTokenEmail(user, TokenType.ACCOUNT_ACTIVATION, "activation?token=", activationToken);
        
        log.info("Resent activation email to user: {}", email);
    }
}

