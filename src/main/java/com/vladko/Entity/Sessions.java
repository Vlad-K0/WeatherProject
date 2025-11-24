package com.vladko.Entity;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "sessions")
public class Sessions {
    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(name = "expires_at")
    private Instant expiresAt;
}
