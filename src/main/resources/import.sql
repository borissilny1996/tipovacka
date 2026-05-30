-- Dev seed data
-- IDs start at 1000 to avoid clashing with Hibernate's sequence pre-allocation.

INSERT INTO competition (id, name, sport, competition_year, submission_deadline, active, actual_winner_id)
VALUES (1000, 'IIHF World Championship 2026', 'Ice Hockey', 2026, '2027-05-28 09:00:00', true, null);

INSERT INTO team (id, name, competition_id) VALUES
(1001, 'Slovensko',      1000),
(1002, 'Nórsko',     1000),
(1003, 'Taliansko',      1000),
(1004, 'Slovinsko',      1000),
(1005, 'Dánsko',       1000),
(1006, 'Česko',       1000),
(1007, 'Kanada',          1000),
(1008, 'Švédsko',      1000),
(1009, 'Fínsko',  1000),
(1010, 'Švajčiarsko',  1000);

INSERT INTO matches (id, competition_id, home_team_id, away_team_id, home_score, away_score)
VALUES
(1010, 1000, 1001, 1002, null, null),
(1011, 1000, 1001, 1003, null, null),
(1012, 1000, 1001, 1004, null, null),
(1013, 1000, 1001, 1005, null, null),
(1014, 1000, 1001, 1006, null, null),
(1015, 1000, 1001, 1007, null, null),
(1016, 1000, 1001, 1008, null, null);
