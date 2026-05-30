package sk.tipovacka.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "match_guess")
public class MatchGuess extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competition_guess_id")
    public CompetitionGuess competitionGuess;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id")
    public Match match;

    public Integer homeScoreGuess;

    public Integer awayScoreGuess;

    public Integer points;
}
