package ru.mirea.firefighter.infrastructure.time;

import java.time.Instant;
import ru.mirea.firefighter.application.port.Clock;

/** Системные часы — рабочая реализация порта {@link Clock}. */
public final class SystemClock implements Clock {

    @Override
    public Instant now() {
        return Instant.now();
    }
}