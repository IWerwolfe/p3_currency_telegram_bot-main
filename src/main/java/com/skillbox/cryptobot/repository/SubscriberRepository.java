package com.skillbox.cryptobot.repository;    /*
 *created by WerWolfe on SubscriberRepository
 */

import com.skillbox.cryptobot.model.Subscriber;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriberRepository extends CrudRepository<Subscriber, UUID> {

    @Query("""
            select s from Subscriber s
            where s.price is not null and s.price <= ?1 and (s.dateNotification is null or s.dateNotification < ?2)""")
    List<Subscriber> findActiveSubscribers(Long price, LocalDateTime dateNotification);

    @Query("select s from Subscriber s where s.userId = ?1")
    Optional<Subscriber> findByUserId(Long userId);

}
