package ru.mirea.firefighter.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.offset;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mirea.firefighter.domain.model.LeaderboardEntry;
import ru.mirea.firefighter.domain.model.Scenario;
import ru.mirea.firefighter.domain.model.TrainingSession;
import ru.mirea.firefighter.domain.model.vo.EnvironmentProfile;
import ru.mirea.firefighter.domain.model.vo.EventType;
import ru.mirea.firefighter.domain.model.vo.Rating;
import ru.mirea.firefighter.domain.model.vo.RatingWeights;
import ru.mirea.firefighter.domain.model.vo.ScenarioId;
import ru.mirea.firefighter.domain.model.vo.TimeLimit;
import ru.mirea.firefighter.domain.model.vo.TraineeId;
import ru.mirea.firefighter.domain.model.vo.TrainingSessionId;
import ru.mirea.firefighter.domain.policy.AccuracyRatingPolicy;
import ru.mirea.firefighter.domain.policy.CombinedRatingPolicy;
import ru.mirea.firefighter.domain.policy.LeaderboardPolicy;
import ru.mirea.firefighter.domain.policy.SpeedRatingPolicy;

/**
 * Проверки политик рейтинга — те же шесть сценариев, что в процедурной
 * реализации ({@code FiremanTraining/java/src/SelfTest.java}).
 *
 * <p>Совпадение результатов двух реализаций — содержательное доказательство
 * того, что объектная модель не изменила поведение алгоритмов этапа 3.</p>
 */
@DisplayName("Алгоритмы 1–4: эквивалентность процедурной реализации")
class RatingPoliciesTest {

    private static final Instant START = Instant.parse("2026-10-02T10:00:00Z");
    private static final String SCENARIO_ID = "scenario-1";

    private final SpeedRatingPolicy speedPolicy = new SpeedRatingPolicy();
    private final AccuracyRatingPolicy accuracyPolicy = new AccuracyRatingPolicy(10.0);

    @Test
    @DisplayName("1. clamp ограничивает значение отрезком [0; 100]")
    void clampKeepsRatingInRange() {
        assertThat(Rating.clamp(-1.0).value()).isZero();
        assertThat(Rating.clamp(200.0).value()).isEqualTo(100.0);
        assertThat(Rating.clamp(42.0).value()).isEqualTo(42.0);
    }

    @Test
    @DisplayName("2. Скорость: Δt = 50 при T_max = 100 даёт V = 50")
    void speedRatingMatchesProceduralImplementation() {
        TrainingSession session = sessionWithActions(10.0, 50.0);

        assertThat(speedPolicy.evaluate(session, scenario(100.0)).value())
                .isCloseTo(50.0, offset(1e-9));
    }

    @Test
    @DisplayName("3. Точность: штрафы 2 + 1 + 3 при S_max = 10 дают A = 40")
    void accuracyRatingMatchesProceduralImplementation() {
        TrainingSession session = TrainingSession.start(
                new TrainingSessionId("session-3"), new TraineeId("trainee-1"),
                new ScenarioId(SCENARIO_ID), START);
        session.recordEvent(EventType.ACTION, "extinguisher", 1.0, EventType.ACTION.defaultWeight());
        session.recordEvent(EventType.SAFETY_VIOLATION, "door", 2.0, EventType.SAFETY_VIOLATION.defaultWeight());
        session.recordEvent(EventType.WRONG_EQUIPMENT, "hose", 3.0, EventType.WRONG_EQUIPMENT.defaultWeight());
        session.recordEvent(EventType.MISSED_STAGE, "victim", 4.0, EventType.MISSED_STAGE.defaultWeight());

        assertThat(accuracyPolicy.evaluate(session, scenario(100.0)).value())
                .isCloseTo(40.0, offset(1e-9));
    }

    @Test
    @DisplayName("4. Комбинированный рейтинг: веса 2/2 нормализуются в 0.5/0.5, R = 70")
    void combinedRatingMatchesProceduralImplementation() {
        CombinedRatingPolicy policy = new CombinedRatingPolicy(new RatingWeights(2.0, 2.0));

        assertThat(policy.combine(new Rating(80.0), new Rating(60.0)).value())
                .isCloseTo(70.0, offset(1e-9));
    }

    @Test
    @DisplayName("5. Таблица лидеров: при равном R выше тот, у кого меньше Δt")
    void leaderboardBreaksTiesByElapsedTime() {
        List<LeaderboardEntry> entries = List.of(
                entry("Первый", 50.0, 10.0, "2026-01-01"),
                entry("Лучший", 90.0, 20.0, "2026-01-02"),
                entry("Быстрый", 50.0, 8.0, "2026-01-03"));

        List<LeaderboardEntry> ranking = new LeaderboardPolicy().rank(entries, 2);

        assertThat(ranking).extracting(LeaderboardEntry::traineeName)
                .containsExactly("Лучший", "Быстрый");
    }

    @Test
    @DisplayName("6. Нулевой норматив времени запрещён")
    void zeroTimeLimitIsRejected() {
        assertThatThrownBy(() -> new TimeLimit(0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Норматив времени");
    }

    // ------------------------------------------------------------------ вспомогательные методы

    private static Scenario scenario(double timeLimitSeconds) {
        return Scenario.create(
                new ScenarioId(SCENARIO_ID),
                "Возгорание в серверной",
                "Тестовый сценарий",
                new EnvironmentProfile(60.0, 5.0, 120.0),
                new TimeLimit(timeLimitSeconds));
    }

    private static TrainingSession sessionWithActions(double... times) {
        TrainingSession session = TrainingSession.start(
                new TrainingSessionId("session-2"),
                new TraineeId("trainee-1"),
                new ScenarioId(SCENARIO_ID),
                START);
        for (double time : times) {
            session.recordEvent(EventType.ACTION, "sensor", time, 0.0);
        }
        return session;
    }

    private static LeaderboardEntry entry(String name, double combined, double elapsed, String date) {
        return new LeaderboardEntry(
                name,
                "Возгорание в серверной",
                Rating.clamp(combined),
                Rating.clamp(combined),
                Rating.clamp(combined),
                elapsed,
                Instant.parse(date + "T10:00:00Z"));
    }
}