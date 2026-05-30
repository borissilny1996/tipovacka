package sk.tipovacka.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "competition_guess",
        uniqueConstraints = @UniqueConstraint(columnNames = {"participant_id", "competition_id"}))
public class CompetitionGuess extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id")
    public Participant participant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competition_id")
    public Competition competition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_guess_team_id")
    public Team winnerGuess;

    @Column(nullable = false)
    public LocalDateTime submittedAt = LocalDateTime.now();

    public int totalPoints;

    @OneToMany(mappedBy = "competitionGuess", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<MatchGuess> matchGuesses = new ArrayList<>();

    public static Optional<CompetitionGuess> findByParticipantAndCompetition(
            Participant participant, Competition competition) {
        return find("participant = ?1 and competition = ?2", participant, competition)
                .firstResultOptional();
    }

    public static List<CompetitionGuess> leaderboard(Competition competition) {
        return find("""
                FROM CompetitionGuess cg
                JOIN FETCH cg.participant p
                WHERE cg.competition = ?1
                ORDER BY cg.totalPoints DESC, p.nickname ASC
                """, competition).list();
    }
}
