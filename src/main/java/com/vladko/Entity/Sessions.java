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
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    private Instant expiresAt;
}
