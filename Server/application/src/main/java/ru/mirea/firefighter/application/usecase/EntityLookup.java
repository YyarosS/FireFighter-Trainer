package ru.mirea.firefighter.application.usecase;

import ru.mirea.firefighter.application.exception.NotFoundException;
import ru.mirea.firefighter.domain.model.Scenario;
import ru.mirea.firefighter.domain.model.Trainee;
import ru.mirea.firefighter.domain.model.TrainingSession;
import ru.mirea.firefighter.domain.model.vo.ScenarioId;
import ru.mirea.firefighter.domain.model.vo.TraineeId;
import ru.mirea.firefighter.domain.model.vo.TrainingSessionId;
import ru.mirea.firefighter.domain.port.ScenarioRepository;
import ru.mirea.firefighter.domain.port.TraineeRepository;
import ru.mirea.firefighter.domain.port.TrainingSessionRepository;

/**
 * Поиск сущностей с преобразованием «нет в хранилище» в прикладное
 * исключение. Вынесено в одно место, чтобы каждый сценарий использования
 * не повторял одну и ту же конструкцию {@code orElseThrow}.
 */
final class EntityLookup {

    private EntityLookup() {
    }

    static Trainee trainee(TraineeRepository repository, TraineeId id) {
        return repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Стажёр", id));
    }

    static Scenario scenario(ScenarioRepository repository, ScenarioId id) {
        return repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Сценарий", id));
    }

    static TrainingSession session(TrainingSessionRepository repository, TrainingSessionId id) {
        return repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Тренировка", id));
    }

    static TrainingSession session(TrainingSessionRepository repository, String id) {
        return session(repository, new TrainingSessionId(id));
    }
}