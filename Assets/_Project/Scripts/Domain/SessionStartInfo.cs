using System;

namespace Firefighter.Domain
{
    /// <summary>Ответ сервера на запрос запуска тренировки.</summary>
    public sealed class SessionStartInfo
    {
        public SessionStartInfo(
            string sessionId,
            string traineeId,
            string scenarioId,
            double lastCommandSequence,
            EnvironmentSnapshot environment)
        {
            if (string.IsNullOrWhiteSpace(sessionId))
            {
                throw new ArgumentException("Идентификатор тренировки не может быть пустым", nameof(sessionId));
            }

            SessionId = sessionId;
            TraineeId = traineeId ?? string.Empty;
            ScenarioId = scenarioId ?? string.Empty;
            LastCommandSequence = lastCommandSequence;
            Environment = environment ?? throw new ArgumentNullException(nameof(environment));
        }

        public string SessionId { get; }

        public string TraineeId { get; }

        public string ScenarioId { get; }

        public double LastCommandSequence { get; }

        public EnvironmentSnapshot Environment { get; }
    }
}