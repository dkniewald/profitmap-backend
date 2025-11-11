package com.profitmap_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "client_companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientCompany {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false)
    private String oib;

    @Column(nullable = false)
    private String address;
}
