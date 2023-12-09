package com.skillbox.cryptobot.model;    /*
 *created by WerWolfe on Subscriber
 */

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "Subscribers")
public class Subscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "user_id")
    private Long userId;
    private Long price;
    @Column(name = "date_notification")
    private LocalDateTime dateNotification;

    public Subscriber(Long userId) {
        this.userId = userId;
    }
}
