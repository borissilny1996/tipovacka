package sk.tipovacka.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "matches")
public class Match extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competition_id")
    public Competition competition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "home_team_id")
    public Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "away_team_id")
    public Team awayTeam;

    public Integer homeScore;

    public Integer awayScore;

    public boolean hasResult() {
        return homeScore != null && awayScore != null;
    }

    public MatchOutcome outcome() {
        if (!hasResult()) return null;
        if (homeScore > awayScore) return MatchOutcome.HOME_WIN;
        if (awayScore > homeScore) return MatchOutcome.AWAY_WIN;
        return MatchOutcome.DRAW;
    }

    public String label() {
        return homeTeam.name + " vs " + awayTeam.name;
    }

    public static List<Match> findByCompetition(Competition competition) {
        return find("competition = ?1 order by id", competition).list();
    }
}
