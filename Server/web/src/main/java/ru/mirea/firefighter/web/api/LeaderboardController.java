package ru.mirea.firefighter.web.api;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mirea.firefighter.application.dto.LeaderboardRow;
import ru.mirea.firefighter.application.usecase.GetLeaderboardService;

/** REST-контроллер таблицы лидеров (алгоритм 4). */
@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final GetLeaderboardService leaderboard;

    public LeaderboardController(GetLeaderboardService leaderboard) {
        this.leaderboard = leaderboard;
    }

    @GetMapping
    public List<LeaderboardRow> leaderboard(@RequestParam String scenarioId,
                                            @RequestParam(defaultValue = "10") int limit) {
        return leaderboard.leaderboard(scenarioId, limit);
    }
}