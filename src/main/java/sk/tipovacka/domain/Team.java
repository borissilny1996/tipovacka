package sk.tipovacka.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "team")
public class Team extends PanacheEntity {

    @Column(nullable = false)
    public String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competition_id")
    public Competition competition;

    public static List<Team> findByCompetition(Competition competition) {
        return find("competition = ?1 order by name", competition).list();
    }
}
