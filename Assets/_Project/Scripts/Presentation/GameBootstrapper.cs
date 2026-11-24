using System;
using System.Threading;
using System.Threading.Tasks;
using Firefighter.Application;
using Firefighter.Domain;
using EventType = Firefighter.Domain.EventType;
using Firefighter.Infrastructure;
using UnityEngine;

namespace Firefighter.Presentation
{
    /// <summary>
    /// Точка сборки клиента (composition root).
    ///
    /// <para>Единственный компонент сцены, который знает обо всех слоях:
    /// создаёт шлюз, часы, журнал и координатор. Дальше сцена работает
    /// только с <see cref="Coordinator"/> и событиями, поэтому замена
    /// транспорта (HTTP → WebSocket) не затрагивает игровые объекты.</para>
    /// </summary>
    public sealed class GameBootstrapper : MonoBehaviour
    {
        [Header("Сервер")]
        [SerializeField]
        [Tooltip("Базовый адрес серверной части, например http://localhost:8080")]
        private string serverUrl = "http://localhost:8080";

        [Header("Тренировка")]
        [SerializeField]
        private string traineeId = "trainee-1";

        [SerializeField]
        private string scenarioId = "";

        [SerializeField]
        [Tooltip("Автоматически начинать сеанс при старте сцены")]
        private bool autoStart;

        [Header("Сеть")]
        [SerializeField]
        [Tooltip("Период отправки пакета телеметрии, с")]
        private float flushIntervalSeconds = 0.5f;

        [SerializeField]
        [Tooltip("Период опроса команд сервера, с")]
        private float pollIntervalSeconds = 1.0f;

        private CancellationTokenSource _cancellation;
        private bool _networkBusy;

        /// <summary>Координатор сеанса — единственная точка доступа к логике.</summary>
        public TrainingSessionCoordinator Coordinator { get; private set; }

        /// <summary>Последний полученный результат тренировки.</summary>
        public TrainingResult LastResult { get; private set; }

        /// <summary>Событие: сервер вернул итоговую оценку.</summary>
        public event Action<TrainingResult> ResultReceived;

        public string TraineeId
        {
            get => traineeId;
            set => traineeId = value;
        }

        public string ScenarioId
        {
            get => scenarioId;
            set => scenarioId = value;
        }

        private void Awake()
        {
            Coordinator = new TrainingSessionCoordinator(
                new UnityTrainingGateway(serverUrl),
                new UnityClock(),
                new UnityLogSink(),
                flushIntervalSeconds: flushIntervalSeconds,
                pollIntervalSeconds: pollIntervalSeconds);
        }

        private void Start()
        {
            _cancellation = new CancellationTokenSource();

            if (autoStart && !string.IsNullOrWhiteSpace(traineeId) && !string.IsNullOrWhiteSpace(scenarioId))
            {
                StartSession();
            }
        }

        private void OnDestroy()
        {
            _cancellation?.Cancel();
            _cancellation?.Dispose();
        }

        private void Update()
        {
            if (Coordinator == null || _cancellation == null)
            {
                return;
            }

            Coordinator.Tick(Time.deltaTime);

            if (_networkBusy)
            {
                return;
            }

            if (Coordinator.IsFlushDue)
            {
                RunNetwork(Coordinator.FlushAsync(_cancellation.Token));
            }
            else if (Coordinator.IsPollDue)
            {
                RunNetwork(Coordinator.PollCommandsAsync(_cancellation.Token));
            }
        }

        /// <summary>Запустить сеанс тренировки.</summary>
        public void StartSession()
        {
            RunNetwork(StartSessionAsync());
        }

        /// <summary>Завершить сеанс и получить оценку.</summary>
        public void FinishSession()
        {
            RunNetwork(FinishSessionAsync());
        }

        /// <summary>Зафиксировать действие стажёра.</summary>
        public bool RecordEvent(EventType type, string targetObject)
        {
            return Coordinator != null && Coordinator.RecordEvent(type, targetObject);
        }

        private async Task StartSessionAsync()
        {
            await Coordinator.StartAsync(traineeId, scenarioId, _cancellation.Token);
        }

        private async Task FinishSessionAsync()
        {
            LastResult = await Coordinator.FinishAsync(_cancellation.Token);
            ResultReceived?.Invoke(LastResult);
        }

        /// <summary>
        /// Запуск сетевой операции без блокировки кадра. Исключения
        /// логируются: сбой одного запроса не должен останавливать VR-сеанс.
        /// </summary>
        private async void RunNetwork(Task operation)
        {
            _networkBusy = true;
            try
            {
                await operation;
            }
            catch (OperationCanceledException)
            {
                // сцена закрывается — это ожидаемо
            }
            catch (Exception exception)
            {
                Debug.LogWarning("[Firefighter] Сетевая операция не выполнена: " + exception.Message);
            }
            finally
            {
                _networkBusy = false;
            }
        }
    }
}