package ru.mirea.firefighter.application.exception;

/** Запрошенная сущность не найдена. На уровне REST транслируется в 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String entity, Object id) {
        return new NotFoundException("%s с идентификатором %s не найден(а)".formatted(entity, id));
    }
}