package ru.mirea.firefighter.web.api;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.mirea.firefighter.application.dto.ScenarioView;
import ru.mirea.firefighter.application.usecase.CreateScenarioService;
import ru.mirea.firefighter.application.usecase.ListScenariosService;
import ru.mirea.firefighter.web.api.dto.CreateScenarioRequest;

/** REST-контроллер каталога сценариев. */
@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {

    private final CreateScenarioService createScenario;
    private final ListScenariosService listScenarios;

    public ScenarioController(CreateScenarioService createScenario, ListScenariosService listScenarios) {
        this.createScenario = createScenario;
        this.listScenarios = listScenarios;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScenarioView create(@Valid @RequestBody CreateScenarioRequest request) {
        return createScenario.create(
                request.title(),
                request.description(),
                request.environment(),
                request.timeLimitSeconds());
    }

    @GetMapping
    public List<ScenarioView> list() {
        return listScenarios.list();
    }
}