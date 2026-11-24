using System;

namespace Firefighter.Domain
{
    /// <summary>
    /// Состояние тренировки на стороне клиента: параметры сценария,
    /// сглаженная среда, таймер и итоговый результат.
    ///
    /// <para>Изменяется только командами сервера и тактом времени
    /// (<see cref="Advance"/>). Клиент не может «назначить» себе результат:
    /// <see cref="Result"/> появляется только из команды END_SESSION.</para>
    /// </summary>
    public sealed class TrainingSessionState
    {
        private readonly EnvironmentInterpolator _interpolator;

        public TrainingSessionState(EnvironmentInterpolator interpolator = null)
        {
            _interpolator = interpolator ?? new EnvironmentInterpolator();
        }

        public bool IsStarted { get; private set; }

        public bool IsCompleted { get; private set; }

        public string ScenarioId { get; private set; } = string.Empty;

        public double TimeLimitSeconds { get; private set; }

        public double ElapsedSeconds { get; private set; }

        public double RemainingSeconds => Math.Max(0.0, TimeLimitSeconds - ElapsedSeconds);

        /// <summary>Отображаемая среда (сглаженная).</summary>
        public EnvironmentSnapshot CurrentEnvironment { get; private set; } = EnvironmentSnapshot.Calm;

        /// <summary>Целевая среда, полученная от сервера.</summary>
        public EnvironmentSnapshot TargetEnvironment { get; private set; } = EnvironmentSnapshot.Calm;

        public string LastHint { get; private set; } = string.Empty;

        public TrainingResult Result { get; private set; }

        public void Begin(string scenarioId, double timeLimitSeconds, EnvironmentSnapshot environment)
        {
            if (IsStarted)
            {
                throw new InvalidOperationException("Сеанс уже запущен");
            }

            if (string.IsNullOrWhiteSpace(scenarioId))
            {
                throw new ArgumentException("Идентификатор сценария не может быть пустым", nameof(scenarioId));
            }

            if (double.IsNaN(timeLimitSeconds) || timeLimitSeconds <= 0.0)
            {
                throw new ArgumentOutOfRangeException(nameof(timeLimitSeconds), "Норматив времени должен быть положительным");
            }

            if (environment == null)
            {
                throw new ArgumentNullException(nameof(environment));
            }

            ScenarioId = scenarioId;
            TimeLimitSeconds = timeLimitSeconds;
            TargetEnvironment = environment;
            CurrentEnvironment = environment;
            ElapsedSeconds = 0.0;
            IsStarted = true;
        }

        public void SetEnvironmentTarget(EnvironmentSnapshot environment)
        {
            if (!IsStarted)
            {
                throw new InvalidOperationException("Нельзя изменить среду до запуска сеанса");
            }

            TargetEnvironment = environment ?? throw new ArgumentNullException(nameof(environment));
        }

        public void AddHint(string text)
        {
            if (string.IsNullOrWhiteSpace(text))
            {
                throw new ArgumentException("Текст подсказки не может быть пустым", nameof(text));
            }

            LastHint = text;
        }

        public void Complete(TrainingResult result)
        {
            Result = result ?? throw new ArgumentNullException(nameof(result));
            IsCompleted = true;
        }

        /// <summary>Такт времени: таймер и сглаживание среды.</summary>
        public void Advance(double deltaSeconds)
        {
            if (!IsStarted || IsCompleted || deltaSeconds <= 0.0)
            {
                return;
            }

            ElapsedSeconds += deltaSeconds;
            CurrentEnvironment = _interpolator.Step(CurrentEnvironment, TargetEnvironment, deltaSeconds);
        }
    }
}