package sk.tipovacka.ui.admin;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import sk.tipovacka.domain.Competition;
import sk.tipovacka.domain.Match;
import sk.tipovacka.domain.Team;
import sk.tipovacka.service.CompetitionService;
import sk.tipovacka.ui.MainLayout;

import java.time.LocalDateTime;
import java.util.List;

import static sk.tipovacka.util.IntegerFieldUtils.getIntegerFieldValueOrDefault;
import static sk.tipovacka.util.NotificationUtil.showError;
import static sk.tipovacka.util.NotificationUtil.showSuccess;

@Route(value = "admin", layout = MainLayout.class)
@PageTitle("Admin | Tipovačka")
public class AdminView extends VerticalLayout {

    private static final String SESSION_KEY = "tipovacka.admin.authenticated";

    private final CompetitionService competitionService;
    private final String adminPassword;

    private Grid<Competition> competitionGrid;
    private VerticalLayout detailSection;

    @Inject
    public AdminView(CompetitionService competitionService,
                     @ConfigProperty(name = "tipovacka.admin.password") String adminPassword) {
        this.competitionService = competitionService;
        this.adminPassword = adminPassword;
        setPadding(true);
        setSpacing(true);
        buildUI();
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    private void buildUI() {
        removeAll();
        if (isAuthenticated()) {
            buildAdminContent();
        } else {
            buildLoginForm();
        }
    }

    // ── Session helpers ───────────────────────────────────────────────────────

    private boolean isAuthenticated() {
        return Boolean.TRUE.equals(VaadinSession.getCurrent().getAttribute(SESSION_KEY));
    }

    private void login(String password) {
        if (adminPassword.equals(password)) {
            VaadinSession.getCurrent().setAttribute(SESSION_KEY, true);
            buildUI();
        } else {
            showError("Nesprávne heslo.");
        }
    }

    private void logout() {
        VaadinSession.getCurrent().setAttribute(SESSION_KEY, false);
        buildUI();
    }

    // ── Login form ────────────────────────────────────────────────────────────

    private void buildLoginForm() {
        add(new H2("Admin"));

        PasswordField passwordField = new PasswordField("Heslo");
        passwordField.setWidth("300px");
        passwordField.setPlaceholder("Zadaj heslo");

        Button enter = new Button("Vstúpiť", e -> login(passwordField.getValue()));
        enter.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Allow submitting with Enter key
        passwordField.addKeyPressListener(Key.ENTER, e -> login(passwordField.getValue()));

        add(passwordField, enter);
    }

    // ── Admin content (shown only after successful login) ─────────────────────

    private void buildAdminContent() {
        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(Alignment.BASELINE);
        header.setWidthFull();
        H2 title = new H2("Admin");
        Button logoutBtn = new Button("Odhlásiť sa", e -> logout());
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        header.add(title, logoutBtn);
        add(header);

        // ── Competition management ────────────────────────────────────────────
        add(new H3("Competitions"));

        Button newCompetition = new Button("+ New Competition", e -> openNewCompetitionDialog());
        newCompetition.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add(newCompetition);

        competitionGrid = new Grid<>();
        competitionGrid.addColumn(c -> c.name).setHeader("Name");
        competitionGrid.addColumn(c -> c.sport + " " + c.year).setHeader("Sport / Year");
        competitionGrid.addColumn(c -> c.submissionDeadline.toString()).setHeader("Deadline");
        competitionGrid.addColumn(c -> c.active ? "Active" : "Inactive").setHeader("Status");
        competitionGrid.setAllRowsVisible(true);
        competitionGrid.addItemClickListener(e -> loadCompetitionDetail(e.getItem()));
        refreshCompetitionGrid();
        add(competitionGrid);

        // ── Detail section (populated on competition select) ──────────────────
        detailSection = new VerticalLayout();
        detailSection.setPadding(false);
        add(detailSection);
    }

    private void refreshCompetitionGrid() {
        competitionGrid.setItems(competitionService.listAll());
    }

    private void loadCompetitionDetail(Competition competition) {
        detailSection.removeAll();
        detailSection.add(new Hr());
        detailSection.add(new H3("Managing: " + competition.name));

        // ── Teams ─────────────────────────────────────────────────────────────
        detailSection.add(new H3("Teams"));

        List<Team> teams = Team.findByCompetition(competition);
        Grid<Team> teamGrid = new Grid<>();
        teamGrid.addColumn(t -> t.name).setHeader("Team name");
        teamGrid.addComponentColumn(t -> {
            Button del = new Button("Remove", e -> {
                competitionService.removeTeam(t.id);
                loadCompetitionDetail(competition);
            });
            del.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            return del;
        }).setHeader("");
        teamGrid.setItems(teams);
        teamGrid.setAllRowsVisible(true);
        detailSection.add(teamGrid);

        HorizontalLayout addTeamRow = new HorizontalLayout();
        addTeamRow.setAlignItems(Alignment.BASELINE);
        TextField teamNameField = new TextField("Team name");
        Button addTeam = new Button("Add Team", e -> {
            String name = teamNameField.getValue().trim();
            if (!name.isEmpty()) {
                competitionService.addTeam(competition, name);
                teamNameField.clear();
                loadCompetitionDetail(competition);
            }
        });
        addTeamRow.add(teamNameField, addTeam);
        detailSection.add(addTeamRow);

        // ── Matches ───────────────────────────────────────────────────────────
        detailSection.add(new H3("Matches"));

        List<Match> matches = Match.findByCompetition(competition);
        Grid<Match> matchGrid = new Grid<>();
        matchGrid.addColumn(m -> m.homeTeam.name + " vs " + m.awayTeam.name).setHeader("Match");
        matchGrid.addColumn(m -> m.hasResult() ? m.homeScore + " : " + m.awayScore : "—").setHeader("Result");
        matchGrid.addComponentColumn(m -> buildResultEditor(m, competition)).setHeader("Set result");
        matchGrid.setItems(matches);
        matchGrid.setAllRowsVisible(true);
        detailSection.add(matchGrid);

        Button addMatchBtn = new Button("+ Add Match", e -> openAddMatchDialog(competition));
        detailSection.add(addMatchBtn);

        // ── Tournament winner ─────────────────────────────────────────────────
        detailSection.add(new H3("Actual Tournament Winner"));
        List<Team> allTeams = Team.findByCompetition(competition);
        ComboBox<Team> winnerCombo = new ComboBox<>("Set actual winner");
        winnerCombo.setItems(allTeams);
        winnerCombo.setItemLabelGenerator(t -> t.name);
        if (competition.actualWinner != null) {
            winnerCombo.setValue(competition.actualWinner);
        }
        Button saveWinner = new Button("Save Winner", e -> {
            if (winnerCombo.getValue() != null) {
                competitionService.setActualWinner(competition.id, winnerCombo.getValue().id);
                showSuccess("Winner saved and points recalculated.");
            }
        });
        detailSection.add(new HorizontalLayout(winnerCombo, saveWinner));
    }

    private HorizontalLayout buildResultEditor(Match match, Competition competition) {
        IntegerField home = new IntegerField();
        home.setMin(0);
        home.setMax(99);
        home.setWidth("65px");
        home.setPlaceholder("H");
        if (match.homeScore != null) home.setValue(match.homeScore);

        IntegerField away = new IntegerField();
        away.setMin(0);
        away.setMax(99);
        away.setWidth("65px");
        away.setPlaceholder("A");
        if (match.awayScore != null) away.setValue(match.awayScore);

        Button save = new Button("Save", e -> {
            competitionService.setMatchResult(match.id, getIntegerFieldValueOrDefault(home), getIntegerFieldValueOrDefault(away));
            showSuccess("Result saved and points recalculated.");
            loadCompetitionDetail(competition);
        });
        save.addThemeVariants(ButtonVariant.LUMO_SMALL);

        return new HorizontalLayout(home, new com.vaadin.flow.component.html.Span(":"), away, save);
    }

    private void openNewCompetitionDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("New Competition");

        TextField name = new TextField("Name");
        name.setWidthFull();
        TextField sport = new TextField("Sport");
        IntegerField year = new IntegerField("Year");
        year.setValue(LocalDateTime.now().getYear());
        DateTimePicker deadline = new DateTimePicker("Submission Deadline");

        VerticalLayout form = new VerticalLayout(name, sport, year, deadline);
        form.setPadding(false);
        dialog.add(form);

        Button save = new Button("Create", e -> {
            if (!name.getValue().isBlank() && deadline.getValue() != null) {
                competitionService.create(
                        name.getValue().trim(),
                        sport.getValue().trim(),
                        year.getValue(),
                        deadline.getValue());
                refreshCompetitionGrid();
                dialog.close();
                showSuccess("Competition created.");
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private void openAddMatchDialog(Competition competition) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add Match");

        List<Team> teams = Team.findByCompetition(competition);

        ComboBox<Team> home = new ComboBox<>("Home team");
        home.setItems(teams);
        home.setItemLabelGenerator(t -> t.name);

        ComboBox<Team> away = new ComboBox<>("Away team");
        away.setItems(teams);
        away.setItemLabelGenerator(t -> t.name);

        VerticalLayout form = new VerticalLayout(home, away);
        form.setPadding(false);
        dialog.add(form);

        Button save = new Button("Add", e -> {
            if (home.getValue() != null && away.getValue() != null) {
                competitionService.addMatch(competition, home.getValue(), away.getValue());
                loadCompetitionDetail(competition);
                dialog.close();
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }
}
