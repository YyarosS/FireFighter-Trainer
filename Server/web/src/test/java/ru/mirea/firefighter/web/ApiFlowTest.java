package ru.mirea.firefighter.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Интеграционная проверка REST API: поднимается реальный Spring-контекст
 * с in-memory адаптерами и проверяется весь протокол клиент↔сервер.
 *
 * <p>Числа совпадают с процедурной реализацией и тестом прикладного слоя:
 * V ≈ 27.22, A = 100, R ≈ 70.89.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("REST API: сквозной сценарий тренировки")
class ApiFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Регистрация, старт, телеметрия, оценка и таблица лидеров")
    void fullTrainingFlow() throws Exception {
        String traineeJson = """
                {"fullName":"Иванов И.И.","studyGroup":"ИКБО-30-24","vrDeviceId":"Quest-3-01"}
                """;
        String traineeId = readId(mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(traineeJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Иванов И.И."))
                .andReturn());

        String scenarioJson = """
                {"title":"Возгорание в серверной","description":"Сценарий с задымлением",
                 "temperatureCelsius":60,"visibilityMeters":5,"toxinConcentrationPpm":120,
                 "timeLimitSeconds":180}
                """;
        String scenarioId = readId(mockMvc.perform(post("/api/scenarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(scenarioJson))
                .andExpect(status().isCreated())
                .andReturn());

        String startJson = """
                {"traineeId":"%s","scenarioId":"%s"}
                """.formatted(traineeId, scenarioId);
        MvcResult startResult = mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(startJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.environment.temperatureCelsius").value(60.0))
                .andReturn();
        String sessionId = JsonPath.read(startResult.getResponse().getContentAsString(), "$.sessionId");

        mockMvc.perform(get("/api/sessions/{id}/commands", sessionId).param("after", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("START_SCENARIO"))
                .andExpect(jsonPath("$[0].timeLimitSeconds").value(180.0));

        String eventsJson = """
                {"events":[
                  {"type":"ACTION","targetObject":"extinguisher","atSeconds":21.5},
                  {"type":"ACTION","targetObject":"door","atSeconds":48.0},
                  {"type":"ACTION","targetObject":"hose","atSeconds":96.4},
                  {"type":"ACTION","targetObject":"victim","atSeconds":131.0}
                ]}
                """;
        mockMvc.perform(post("/api/sessions/{id}/events", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventsJson))
                .andExpect(status().isNoContent());

        MvcResult finishResult = mockMvc.perform(post("/api/sessions/{id}/finish", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grade").value("SATISFACTORY"))
                .andReturn();

        String finishBody = finishResult.getResponse().getContentAsString();
        assertThat((Double) JsonPath.read(finishBody, "$.speed")).isCloseTo(27.2222, offset(1e-4));
        assertThat((Double) JsonPath.read(finishBody, "$.accuracy")).isCloseTo(100.0, offset(1e-9));
        assertThat((Double) JsonPath.read(finishBody, "$.combined")).isCloseTo(70.8889, offset(1e-4));

        mockMvc.perform(get("/api/leaderboard").param("scenarioId", scenarioId).param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].place").value(1))
                .andExpect(jsonPath("$[0].traineeName").value("Иванов И.И."));

        mockMvc.perform(get("/api/trainees/{id}/sessions", traineeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("FINISHED"));
    }

    @Test
    @DisplayName("Неизвестная тренировка даёт 404, пустое ФИО — 400")
    void errorResponses() throws Exception {
        mockMvc.perform(get("/api/sessions/does-not-exist"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"\",\"studyGroup\":\"ИКБО-30-24\",\"vrDeviceId\":\"Quest\"}"))
                .andExpect(status().isBadRequest());
    }

    private static String readId(MvcResult result) throws Exception {
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }
}