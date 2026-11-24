package ru.mirea.firefighter.domain.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import ru.mirea.firefighter.domain.model.LeaderboardEntry;

/**
 * Алгоритм 4 (п. 2.5 отчёта): таблица лидеров.
 *
 * <p>Порядок: по убыванию комбинированного рейтинга, при равенстве —
 * по возрастанию времени выполнения, затем по дате завершения. Именно
 * эта последовательность критериев задана в аналитическом этапе, поэтому
 * она зафиксирована в одном классе, а не размазана по вызывающему коду.</p>
 *
 * <p>Сложность O(N log N) по времени и O(N) по памяти — как и в
 * процедурной реализации на Java.</p>
 */
public final class LeaderboardPolicy {

    private static final Comparator<LeaderboardEntry> RANKING =
            Comparator.comparingDouble((LeaderboardEntry entry) -> entry.combined().value()).reversed()
                    .thenComparingDouble(LeaderboardEntry::elapsedSeconds)
                    .thenComparing(LeaderboardEntry::finishedAt);

    /**
     * Отсортировать результаты и отобрать лучшие {@code limit} записей.
     *
     * @param entries результаты тренировок (порядок не важен)
     * @param limit   размер таблицы, должен быть положительным
     * @return лучшие результаты в порядке мест
     */
    public List<LeaderboardEntry> rank(Collection<LeaderboardEntry> entries, int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Размер таблицы лидеров должен быть положительным");
        }
        List<LeaderboardEntry> sorted = new ArrayList<>(entries);
        sorted.sort(RANKING);
        return List.copyOf(sorted.subList(0, Math.min(limit, sorted.size())));
    }
}