package com.profitmap_backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentClient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic client info (common to both types)
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String contact;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", nullable = false)
    private ClientType clientType;

    // Company-specific fields (nullable)
    @Column
    private String oib;

    @Column
    private String address;

    // Person-specific fields (nullable)
    @Column
    private String surname;

    // Reference to original client (for tracking)
    // Nullable because documents can be created with manually entered client data
    @Column(name = "original_client_id")
    private Long originalClientId;

    // Timestamp when this snapshot was taken
    @CreationTimestamp
    @Column(name = "snapshot_date", nullable = false)
    private LocalDateTime snapshotDate;

    // Helper method to create snapshot from Client
    public static DocumentClient createSnapshot(Client client) {
        DocumentClientBuilder builder = DocumentClient.builder()
                .name(client.getName())
                .contact(client.getContact())
                .email(client.getEmail())
                .clientType(client.getClientType())
                .originalClientId(client.getId());

        // Add type-specific fields
        if (client.getClientType() == ClientType.COMPANY && client.getClientCompany() != null) {
            builder.oib(client.getClientCompany().getOib())
                   .address(client.getClientCompany().getAddress());
        } else if (client.getClientType() == ClientType.PERSON && client.getClientPerson() != null) {
            builder.surname(client.getClientPerson().getSurname());
        }

        return builder.build();
    }
}
