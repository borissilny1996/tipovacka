package sk.tipovacka.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import sk.tipovacka.domain.*;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class ScoringServiceTest {

    @Inject
    ScoringService scoringService;

    @Test
    void exactScoreReturns2Points() {
        Match match = matchWithResult(3, 1);
        MatchGuess guess = guessWithScore(3, 1);
        assertThat(scoringService.scoreMatchGuess(guess, match)).isEqualTo(2);
    }

    @Test
    void correctOutcomeReturns1Point() {
        Match match = matchWithResult(3, 1);
        MatchGuess guess = guessWithScore(2, 0);
        assertThat(scoringService.scoreMatchGuess(guess, match)).isEqualTo(1);
    }

    @Test
    void wrongGuessReturns0Points() {
        Match match = matchWithResult(3, 1);
        MatchGuess guess = guessWithScore(0, 2);
        assertThat(scoringService.scoreMatchGuess(guess, match)).isEqualTo(0);
    }

    @Test
    void drawExactScoreReturns2Points() {
        Match match = matchWithResult(2, 2);
        MatchGuess guess = guessWithScore(2, 2);
        assertThat(scoringService.scoreMatchGuess(guess, match)).isEqualTo(2);
    }

    @Test
    void drawCorrectOutcomeReturns1Point() {
        Match match = matchWithResult(2, 2);
        MatchGuess guess = guessWithScore(1, 1);
        assertThat(scoringService.scoreMatchGuess(guess, match)).isEqualTo(1);
    }

    @Test
    void noResultReturns0Points() {
        Match match = new Match();
        MatchGuess guess = guessWithScore(1, 0);
        assertThat(scoringService.scoreMatchGuess(guess, match)).isEqualTo(0);
    }

    @Test
    void correctTournamentWinnerReturns2Points() {
        Team team = new Team();
        team.id = 1L;
        assertThat(scoringService.scoreTournamentWinner(team, team)).isEqualTo(2);
    }

    @Test
    void wrongTournamentWinnerReturns0Points() {
        Team teamA = new Team();
        teamA.id = 1L;
        Team teamB = new Team();
        teamB.id = 2L;
        assertThat(scoringService.scoreTournamentWinner(teamA, teamB)).isEqualTo(0);
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private Match matchWithResult(int home, int away) {
        Match m = new Match();
        m.homeScore = home;
        m.awayScore = away;
        return m;
    }

    private MatchGuess guessWithScore(int home, int away) {
        MatchGuess g = new MatchGuess();
        g.homeScoreGuess = home;
        g.awayScoreGuess = away;
        return g;
    }
}
