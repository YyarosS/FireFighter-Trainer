package ru.mirea.firefighter.infrastructure.id;

import java.util.UUID;
import ru.mirea.firefighter.application.port.IdGenerator;

/** Генератор идентификаторов на основе UUID. */
public final class UuidGenerator implements IdGenerator {

    @Override
    public String nextId() {
        return UUID.randomUUID().toString();
    }
}