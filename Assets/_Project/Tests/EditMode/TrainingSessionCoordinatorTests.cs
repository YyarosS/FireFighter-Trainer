using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Firefighter.Application;
using Firefighter.Application.Ports;
using Firefighter.Domain;
using NUnit.Framework;

namespace Firefighter.Tests
{
    /// <summary>
    /// Проверки координатора сеанса с поддельным шлюзом: проверяется
    /// именно логика протокола, без сети и без Unity.
    /// </summary>
    public sealed class TrainingSessionCoordinatorTests
    {
        [Test]
        public void StartAppliesStartCommandFromServer()
        {
            FakeGateway gateway = WithStartCommand();
            TrainingSessionCoordinator coordinator = Create(gateway);

            coordinator.StartAsync("trainee-1", "scenario-1").GetAwaiter().GetResult();

            Assert.AreEqual(SessionState.Running, coordinator.State);
            Assert.IsTrue(coordinator.Session.IsStarted);
            Assert.AreEqual(180.0, coordinator.Session.TimeLimitSeconds, 1e-9);
            Assert.AreEqual(1, coordinator.LastCommandSequence);
        }

        [Test]
        public void EventsAreRejectedUntilSessionRuns()
        {
            TrainingSessionCoordinator coordinator = Create(new FakeGateway());

            Assert.IsFalse(coordinator.RecordEvent(EventType.Action, "door"));
        }

        [Test]
        public void TelemetryIsBufferedAndSentAsBatch()
        {
            FakeGateway gateway = WithStartCommand();
            TrainingSessionCoordinator coordinator = Create(gateway);
            coordinator.StartAsync("trainee-1", "scenario-1").GetAwaiter().GetResult();

            Assert.IsTrue(coordinator.RecordEvent(EventType.Action, "door"));
            Assert.IsTrue(coordinator.RecordEvent(EventType.SafetyViolation, "hose"));
            Assert.AreEqual(2, coordinator.PendingEvents);

            int sent = coordinator.FlushAsync().GetAwaiter().GetResult();

            Assert.AreEqual(2, sent);
            Assert.AreEqual(2, gateway.SentEvents.Count);
            Assert.AreEqual(0, coordinator.PendingEvents);
        }

        [Test]
        public void PollAppliesEnvironmentUpdate()
        {
            FakeGateway gateway = WithStartCommand();
            gateway.Commands.Add(new UpdateEnvironmentCommand(
                2, DateTimeOffset.UnixEpoch, new EnvironmentSnapshot(90.0, 1.0, 500.0)));

            TrainingSessionCoordinator coordinator = Create(gateway);
            coordinator.StartAsync("trainee-1", "scenario-1").GetAwaiter().GetResult();
            coordinator.PollCommandsAsync().GetAwaiter().GetResult();

            Assert.AreEqual(90.0, coordinator.Session.TargetEnvironment.TemperatureCelsius, 1e-9);
            Assert.AreEqual(2, coordinator.LastCommandSequence);
        }

        [Test]
        public void FinishReturnsResultFromServer()
        {
            FakeGateway gateway = WithStartCommand();
            TrainingSessionCoordinator coordinator = Create(gateway);
            coordinator.StartAsync("trainee-1", "scenario-1").GetAwaiter().GetResult();
            coordinator.Tick(30.0);

            TrainingResult result = coordinator.FinishAsync().GetAwaiter().GetResult();

            Assert.AreEqual(SessionState.Finished, coordinator.State);
            Assert.AreEqual(86.0, result.Combined, 1e-9);
            Assert.AreEqual("хорошо", coordinator.Session.Result.GradeLabel);
            Assert.AreEqual(30.0, coordinator.Session.ElapsedSeconds, 1e-9);
        }

        private static FakeGateway WithStartCommand()
        {
            var gateway = new FakeGateway();
            gateway.Commands.Add(new StartScenarioCommand(
                1,
                DateTimeOffset.UnixEpoch,
                "scenario-1",
                180.0,
                new EnvironmentSnapshot(60.0, 5.0, 120.0)));
            return gateway;
        }

        private static TrainingSessionCoordinator Create(FakeGateway gateway)
        {
            return new TrainingSessionCoordinator(gateway, new FrozenClock(), new SilentLog());
        }

        private sealed class FakeGateway : ITrainingGateway
        {
            public readonly List<ServerCommand> Commands = new List<ServerCommand>();
            public readonly List<TelemetryEvent> SentEvents = new List<TelemetryEvent>();

            public Task<SessionStartInfo> StartSessionAsync(string traineeId, string scenarioId, CancellationToken cancellationToken)
            {
                return Task.FromResult(new SessionStartInfo(
                    "session-1", traineeId, scenarioId, 0, EnvironmentSnapshot.Calm));
            }

            public Task SendEventsAsync(string sessionId, IReadOnlyList<TelemetryEvent> events, CancellationToken cancellationToken)
            {
                SentEvents.AddRange(events);
                return Task.CompletedTask;
            }

            public Task<IReadOnlyList<ServerCommand>> FetchCommandsAsync(
                string sessionId, long afterSequence, CancellationToken cancellationToken)
            {
                IReadOnlyList<ServerCommand> pending = Commands
                    .Where(command => command.Sequence > afterSequence)
                    .OrderBy(command => command.Sequence)
                    .ToList();
                return Task.FromResult(pending);
            }

            public Task<TrainingResult> FinishSessionAsync(string sessionId, CancellationToken cancellationToken)
            {
                return Task.FromResult(new TrainingResult(80.0, 90.0, 86.0, "хорошо"));
            }

            public Task<IReadOnlyList<ScenarioInfo>> GetScenariosAsync(CancellationToken cancellationToken)
            {
                IReadOnlyList<ScenarioInfo> scenarios = new[]
                {
                    new ScenarioInfo("scenario-1", "Возгорание в серверной", 180.0, EnvironmentSnapshot.Calm)
                };
                return Task.FromResult(scenarios);
            }

            public Task<IReadOnlyList<LeaderboardRowInfo>> GetLeaderboardAsync(
                string scenarioId, int limit, CancellationToken cancellationToken)
            {
                return Task.FromResult<IReadOnlyList<LeaderboardRowInfo>>(Array.Empty<LeaderboardRowInfo>());
            }
        }

        private sealed class FrozenClock : IClock
        {
            public DateTimeOffset UtcNow => DateTimeOffset.UnixEpoch;

            public double MonotonicSeconds => 0.0;
        }

        private sealed class SilentLog : ILogSink
        {
            public void Info(string message)
            {
            }

            public void Warning(string message)
            {
            }

            public void Error(string message, Exception exception = null)
            {
            }
        }
    }
}