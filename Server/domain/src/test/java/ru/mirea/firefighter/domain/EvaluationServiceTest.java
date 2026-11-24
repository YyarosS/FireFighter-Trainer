package ru.mirea.firefighter.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.offset;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mirea.firefighter.domain.model.Evaluation;
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
import ru.mirea.firefighter.domain.policy.EvaluationService;
import ru.mirea.firefighter.domain.policy.SpeedRatingPolicy;

/** Проверки доменного сервиса оценки: композиция политик и качественная шкала. */
@DisplayName("Сервис оценки тренировки")
class EvaluationServiceTest {

    private static final Instant START = Instant.parse("2026-10-02T10:00:00Z");

    private final EvaluationService service = new EvaluationService(
            new SpeedRatingPolicy(),
            new AccuracyRatingPolicy(10.0),
            new CombinedRatingPolicy(new RatingWeights(0.4, 0.6)));

    @Test
    @DisplayName("Незавершённую тренировку оценить нельзя")
    void unfinishedSessionIsRejected() {
        TrainingSession session = session();

        assertThatThrownBy(() -> service.evaluate(session, scenario()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("незавершённую");
    }

    @Test
    @DisplayName("Оценка объединяет скорость, точность и комбинированный рейтинг")
    void evaluationCombinesPolicies() {
        TrainingSession session = session();
        session.recordEvent(EventType.ACTION, "extinguisher", 50.0, 0.0);
        session.recordEvent(EventType.SAFETY_VIOLATION, "door", 60.0, 2.0);
        session.finish(START.plusSeconds(70));

        Evaluation evaluation = service.evaluate(session, scenario());

        // V = 100 * (1 - 60/100) = 40; A = 100 * (1 - 2/10) = 80; R = 0.4*40 + 0.6*80 = 64
        assertThat(evaluation.speed().value()).isCloseTo(40.0, offset(1e-9));
        assertThat(evaluation.accuracy().value()).isCloseTo(80.0, offset(1e-9));
        assertThat(evaluation.combined().value()).isCloseTo(64.0, offset(1e-9));
        assertThat(evaluation.grade()).isEqualTo(Evaluation.Grade.SATISFACTORY);
    }

    @Test
    @DisplayName("Качественная шкала соответствует порогам 60/75/90")
    void gradeBoundaries() {
        assertThat(gradeOf(95.0)).isEqualTo(Evaluation.Grade.EXCELLENT);
        assertThat(gradeOf(80.0)).isEqualTo(Evaluation.Grade.GOOD);
        assertThat(gradeOf(65.0)).isEqualTo(Evaluation.Grade.SATISFACTORY);
        assertThat(gradeOf(30.0)).isEqualTo(Evaluation.Grade.UNSATISFACTORY);
    }

    private static Evaluation.Grade gradeOf(double combined) {
        return new Evaluation(
                new TrainingSessionId("session-1"),
                Rating.clamp(combined),
                Rating.clamp(combined),
                Rating.clamp(combined),
                new RatingWeights(0.4, 0.6),
                10.0,
                START).grade();
    }

    private static TrainingSession session() {
        return TrainingSession.start(
                new TrainingSessionId("session-1"),
                new TraineeId("trainee-1"),
                new ScenarioId("scenario-1"),
                START);
    }

    private static Scenario scenario() {
        return Scenario.create(
                new ScenarioId("scenario-1"),
                "Возгорание в серверной",
                "",
                new EnvironmentProfile(60.0, 5.0, 120.0),
                new TimeLimit(100.0));
    }
}