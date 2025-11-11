package com.profitmap_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<User> users;

    @Column(nullable = false)
    private String companyName;

    @Column(unique = true)
    private String oib;

    @Column(columnDefinition = "TEXT")
    private String pdv;

    @Column(nullable = false)
    private Boolean isPdvActive;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isKpdActive = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDemo = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false)
    private Double percentagePdv;

    private String bank;

    private String iban;

    private String swiftBic;

    private String mbs;

    private String responsiblePerson;

    private String phone;

    private String email;

    private String logoPath;

    // Document series configuration
    @Column(name = "offer_prefix", nullable = false)
    private String offerPrefix;

    @Column(name = "offer_year", nullable = false)
    private String offerYear;

    @Column(name = "invoice_prefix", nullable = false)
    private String invoicePrefix;

    @Column(name = "invoice_year", nullable = false)
    private String invoiceYear;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime demoExpiration;
}
