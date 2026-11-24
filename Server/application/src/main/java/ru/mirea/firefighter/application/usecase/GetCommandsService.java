package ru.mirea.firefighter.application.usecase;

import java.util.List;
import java.util.Objects;
import ru.mirea.firefighter.application.dto.CommandView;
import ru.mirea.firefighter.domain.port.TrainingSessionRepository;

/**
 * Сценарий использования «Выдать команды сервера, начиная с указанной».
 *
 * <p>Догоняющая синхронизация: клиент хранит номер последней полученной
 * команды и при следующем опросе получает только новые. При обрыве связи
 * команды не теряются.</p>
 */
public final class GetCommandsService {

    private final TrainingSessionRepository sessions;

    public GetCommandsService(TrainingSessionRepository sessions) {
        this.sessions = Objects.requireNonNull(sessions, "sessions");
    }

    public List<CommandView> commands(String sessionId, long afterSequence) {
        if (afterSequence < 0) {
            throw new IllegalArgumentException("Номер последней команды не может быть отрицательным");
        }
        return EntityLookup.session(sessions, sessionId).commandsAfter(afterSequence).stream()
                .map(CommandView::from)
                .toList();
    }
}