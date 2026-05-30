package sk.tipovacka.ui;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.inject.Inject;
import sk.tipovacka.domain.Competition;
import sk.tipovacka.domain.CompetitionGuess;
import sk.tipovacka.domain.Match;
import sk.tipovacka.domain.MatchGuess;
import sk.tipovacka.service.CompetitionService;

import java.util.ArrayList;
import java.util.List;

@Route(value = "leaderboard", layout = MainLayout.class)
@PageTitle("Leaderboard | Tipovačka")
public class LeaderboardView extends VerticalLayout {

    private final CompetitionService competitionService;

    private final VerticalLayout contentContainer = new VerticalLayout();

    @Inject
    public LeaderboardView(CompetitionService competitionService) {
        this.competitionService = competitionService;
        setPadding(true);
        setSpacing(true);
        contentContainer.setPadding(false);
        contentContainer.setSpacing(true);
        buildUI();
    }

    private void buildUI() {
        List<Competition> allCompetitions = competitionService.listAll();

        if (allCompetitions.isEmpty()) {
            add(new Span("Žiadna súťaž."));
            return;
        }

        if (allCompetitions.size() > 1) {
            ComboBox<Competition> selector = new ComboBox<>("Vyber súťaž");
            selector.setItems(allCompetitions);
            selector.setItemLabelGenerator(c -> c.name + (c.active ? "" : " (ukončená)"));
            selector.setWidth("400px");
            selector.addValueChangeListener(e -> {
                contentContainer.removeAll();
                if (e.getValue() != null) {
                    buildLeaderboard(e.getValue());
                }
            });
            add(selector);
        }

        add(contentContainer);

        // Auto-select the most recent competition
        buildLeaderboard(allCompetitions.get(0));
    }

    private void buildLeaderboard(Competition competition) {
        contentContainer.removeAll();

        contentContainer.add(new H2("Tabuľka — " + competition.name));

        List<CompetitionGuess> ranked = CompetitionGuess.leaderboard(competition);

        if (ranked.isEmpty()) {
            contentContainer.add(new Span("Zatiaľ žiadne tipy."));
            return;
        }

        // ── Rankings table ──────────────────────────────────────────────────
        Grid<RankEntry> rankGrid = new Grid<>();
        rankGrid.addColumn(RankEntry::rank).setHeader("#").setWidth("60px").setFlexGrow(0);
        rankGrid.addColumn(e -> e.guess().participant.nickname).setHeader("Prezývka");
        rankGrid.addColumn(e -> e.guess().winnerGuess != null ? e.guess().winnerGuess.name : "—")
                .setHeader("Tip na víťaza");
        rankGrid.addColumn(e -> e.guess().totalPoints).setHeader("Počet bodov");
        rankGrid.setItems(toRankEntries(ranked));
        rankGrid.setAllRowsVisible(true);
        contentContainer.add(rankGrid);

        // ── Detailed guesses per match ───────────────────────────────────
        contentContainer.add(new H3("Výsledky a vaše tipy"));

        List<Match> matches = Match.findByCompetition(competition);
        for (Match match : matches) {
            contentContainer.add(buildMatchDetailRow(match, ranked));
        }
    }

    private VerticalLayout buildMatchDetailRow(Match match, List<CompetitionGuess> ranked) {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(false);
        section.setPadding(false);

        String result = match.hasResult()
                ? match.homeScore + " : " + match.awayScore
                : "TBD";
        section.add(new Span(match.homeTeam.name + " vs " + match.awayTeam.name + "  —  " + result));

        Grid<GuessRow> grid = new Grid<>();
        grid.addColumn(GuessRow::nickname).setHeader("Prezývka");
        grid.addColumn(GuessRow::guessLabel).setHeader("Tip");
        grid.addColumn(GuessRow::points).setHeader("Body");
        grid.setAllRowsVisible(true);

        List<GuessRow> rows = ranked.stream()
                .map(cg -> {
                    MatchGuess mg = findMatchGuess(cg, match);
                    String guessLabel = mg != null && mg.homeScoreGuess != null
                            ? mg.homeScoreGuess + " : " + mg.awayScoreGuess
                            : "—";
                    String pts = mg != null && mg.points != null ? mg.points + "b" : "—";
                    return new GuessRow(cg.participant.nickname, guessLabel, pts);
                })
                .toList();

        grid.setItems(rows);
        section.add(grid);
        return section;
    }

    private MatchGuess findMatchGuess(CompetitionGuess cg, Match match) {
        return cg.matchGuesses.stream()
                .filter(mg -> mg.match.id.equals(match.id))
                .findFirst()
                .orElse(null);
    }

    private List<RankEntry> toRankEntries(List<CompetitionGuess> sorted) {
        List<RankEntry> result = new ArrayList<>();
        int currentRank = 1;
        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0 && sorted.get(i).totalPoints < sorted.get(i - 1).totalPoints) {
                currentRank = i + 1;
            }
            result.add(new RankEntry(currentRank, sorted.get(i)));
        }
        return result;
    }

    private record RankEntry(int rank, CompetitionGuess guess) {}

    private record GuessRow(String nickname, String guessLabel, String points) {}
}
