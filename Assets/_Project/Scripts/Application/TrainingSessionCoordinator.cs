using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using Firefighter.Application.Ports;
using Firefighter.Domain;

namespace Firefighter.Application
{
    /// <summary>
    /// Координатор сеанса тренировки на клиенте.
    ///
    /// <para>Связывает состояние сеанса, буфер телеметрии и шлюз к серверу,
    /// но не знает ни об Unity, ни о HTTP. Вся логика протокола —
    /// «накопить события → отправить пакетом → забрать команды →
    /// применить к состоянию» — сосредоточена здесь и проверяется
    /// модульными тестами с поддельным шлюзом.</para>
    ///
    /// <para>Сетевые вызовы не выполняются в <see cref="Tick"/>: метод
    /// только сдвигает время. Монобехавиор вызывает
    /// <see cref="IsFlushDue"/> / <see cref="IsPollDue"/> и запускает
    /// асинхронные операции, поэтому кадр в VR не блокируется.</para>
    /// </summary>
    public sealed class TrainingSessionCoordinator
    {
        /// <summary>Максимальный размер пакета телеметрии за один запрос.</summary>
        public const int MaxEventsPerBatch = 64;

        private readonly ITrainingGateway _gateway;
        private readonly IClock _clock;
        private readonly ILogSink _log;
        private readonly TelemetryBuffer _buffer;
        private readonly SessionStateMachine _stateMachine = new SessionStateMachine();
        private readonly TrainingSessionState _sessionState;
        private readonly double _flushIntervalSeconds;
        private readonly double _pollIntervalSeconds;

        private string _sessionId = string.Empty;
        private double _sessionStartMonotonic;
        private double _lastFlushMonotonic;
        private double _lastPollMonotonic;
        private long _lastCommandSequence;

        public TrainingSessionCoordinator(
            ITrainingGateway gateway,
            IClock clock,
            ILogSink log,
            int telemetryCapacity = 256,
            double flushIntervalSeconds = 0.5,
            double pollIntervalSeconds = 1.0)
        {
            _gateway = gateway ?? throw new ArgumentNullException(nameof(gateway));
            _clock = clock ?? throw new ArgumentNullException(nameof(clock));
            _log = log ?? throw new ArgumentNullException(nameof(log));

            if (flushIntervalSeconds <= 0.0)
            {
                throw new ArgumentOutOfRangeException(nameof(flushIntervalSeconds));
            }

            if (pollIntervalSeconds <= 0.0)
            {
                throw new ArgumentOutOfRangeException(nameof(pollIntervalSeconds));
            }

            _flushIntervalSeconds = flushIntervalSeconds;
            _pollIntervalSeconds = pollIntervalSeconds;
            _buffer = new TelemetryBuffer(telemetryCapacity);
            _sessionState = new TrainingSessionState();
        }

        /// <summary>Текущее состояние сеанса.</summary>
        public SessionState State => _stateMachine.State;

        /// <summary>Состояние тренировки, отображаемое в VR.</summary>
        public TrainingSessionState Session => _sessionState;

        public string SessionId => _sessionId;

        public long LastCommandSequence => _lastCommandSequence;

        public int PendingEvents => _buffer.PendingCount;

        /// <summary>Пора ли отправить накопленную телеметрию.</summary>
        public bool IsFlushDue => State == SessionState.Running
                                  && SecondsSince(_lastFlushMonotonic) >= _flushIntervalSeconds;

        /// <summary>Пора ли опросить команды сервера.</summary>
        public bool IsPollDue => State == SessionState.Running
                                 && SecondsSince(_lastPollMonotonic) >= _pollIntervalSeconds;

        /// <summary>Запустить сеанс: сервер создаёт тренировку и выдаёт START_SCENARIO.</summary>
        public async Task<SessionStartInfo> StartAsync(
            string traineeId,
            string scenarioId,
            CancellationToken cancellationToken = default)
        {
            if (State != SessionState.Idle && State != SessionState.Failed)
            {
                throw new InvalidOperationException($"Сеанс уже находится в состоянии {State}");
            }

            _stateMachine.BeginConnecting();
            try
            {
                SessionStartInfo info = await _gateway.StartSessionAsync(traineeId, scenarioId, cancellationToken);

                _sessionId = info.SessionId;
                _sessionStartMonotonic = _clock.MonotonicSeconds;
                _lastFlushMonotonic = _sessionStartMonotonic;
                _lastPollMonotonic = _sessionStartMonotonic;
                _lastCommandSequence = 0;
                _buffer.Clear();

                _stateMachine.MarkRunning();
                await PollCommandsAsync(cancellationToken);

                _log.Info($"Тренировка {_sessionId} запущена");
                return info;
            }
            catch (Exception exception)
            {
                _stateMachine.MarkFailed(exception.Message);
                _log.Error("Не удалось запустить тренировку", exception);
                throw;
            }
        }

        /// <summary>
        /// Зафиксировать действие стажёра. Возвращает <c>false</c>, если
        /// сеанс не идёт или буфер переполнен, — вызывающий код может
        /// показать предупреждение, но кадр не прерывается.
        /// </summary>
        public bool RecordEvent(EventType type, string targetObject)
        {
            if (!_stateMachine.CanAcceptTelemetry)
            {
                _log.Warning($"Событие {type} проигнорировано: сеанс в состоянии {State}");
                return false;
            }

            double atSeconds = Math.Max(0.0, _clock.MonotonicSeconds - _sessionStartMonotonic);
            if (!_buffer.TryRecord(new TelemetryEvent(type, targetObject, atSeconds)))
            {
                _log.Warning("Буфер телеметрии переполнен, событие не принято");
                return false;
            }

            return true;
        }

        /// <summary>Такт времени: таймер сеанса и сглаживание среды.</summary>
        public void Tick(double deltaSeconds)
        {
            _sessionState.Advance(deltaSeconds);
        }

        /// <summary>Отправить пакет накопленных событий.</summary>
        public async Task<int> FlushAsync(CancellationToken cancellationToken = default)
        {
            if (State != SessionState.Running)
            {
                return 0;
            }

            IReadOnlyList<TelemetryEvent> batch = _buffer.Drain(MaxEventsPerBatch);
            _lastFlushMonotonic = _clock.MonotonicSeconds;

            if (batch.Count == 0)
            {
                return 0;
            }

            await _gateway.SendEventsAsync(_sessionId, batch, cancellationToken);
            _log.Info($"Отправлен пакет телеметрии: {batch.Count} событий");
            return batch.Count;
        }

        /// <summary>Забрать новые команды сервера и применить их к состоянию.</summary>
        public async Task<int> PollCommandsAsync(CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrEmpty(_sessionId))
            {
                return 0;
            }

            IReadOnlyList<ServerCommand> commands =
                await _gateway.FetchCommandsAsync(_sessionId, _lastCommandSequence, cancellationToken);

            int applied = 0;
            foreach (ServerCommand command in commands)
            {
                command.ApplyTo(_sessionState);
                _lastCommandSequence = command.Sequence;
                applied++;
            }

            _lastPollMonotonic = _clock.MonotonicSeconds;
            return applied;
        }

        /// <summary>Завершить сеанс и получить итоговую оценку сервера.</summary>
        public async Task<TrainingResult> FinishAsync(CancellationToken cancellationToken = default)
        {
            if (State != SessionState.Running)
            {
                throw new InvalidOperationException($"Завершить можно только идущий сеанс (текущее состояние: {State})");
            }

            await FlushAsync(cancellationToken);
            _stateMachine.BeginFinishing();

            try
            {
                TrainingResult result = await _gateway.FinishSessionAsync(_sessionId, cancellationToken);
                await PollCommandsAsync(cancellationToken);

                // Итог из HTTP-ответа содержит качественную оценку, которой нет в команде.
                _sessionState.Complete(result);

                _stateMachine.MarkFinished();
                _log.Info($"Тренировка завершена: R = {result.Combined:F2} ({result.GradeLabel})");
                return result;
            }
            catch (Exception exception)
            {
                _stateMachine.MarkFailed(exception.Message);
                _log.Error("Не удалось завершить тренировку", exception);
                throw;
            }
        }

        /// <summary>Каталог сценариев (для экрана выбора).</summary>
        public Task<IReadOnlyList<ScenarioInfo>> GetScenariosAsync(CancellationToken cancellationToken = default)
        {
            return _gateway.GetScenariosAsync(cancellationToken);
        }

        /// <summary>Таблица лидеров по сценарию.</summary>
        public Task<IReadOnlyList<LeaderboardRowInfo>> GetLeaderboardAsync(
            string scenarioId,
            int limit,
            CancellationToken cancellationToken = default)
        {
            return _gateway.GetLeaderboardAsync(scenarioId, limit, cancellationToken);
        }

        private double SecondsSince(double monotonicMark)
        {
            return _clock.MonotonicSeconds - monotonicMark;
        }
    }
}