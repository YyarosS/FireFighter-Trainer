package ru.mirea.firefighter.web.api;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.mirea.firefighter.application.dto.SessionSummary;
import ru.mirea.firefighter.application.dto.TraineeView;
import ru.mirea.firefighter.application.usecase.GetTraineeHistoryService;
import ru.mirea.firefighter.application.usecase.RegisterTraineeService;
import ru.mirea.firefighter.web.api.dto.RegisterTraineeRequest;

/** REST-контроллер стажёров: регистрация и история тренировок. */
@RestController
@RequestMapping("/api/trainees")
public class TraineeController {

    private final RegisterTraineeService registerTrainee;
    private final GetTraineeHistoryService history;

    public TraineeController(RegisterTraineeService registerTrainee, GetTraineeHistoryService history) {
        this.registerTrainee = registerTrainee;
        this.history = history;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TraineeView register(@Valid @RequestBody RegisterTraineeRequest request) {
        return registerTrainee.register(request.fullName(), request.studyGroup(), request.vrDeviceId());
    }

    @GetMapping("/{traineeId}/sessions")
    public List<SessionSummary> sessions(@PathVariable String traineeId) {
        return history.history(traineeId);
    }
}