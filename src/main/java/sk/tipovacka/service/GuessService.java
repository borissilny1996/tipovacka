package sk.tipovacka.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import sk.tipovacka.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class GuessService {

    @Inject
    ScoringService scoringService;

    @Transactional
    public CompetitionGuess submit(
            String nickname,
            Competition competition,
            Team winnerGuess,
            Map<Long, int[]> scoresByMatchId) {

        if (competition.isDeadlinePassed()) {
            throw new IllegalStateException("Už nie je možné pridať tipy.");
        }

        Optional<Participant> existingParticipant = Participant.findByNickname(nickname);
        if (existingParticipant.isPresent()) {
            throw new IllegalStateException("Toto meno už niekto používa.");
        }

        Participant participant = Participant.createAndPersist(nickname);

        CompetitionGuess guess = CompetitionGuess
                .findByParticipantAndCompetition(participant, competition)
                .orElseGet(() -> {
                    CompetitionGuess g = new CompetitionGuess();
                    g.participant = participant;
                    g.competition = competition;
                    return g;
                });

        guess.winnerGuess = winnerGuess;
        guess.submittedAt = LocalDateTime.now();
        guess.matchGuesses.clear();

        List<Match> matches = Match.findByCompetition(competition);
        for (Match match : matches) {
            int[] scores = scoresByMatchId.get(match.id);
            if (scores == null) continue;
            MatchGuess mg = new MatchGuess();
            mg.competitionGuess = guess;
            mg.match = match;
            mg.homeScoreGuess = scores[0];
            mg.awayScoreGuess = scores[1];
            mg.points = match.hasResult() ? scoringService.scoreMatchGuess(mg, match) : null;
            guess.matchGuesses.add(mg);
        }

        recalculateTotalPoints(guess, competition);
        guess.persist();
        return guess;
    }

    @Transactional
    public void recalculatePoints(Competition competition) {
        List<CompetitionGuess> guesses = CompetitionGuess.find("competition", competition).list();
        for (CompetitionGuess guess : guesses) {
            for (MatchGuess mg : guess.matchGuesses) {
                if (mg.match.hasResult()) {
                    mg.points = scoringService.scoreMatchGuess(mg, mg.match);
                }
            }
            recalculateTotalPoints(guess, competition);
        }
    }

    private void recalculateTotalPoints(CompetitionGuess guess, Competition competition) {
        int total = guess.matchGuesses.stream()
                .mapToInt(mg -> mg.points != null ? mg.points : 0)
                .sum();
        total += scoringService.scoreTournamentWinner(guess.winnerGuess, competition.actualWinner);
        guess.totalPoints = total;
    }
}
