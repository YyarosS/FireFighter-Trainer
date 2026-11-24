package ru.mirea.firefighter.domain.model;

import java.util.Objects;
import ru.mirea.firefighter.domain.model.vo.TraineeId;

/**
 * Стажёр — учётная запись обучаемого и используемое VR-устройство
 * (сущность «Стажёр», п. 2.4 отчёта).
 */
public final class Trainee {

    private final TraineeId id;
    private String fullName;
    private String studyGroup;
    private String vrDeviceId;

    private Trainee(TraineeId id, String fullName, String studyGroup, String vrDeviceId) {
        this.id = Objects.requireNonNull(id, "id");
        this.fullName = requireText(fullName, "ФИО стажёра");
        this.studyGroup = requireText(studyGroup, "Учебная группа");
        this.vrDeviceId = requireText(vrDeviceId, "Идентификатор VR-устройства");
    }

    /** Создание нового стажёра. */
    public static Trainee register(TraineeId id, String fullName, String studyGroup, String vrDeviceId) {
        return new Trainee(id, fullName, studyGroup, vrDeviceId);
    }

    /** Смена VR-устройства: стажёр может выполнить тренировку на другом шлеме. */
    public void changeDevice(String newDeviceId) {
        this.vrDeviceId = requireText(newDeviceId, "Идентификатор VR-устройства");
    }

    public void rename(String newFullName) {
        this.fullName = requireText(newFullName, "ФИО стажёра");
    }

    public TraineeId id() {
        return id;
    }

    public String fullName() {
        return fullName;
    }

    public String studyGroup() {
        return studyGroup;
    }

    public String vrDeviceId() {
        return vrDeviceId;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " не может быть пустым");
        }
        return value.strip();
    }
}