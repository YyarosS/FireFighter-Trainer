package ru.mirea.firefighter.web.api;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.mirea.firefighter.application.dto.CommandView;
import ru.mirea.firefighter.application.dto.SessionResult;
import ru.mirea.firefighter.application.dto.SessionStateView;
import ru.mirea.firefighter.application.usecase.FinishTrainingService;
import ru.mirea.firefighter.application.usecase.GetCommandsService;
import ru.mirea.firefighter.application.usecase.GetSessionStateService;
import ru.mirea.firefighter.application.usecase.RecordEventService;
import ru.mirea.firefighter.application.usecase.StartTrainingService;
import ru.mirea.firefighter.application.usecase.UpdateEnvironmentService;
import ru.mirea.firefighter.web.api.dto.RecordEventsRequest;
import ru.mirea.firefighter.web.api.dto.StartSessionRequest;
import ru.mirea.firefighter.web.api.dto.UpdateEnvironmentRequest;

/**
 * REST-контроллер сеанса тренировки — основной канал взаимодействия
 * с VR-клиентом.
 *
 * <p>Клиент отправляет факты (события) и получает решения сервера
 * (состояние и команды). Ни одна операция не позволяет клиенту
 * назначить рейтинг напрямую.</p>
 */
@RestController
@RequestMapping("/api/sessions")
public class TrainingSessionController {

    private final StartTrainingService startTraining;
    private final RecordEventService recordEvent;
    private final FinishTrainingService finishTraining;
    private final GetSessionStateService sessionState;
    private final GetCommandsService commands;
    private final UpdateEnvironmentService updateEnvironment;

    public TrainingSessionController(StartTrainingService startTraining,
                                     RecordEventService recordEvent,
                                     FinishTrainingService finishTraining,
                                     GetSessionStateService sessionState,
                                     GetCommandsService commands,
                                     UpdateEnvironmentService updateEnvironment) {
        this.startTraining = startTraining;
        this.recordEvent = recordEvent;
        this.finishTraining = finishTraining;
        this.sessionState = sessionState;
        this.commands = commands;
        this.updateEnvironment = updateEnvironment;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionStateView start(@Valid @RequestBody StartSessionRequest request) {
        return startTraining.start(request.traineeId(), request.scenarioId());
    }

    @PostMapping("/{sessionId}/events")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recordEvents(@PathVariable String sessionId, @Valid @RequestBody RecordEventsRequest request) {
        recordEvent.record(sessionId, request.toInputs());
    }

    @PostMapping("/{sessionId}/finish")
    public SessionResult finish(@PathVariable String sessionId) {
        return finishTraining.finish(sessionId);
    }

    @GetMapping("/{sessionId}")
    public SessionStateView state(@PathVariable String sessionId) {
        return sessionState.state(sessionId);
    }

    @GetMapping("/{sessionId}/commands")
    public List<CommandView> commands(@PathVariable String sessionId,
                                      @RequestParam(defaultValue = "0") long after) {
        return commands.commands(sessionId, after);
    }

    @PostMapping("/{sessionId}/environment")
    public SessionStateView updateEnvironment(@PathVariable String sessionId,
                                              @RequestBody UpdateEnvironmentRequest request) {
        return updateEnvironment.update(sessionId, request.toView());
    }
}