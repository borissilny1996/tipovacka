package sk.tipovacka.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Table(name = "participant")
public class Participant extends PanacheEntity {

    @Column(nullable = false, unique = true)
    public String nickname;

    @Column(nullable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    public static Optional<Participant> findByNickname(String nickname) {
        return find("nickname", nickname).firstResultOptional();
    }

    public static Participant createAndPersist(String nickname) {
        Participant p = new Participant();
        p.nickname = nickname;
        p.persist();
        return p;
    }
}
