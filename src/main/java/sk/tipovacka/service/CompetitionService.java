package sk.tipovacka.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import sk.tipovacka.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CompetitionService {

    @Inject
    GuessService guessService;

    public Optional<Competition> findActive() {
        return Competition.findActive();
    }

    public List<Competition> listActive() {
        return Competition.listActive();
    }

    public List<Competition> listAll() {
        return Competition.listAllOrdered();
    }

    @Transactional
    public Competition create(String name, String sport, int year, LocalDateTime deadline) {
        Competition c = new Competition();
        c.name = name;
        c.sport = sport;
        c.year = year;
        c.submissionDeadline = deadline;
        c.persist();
        return c;
    }

    @Transactional
    public void update(Competition competition, String name, String sport, int year, LocalDateTime deadline) {
        competition.name = name;
        competition.sport = sport;
        competition.year = year;
        competition.submissionDeadline = deadline;
    }

    @Transactional
    public Team addTeam(Competition competition, String teamName) {
        Team t = new Team();
        t.name = teamName;
        t.competition = competition;
        t.persist();
        return t;
    }

    @Transactional
    public void removeTeam(Long teamId) {
        Team.deleteById(teamId);
    }

    @Transactional
    public Match addMatch(Competition competition, Team homeTeam, Team awayTeam) {
        Match m = new Match();
        m.competition = competition;
        m.homeTeam = homeTeam;
        m.awayTeam = awayTeam;
        m.persist();
        return m;
    }

    @Transactional
    public void setMatchResult(Long matchId, int homeScore, int awayScore) {
        Match match = Match.findById(matchId);
        match.homeScore = homeScore;
        match.awayScore = awayScore;
        guessService.recalculatePoints(match.competition);
    }

    @Transactional
    public void setActualWinner(Long competitionId, Long winnerTeamId) {
        Competition competition = Competition.findById(competitionId);
        competition.actualWinner = Team.findById(winnerTeamId);
        guessService.recalculatePoints(competition);
    }
}
