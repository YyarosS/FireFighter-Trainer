package ru.mirea.firefighter.application.usecase;

import java.util.Objects;
import ru.mirea.firefighter.application.dto.TraineeView;
import ru.mirea.firefighter.application.port.IdGenerator;
import ru.mirea.firefighter.domain.model.Trainee;
import ru.mirea.firefighter.domain.model.vo.TraineeId;
import ru.mirea.firefighter.domain.port.TraineeRepository;

/** Сценарий использования «Зарегистрировать стажёра». */
public final class RegisterTraineeService {

    private final TraineeRepository trainees;
    private final IdGenerator ids;

    public RegisterTraineeService(TraineeRepository trainees, IdGenerator ids) {
        this.trainees = Objects.requireNonNull(trainees, "trainees");
        this.ids = Objects.requireNonNull(ids, "ids");
    }

    public TraineeView register(String fullName, String studyGroup, String vrDeviceId) {
        Trainee trainee = Trainee.register(new TraineeId(ids.nextId()), fullName, studyGroup, vrDeviceId);
        trainees.save(trainee);
        return TraineeView.from(trainee);
    }
}