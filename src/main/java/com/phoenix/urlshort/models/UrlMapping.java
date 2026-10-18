
package com.phoenix.urlshort.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "url_mapping",
        indexes = {
                @Index(
                        name = "idx_url_mappings_mapping",
                        columnList = "mapping",
                        unique = true
                ),
                @Index(
                        name = "idx_url_mappings_hash",
                        columnList = "url_hash",
                        unique = true
                )
        }
)
public class UrlMapping {

    @Id
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(name = "url_hash", nullable = false, length = 64)
    private String urlHash;

    @Column(nullable = false, length = 7)
    private String mapping;
}
