package com.profitmap_backend.repository;

import com.profitmap_backend.model.Token;
import com.profitmap_backend.model.TokenType;
import com.profitmap_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    
    /**
     * Find token by token hash and type
     */
    Optional<Token> findByTokenHashAndType(String tokenHash, TokenType type);
    
    /**
     * Find valid (unused and not expired) token by hash and type
     */
    @Query("SELECT t FROM Token t WHERE t.tokenHash = :tokenHash " +
           "AND t.type = :type AND t.used = false AND t.expiresAt > :now")
    Optional<Token> findValidToken(@Param("tokenHash") String tokenHash, 
                                   @Param("type") TokenType type, 
                                   @Param("now") LocalDateTime now);
    
    /**
     * Find all valid tokens for a user by type
     */
    @Query("SELECT t FROM Token t WHERE t.user = :user " +
           "AND t.type = :type AND t.used = false AND t.expiresAt > :now")
    java.util.List<Token> findValidTokensByUserAndType(@Param("user") User user, 
                                                        @Param("type") TokenType type, 
                                                        @Param("now") LocalDateTime now);
    
    /**
     * Find all unused tokens by user
     */
    java.util.List<Token> findByUserAndUsedFalse(User user);
    
    /**
     * Find all tokens by user and type
     */
    java.util.List<Token> findByUserAndType(User user, TokenType type);
    
    /**
     * Delete expired tokens
     */
    @Query("DELETE FROM Token t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * Count unused tokens by user and type
     */
    long countByUserAndTypeAndUsedFalse(User user, TokenType type);
}

