package ru.mirea.firefighter.domain.port;

import java.util.List;
import java.util.Optional;
import ru.mirea.firefighter.domain.model.Trainee;
import ru.mirea.firefighter.domain.model.vo.TraineeId;

/**
 * Порт хранения стажёров.
 *
 * <p>Интерфейс принадлежит домену, реализация — инфраструктуре.
 * На этапе 4 реализация in-memory, на этапе 5 — PostgreSQL через JPA;
 * домен при этом не меняется (принцип инверсии зависимостей).</p>
 */
public interface TraineeRepository {

    void save(Trainee trainee);

    Optional<Trainee> findById(TraineeId id);

    List<Trainee> findAll();

    boolean existsById(TraineeId id);
}