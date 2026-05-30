package sk.tipovacka.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "tipovacka.scoring")
public interface ScoringConfig {

    @WithDefault("2")
    int exactScore();

    @WithDefault("1")
    int correctOutcome();

    @WithDefault("2")
    int tournamentWinner();
}
