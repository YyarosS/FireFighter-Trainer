package ru.mirea.firefighter.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mirea.firefighter.domain.model.Evaluation;
import ru.mirea.firefighter.domain.model.TrainingSession;
import ru.mirea.firefighter.domain.model.vo.EnvironmentProfile;
import ru.mirea.firefighter.domain.model.vo.EventType;
import ru.mirea.firefighter.domain.model.vo.Rating;
import ru.mirea.firefighter.domain.model.vo.RatingWeights;
import ru.mirea.firefighter.domain.model.vo.ScenarioId;
import ru.mirea.firefighter.domain.model.vo.SessionStatus;
import ru.mirea.firefighter.domain.model.vo.TimeLimit;
import ru.mirea.firefighter.domain.model.vo.TraineeId;
import ru.mirea.firefighter.domain.model.vo.TrainingSessionId;

/**
 * Проверки инвариантов агрегата «Тренировка».
 *
 * <p>Именно эти правила в процедурной реализации приходилось держать
 * «в голове» вызывающего кода — здесь они закреплены тестами модели.</p>
 */
@DisplayName("Агрегат «Тренировка»: инварианты")
class TrainingSessionTest {

    private static final Instant START = Instant.parse("2026-10-02T10:00:00Z");
    private static final EnvironmentProfile ENVIRONMENT = new EnvironmentProfile(60.0, 5.0, 120.0);
    private static final TimeLimit LIMIT = new TimeLimit(180.0);

    private TrainingSession session;

    @BeforeEach
    void setUp() {
        session = TrainingSession.start(
                new TrainingSessionId("session-1"),
                new TraineeId("trainee-1"),
                new ScenarioId("scenario-1"),
                START);
        session.issueStartCommand(START, ENVIRONMENT, LIMIT);
    }

    @Test
    @DisplayName("После завершения события не принимаются")
    void eventsAreRejectedAfterFinish() {
        session.recordEvent(EventType.ACTION, "extinguisher", 12.0, 0.0);
        session.finish(START.plusSeconds(30));

        assertThatThrownBy(() -> session.recordEvent(EventType.ACTION, "door", 31.0, 0.0))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("уже завершена");
    }

    @Test
    @DisplayName("Завершить тренировку можно только один раз")
    void finishIsGuarded() {
        session.finish(START.plusSeconds(30));

        assertThatThrownBy(() -> session.finish(START.plusSeconds(40)))
                .isInstanceOf(DomainException.class);
        assertThat(session.status()).isEqualTo(SessionStatus.FINISHED);
    }

    @Test
    @DisplayName("Время событий не может уменьшаться")
    void eventTimeMustBeMonotonic() {
        session.recordEvent(EventType.ACTION, "extinguisher", 20.0, 0.0);

        assertThatThrownBy(() -> session.recordEvent(EventType.ACTION, "door", 15.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("не может быть меньше");
    }

    @Test
    @DisplayName("Команда запуска выдаётся ровно один раз")
    void startCommandIsIssuedOnce() {
        assertThatThrownBy(() -> session.issueStartCommand(START, ENVIRONMENT, LIMIT))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("уже выдана");
    }

    @Test
    @DisplayName("Клиент догоняет команды по номеру последней полученной")
    void commandsCanBePolledIncrementally() {
        long afterStart = session.lastCommandSequence();
        session.issueEnvironmentCommand(START.plusSeconds(5), new EnvironmentProfile(80.0, 2.0, 300.0));
        session.issueHintCommand(START.plusSeconds(6), "Проверьте огнетушитель");

        assertThat(session.commandsAfter(afterStart)).hasSize(2);
        assertThat(session.commandsAfter(session.lastCommandSequence())).isEmpty();
    }

    @Test
    @DisplayName("Текущая среда определяется последней командой сервера")
    void environmentFollowsLastCommand() {
        EnvironmentProfile hotter = new EnvironmentProfile(90.0, 1.0, 500.0);
        session.issueEnvironmentCommand(START.plusSeconds(10), hotter);

        assertThat(session.currentEnvironment()).isEqualTo(hotter);
    }

    @Test
    @DisplayName("Итоговая команда невозможна до завершения тренировки")
    void endCommandRequiresFinishedSession() {
        Evaluation evaluation = new Evaluation(
                session.id(),
                Rating.clamp(80),
                Rating.clamp(90),
                Rating.clamp(86),
                new RatingWeights(0.4, 0.6),
                30.0,
                START);

        assertThatThrownBy(() -> session.issueEndCommand(START, evaluation))
                .isInstanceOf(DomainException.class);
    }
}