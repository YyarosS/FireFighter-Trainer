using System;
using System.Collections.Generic;
using System.Globalization;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Firefighter.Application.Ports;
using Firefighter.Domain;
using EventType = Firefighter.Domain.EventType;
using UnityEngine;
using UnityEngine.Networking;

namespace Firefighter.Infrastructure
{
    /// <summary>
    /// HTTP-шлюз к серверной части на UnityWebRequest.
    ///
    /// <para>Здесь сосредоточена вся «грязная» работа: формирование URL,
    /// JSON-сериализация через JsonUtility, разбор ответов и перенос
    /// данных в доменные объекты. Прикладной слой видит только
    /// <see cref="ITrainingGateway"/>.</para>
    ///
    /// <para>Сервер отдаёт массивы верхнего уровня, а JsonUtility умеет
    /// разбирать только объект, поэтому массивы оборачиваются в
    /// <c>{"items":[...]}</c> перед разбором.</para>
    /// </summary>
    public sealed class UnityTrainingGateway : ITrainingGateway
    {
        private readonly string _baseUrl;

        public UnityTrainingGateway(string baseUrl)
        {
            if (string.IsNullOrWhiteSpace(baseUrl))
            {
                throw new ArgumentException("Адрес сервера не может быть пустым", nameof(baseUrl));
            }

            _baseUrl = baseUrl.TrimEnd('/');
        }

        public async Task<SessionStartInfo> StartSessionAsync(
            string traineeId, string scenarioId, CancellationToken cancellationToken)
        {
            var request = new StartSessionRequestDto
            {
                traineeId = traineeId,
                scenarioId = scenarioId
            };

            string response = await SendAsync("POST", "/api/sessions", JsonUtility.ToJson(request), cancellationToken);
            StartSessionResponseDto dto = JsonUtility.FromJson<StartSessionResponseDto>(response);
            return new SessionStartInfo(
                dto.sessionId,
                dto.traineeId,
                dto.scenarioId,
                dto.lastCommandSequence,
                ToEnvironment(dto.environment));
        }

        public Task SendEventsAsync(
            string sessionId, IReadOnlyList<TelemetryEvent> events, CancellationToken cancellationToken)
        {
            if (events == null || events.Count == 0)
            {
                return Task.CompletedTask;
            }

            var payload = new EventsRequestDto
            {
                events = new EventDto[events.Count]
            };

            for (int index = 0; index < events.Count; index++)
            {
                TelemetryEvent telemetryEvent = events[index];
                payload.events[index] = new EventDto
                {
                    type = ToWireType(telemetryEvent.Type),
                    targetObject = telemetryEvent.TargetObject,
                    atSeconds = telemetryEvent.AtSeconds
                };
            }

            string path = $"/api/sessions/{UnityWebRequest.EscapeURL(sessionId)}/events";
            return SendAsync("POST", path, JsonUtility.ToJson(payload), cancellationToken);
        }

        public async Task<IReadOnlyList<ServerCommand>> FetchCommandsAsync(
            string sessionId, long afterSequence, CancellationToken cancellationToken)
        {
            string path = $"/api/sessions/{UnityWebRequest.EscapeURL(sessionId)}/commands?after={afterSequence}";
            string response = await SendAsync("GET", path, null, cancellationToken);

            var list = JsonUtility.FromJson<CommandListDto>("{\"items\":" + response + "}");
            var commands = new List<ServerCommand>();
            if (list?.items == null)
            {
                return commands;
            }

            foreach (CommandEnvelopeDto dto in list.items)
            {
                ServerCommand command = MapCommand(dto);
                if (command != null)
                {
                    commands.Add(command);
                }
            }

            return commands;
        }

        public async Task<TrainingResult> FinishSessionAsync(string sessionId, CancellationToken cancellationToken)
        {
            string path = $"/api/sessions/{UnityWebRequest.EscapeURL(sessionId)}/finish";
            string response = await SendAsync("POST", path, null, cancellationToken);
            SessionResultDto dto = JsonUtility.FromJson<SessionResultDto>(response);
            return new TrainingResult(dto.speed, dto.accuracy, dto.combined, dto.gradeLabel);
        }

        public async Task<IReadOnlyList<ScenarioInfo>> GetScenariosAsync(CancellationToken cancellationToken)
        {
            string response = await SendAsync("GET", "/api/scenarios", null, cancellationToken);
            var list = JsonUtility.FromJson<ScenarioListDto>("{\"items\":" + response + "}");

            var scenarios = new List<ScenarioInfo>();
            if (list?.items == null)
            {
                return scenarios;
            }

            foreach (ScenarioDto dto in list.items)
            {
                scenarios.Add(new ScenarioInfo(
                    dto.id,
                    dto.title,
                    dto.timeLimitSeconds,
                    ToEnvironment(dto.environment)));
            }

            return scenarios;
        }

        public async Task<IReadOnlyList<LeaderboardRowInfo>> GetLeaderboardAsync(
            string scenarioId, int limit, CancellationToken cancellationToken)
        {
            string path = $"/api/leaderboard?scenarioId={UnityWebRequest.EscapeURL(scenarioId)}&limit={limit}";
            string response = await SendAsync("GET", path, null, cancellationToken);
            var list = JsonUtility.FromJson<LeaderboardListDto>("{\"items\":" + response + "}");

            var rows = new List<LeaderboardRowInfo>();
            if (list?.items == null)
            {
                return rows;
            }

            foreach (LeaderboardRowDto dto in list.items)
            {
                rows.Add(new LeaderboardRowInfo(
                    dto.place, dto.traineeName, dto.speed, dto.accuracy, dto.combined, dto.elapsedSeconds));
            }

            return rows;
        }

        // ------------------------------------------------------------------ транспорт

        private Task<string> SendAsync(string method, string path, string body, CancellationToken cancellationToken)
        {
            using (var request = new UnityWebRequest(_baseUrl + path, method))
            {
                request.downloadHandler = new DownloadHandlerBuffer();
                request.SetRequestHeader("Accept", "application/json");

                if (!string.IsNullOrEmpty(body))
                {
                    request.uploadHandler = new UploadHandlerRaw(Encoding.UTF8.GetBytes(body));
                    request.SetRequestHeader("Content-Type", "application/json");
                }

                var completion = new TaskCompletionSource<string>();
                using (cancellationToken.Register(() =>
                       {
                           request.Abort();
                           completion.TrySetCanceled();
                       }))
                {
                    UnityWebRequestAsyncOperation operation = request.SendWebRequest();
                    operation.completed += _ =>
                    {
                        if (request.result == UnityWebRequest.Result.Success)
                        {
                            completion.TrySetResult(request.downloadHandler.text);
                        }
                        else
                        {
                            completion.TrySetException(new InvalidOperationException(
                                $"HTTP {request.responseCode} {method} {path}: {request.error} {request.downloadHandler.text}"));
                        }
                    };

                    return completion.Task;
                }
            }
        }

        // ------------------------------------------------------------------ преобразования

        private static EnvironmentSnapshot ToEnvironment(EnvironmentDto dto)
        {
            return dto == null
                ? EnvironmentSnapshot.Calm
                : new EnvironmentSnapshot(dto.temperatureCelsius, dto.visibilityMeters, dto.toxinConcentrationPpm);
        }

        private static string ToWireType(EventType type)
        {
            switch (type)
            {
                case EventType.Action: return "ACTION";
                case EventType.SafetyViolation: return "SAFETY_VIOLATION";
                case EventType.WrongEquipment: return "WRONG_EQUIPMENT";
                case EventType.MissedStage: return "MISSED_STAGE";
                default: throw new ArgumentOutOfRangeException(nameof(type), type, "Неизвестный тип события");
            }
        }

        private static ServerCommand MapCommand(CommandEnvelopeDto dto)
        {
            if (dto == null || string.IsNullOrEmpty(dto.type))
            {
                return null;
            }

            DateTimeOffset issuedAt = ParseIssuedAt(dto.issuedAt);
            switch (dto.type)
            {
                case "START_SCENARIO":
                    return new StartScenarioCommand(
                        dto.sequence, issuedAt, dto.scenarioId, dto.timeLimitSeconds, ToEnvironment(dto.environment));
                case "UPDATE_ENVIRONMENT":
                    return new UpdateEnvironmentCommand(dto.sequence, issuedAt, ToEnvironment(dto.environment));
                case "SHOW_HINT":
                    return new ShowHintCommand(dto.sequence, issuedAt, dto.hint);
                case "END_SESSION":
                    return new EndSessionCommand(dto.sequence, issuedAt, dto.speed, dto.accuracy, dto.combined);
                default:
                    return null;
            }
        }

        private static DateTimeOffset ParseIssuedAt(string value)
        {
            return DateTimeOffset.TryParse(
                value,
                CultureInfo.InvariantCulture,
                DateTimeStyles.RoundtripKind,
                out DateTimeOffset parsed)
                ? parsed
                : DateTimeOffset.UtcNow;
        }

        // ------------------------------------------------------------------ DTO (JsonUtility)

        [Serializable]
        private sealed class StartSessionRequestDto
        {
            public string traineeId;
            public string scenarioId;
        }

        [Serializable]
        private sealed class EventsRequestDto
        {
            public EventDto[] events;
        }

        [Serializable]
        private sealed class EventDto
        {
            public string type;
            public string targetObject;
            public double atSeconds;
        }

        [Serializable]
        private sealed class StartSessionResponseDto
        {
            public string sessionId;
            public string traineeId;
            public string scenarioId;
            public long lastCommandSequence;
            public EnvironmentDto environment;
        }

        [Serializable]
        private sealed class EnvironmentDto
        {
            public double temperatureCelsius;
            public double visibilityMeters;
            public double toxinConcentrationPpm;
        }

        [Serializable]
        private sealed class CommandEnvelopeDto
        {
            public long sequence;
            public string type;
            public string issuedAt;
            public string scenarioId;
            public double timeLimitSeconds;
            public EnvironmentDto environment;
            public string hint;
            public double speed;
            public double accuracy;
            public double combined;
        }

        [Serializable]
        private sealed class CommandListDto
        {
            public CommandEnvelopeDto[] items;
        }

        [Serializable]
        private sealed class SessionResultDto
        {
            public double speed;
            public double accuracy;
            public double combined;
            public string gradeLabel;
        }

        [Serializable]
        private sealed class ScenarioListDto
        {
            public ScenarioDto[] items;
        }

        [Serializable]
        private sealed class ScenarioDto
        {
            public string id;
            public string title;
            public double timeLimitSeconds;
            public EnvironmentDto environment;
        }

        [Serializable]
        private sealed class LeaderboardListDto
        {
            public LeaderboardRowDto[] items;
        }

        [Serializable]
        private sealed class LeaderboardRowDto
        {
            public int place;
            public string traineeName;
            public double speed;
            public double accuracy;
            public double combined;
            public double elapsedSeconds;
        }
    }
}