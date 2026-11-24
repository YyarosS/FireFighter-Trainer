using System;

namespace Firefighter.Domain
{
    /// <summary>Описание сценария из каталога сервера (для экрана выбора).</summary>
    public sealed class ScenarioInfo
    {
        public ScenarioInfo(string id, string title, double timeLimitSeconds, EnvironmentSnapshot environment)
        {
            if (string.IsNullOrWhiteSpace(id))
            {
                throw new ArgumentException("Идентификатор сценария не может быть пустым", nameof(id));
            }

            Id = id;
            Title = title ?? string.Empty;
            TimeLimitSeconds = timeLimitSeconds;
            Environment = environment ?? throw new ArgumentNullException(nameof(environment));
        }

        public string Id { get; }

        public string Title { get; }

        public double TimeLimitSeconds { get; }

        public EnvironmentSnapshot Environment { get; }
    }
}