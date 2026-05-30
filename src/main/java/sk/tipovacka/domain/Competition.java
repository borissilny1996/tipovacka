package sk.tipovacka.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "competition")
public class Competition extends PanacheEntity {

    @Column(nullable = false)
    public String name;

    public String sport;

    @Column(name = "competition_year")
    public Integer year;

    @Column(nullable = false)
    public LocalDateTime submissionDeadline;

    public boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actual_winner_id")
    public Team actualWinner;

    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("name")
    public List<Team> teams = new ArrayList<>();

    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    public List<Match> matches = new ArrayList<>();

    public boolean isDeadlinePassed() {
        return LocalDateTime.now().isAfter(submissionDeadline);
    }

    public static Optional<Competition> findActive() {
        return find("active = true order by id desc").firstResultOptional();
    }

    public static List<Competition> listActive() {
        return find("active = true order by id desc").list();
    }

    public static List<Competition> listAllOrdered() {
        return find("order by id desc").list();
    }
}
