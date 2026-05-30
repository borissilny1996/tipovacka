package sk.tipovacka.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import sk.tipovacka.config.ScoringConfig;
import sk.tipovacka.domain.*;

@ApplicationScoped
public class ScoringService {

    @Inject
    ScoringConfig config;

    public int scoreMatchGuess(MatchGuess guess, Match match) {
        int points = 0;
        if (!match.hasResult() || guess.homeScoreGuess == null || guess.awayScoreGuess == null) {
            return points;
        }
        if (guess.homeScoreGuess.equals(match.homeScore) && guess.awayScoreGuess.equals(match.awayScore)) {
            points += config.exactScore();
        }
        if (outcome(guess.homeScoreGuess, guess.awayScoreGuess) == match.outcome()) {
            points += config.correctOutcome();
        }
        return points;
    }

    public int scoreTournamentWinner(Team winnerGuess, Team actualWinner) {
        if (winnerGuess == null || actualWinner == null) return 0;
        return winnerGuess.id.equals(actualWinner.id) ? config.tournamentWinner() : 0;
    }

    private MatchOutcome outcome(int home, int away) {
        if (home > away) return MatchOutcome.HOME_WIN;
        if (away > home) return MatchOutcome.AWAY_WIN;
        return MatchOutcome.DRAW;
    }
}
