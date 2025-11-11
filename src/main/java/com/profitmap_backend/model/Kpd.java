package com.profitmap_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kpds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kpd {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;
}

