package ru.mirea.firefighter.domain;

/**
 * Нарушение бизнес-правила предметной области (инварианта агрегата).
 *
 * <p>Отличается от {@link IllegalArgumentException}: последнее означает
 * некорректные входные данные, а это исключение — попытку перевести
 * корректный объект в недопустимое состояние (например, завершить уже
 * завершённую тренировку). На уровне REST такие ошибки транслируются
 * в 409 Conflict.</p>
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}