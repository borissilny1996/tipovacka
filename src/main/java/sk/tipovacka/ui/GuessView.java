package sk.tipovacka.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.inject.Inject;
import sk.tipovacka.domain.Competition;
import sk.tipovacka.domain.Match;
import sk.tipovacka.domain.Team;
import sk.tipovacka.service.CompetitionService;
import sk.tipovacka.service.GuessService;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static sk.tipovacka.util.IntegerFieldUtils.getIntegerFieldValueOrDefault;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Submit Guesses | Tipovačka")
public class GuessView extends VerticalLayout {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final GuessService guessService;
    private final CompetitionService competitionService;

    private TextField nicknameField;
    private ComboBox<Team> winnerCombo;
    private final Map<Long, IntegerField[]> scoreFields = new HashMap<>();

    // Container rebuilt every time the selected competition changes
    private final VerticalLayout formContainer = new VerticalLayout();

    @Inject
    public GuessView(GuessService guessService, CompetitionService competitionService) {
        this.guessService = guessService;
        this.competitionService = competitionService;
        setPadding(true);
        setSpacing(true);
        formContainer.setPadding(false);
        formContainer.setSpacing(true);
        buildUI();
    }

    private void buildUI() {
        List<Competition> activeCompetitions = competitionService.listActive();

        if (activeCompetitions.isEmpty()) {
            add(new Paragraph("Aktuálne nie je k dispozícii žiadny turnaj. Vráť sa neskôr."));
            return;
        }

        if (activeCompetitions.size() > 1) {
            ComboBox<Competition> selector = new ComboBox<>("Vyber súťaž");
            selector.setItems(activeCompetitions);
            selector.setItemLabelGenerator(c -> c.name);
            selector.setWidth("400px");
            selector.addValueChangeListener(e -> {
                formContainer.removeAll();
                scoreFields.clear();
                if (e.getValue() != null) {
                    buildCompetitionForm(e.getValue());
                }
            });
            add(selector);
        }

        add(formContainer);

        // Auto-select: always pre-load the first (or only) competition
        buildCompetitionForm(activeCompetitions.get(0));
    }

    private void buildCompetitionForm(Competition competition) {
        formContainer.removeAll();
        scoreFields.clear();

        boolean deadlinePassed = competition.isDeadlinePassed();

        formContainer.add(new H2(competition.name));

        if (deadlinePassed) {
            Span banner = new Span("Už nie je možné tipovať, deadline: (" +
                    competition.submissionDeadline.format(DATE_FMT) + ")");
            banner.getStyle().set("color", "var(--lumo-error-color)").set("font-weight", "bold");
            formContainer.add(banner);
        } else {
            formContainer.add(new Span("Deadline: " + competition.submissionDeadline.format(DATE_FMT)));
        }

        nicknameField = new TextField("Tvoje meno");
        nicknameField.setRequired(true);
        nicknameField.setPlaceholder("e.g. boris");
        nicknameField.setEnabled(!deadlinePassed);
        nicknameField.setWidth("300px");
        formContainer.add(nicknameField);

        List<Team> teams = Team.findByCompetition(competition);
        winnerCombo = new ComboBox<>("Celkový víťaz");
        winnerCombo.setItems(teams);
        winnerCombo.setItemLabelGenerator(t -> t.name);
        winnerCombo.setEnabled(!deadlinePassed);
        winnerCombo.setWidth("300px");
        formContainer.add(winnerCombo);

        formContainer.add(new H3("Tipy na výsledky"));

        List<Match> matches = Match.findByCompetition(competition);
        for (Match match : matches) {
            formContainer.add(buildMatchRow(match, deadlinePassed));
        }

        if (!deadlinePassed) {
            Button submit = new Button("Uložiť tipy", e -> submitGuesses(competition));
            submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            submit.getStyle().set("margin-top", "1em");
            formContainer.add(submit);
        }
    }

    private HorizontalLayout buildMatchRow(Match match, boolean disabled) {
        HorizontalLayout row = new HorizontalLayout();
        row.setAlignItems(Alignment.BASELINE);
        row.setSpacing(true);

        Span label = new Span(match.homeTeam.name + " vs " + match.awayTeam.name);
        label.setWidth("220px");

        IntegerField home = new IntegerField();
        home.setMin(0);
        home.setEnabled(!disabled);
        home.setPlaceholder("0");
        home.setWidth("70px");

        Span dash = new Span(":");

        IntegerField away = new IntegerField();
        away.setMin(0);
        away.setEnabled(!disabled);
        away.setPlaceholder("0");
        away.setWidth("70px");

        scoreFields.put(match.id, new IntegerField[]{home, away});
        row.add(label, home, dash, away);
        return row;
    }

    private void submitGuesses(Competition competition) {
        String nickname = nicknameField.getValue() == null ? "" : nicknameField.getValue().trim();
        if (nickname.isEmpty()) {
            showError("Prosím, zvoľ si meno.");
            return;
        }

        Map<Long, int[]> scores = new HashMap<>();
        for (Map.Entry<Long, IntegerField[]> entry : scoreFields.entrySet()) {
            IntegerField homeField = entry.getValue()[0];
            IntegerField awayField = entry.getValue()[1];
            scores.put(entry.getKey(), new int[]{
                    getIntegerFieldValueOrDefault(homeField),
                    getIntegerFieldValueOrDefault(awayField)
            });
        }

        try {
            guessService.submit(nickname, competition, winnerCombo.getValue(), scores);
            Notification n = Notification.show("Tvoje tipy boli úspešne pridané!");
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            n.setDuration(4000);
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String message) {
        Notification n = Notification.show(message);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        n.setDuration(5000);
    }
}
