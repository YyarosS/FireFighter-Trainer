using System;

namespace Firefighter.Domain
{
    /// <summary>Команда запуска сценария: параметры среды и норматив времени.</summary>
    public sealed class StartScenarioCommand : ServerCommand
    {
        public StartScenarioCommand(
            long sequence,
            DateTimeOffset issuedAt,
            string scenarioId,
            double timeLimitSeconds,
            EnvironmentSnapshot environment)
            : base(sequence, issuedAt)
        {
            if (string.IsNullOrWhiteSpace(scenarioId))
            {
                throw new ArgumentException("Идентификатор сценария не может быть пустым", nameof(scenarioId));
            }

            ScenarioId = scenarioId;
            TimeLimitSeconds = timeLimitSeconds;
            Environment = environment ?? throw new ArgumentNullException(nameof(environment));
        }

        public string ScenarioId { get; }

        public double TimeLimitSeconds { get; }

        public EnvironmentSnapshot Environment { get; }

        public override string Type => "START_SCENARIO";

        public override void ApplyTo(TrainingSessionState state)
        {
            state.Begin(ScenarioId, TimeLimitSeconds, Environment);
        }
    }
}