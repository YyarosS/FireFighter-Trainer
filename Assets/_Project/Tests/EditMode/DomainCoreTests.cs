using System;
using Firefighter.Domain;
using NUnit.Framework;

namespace Firefighter.Tests
{
    /// <summary>
    /// Проверки доменного слоя клиента. Тесты не требуют Unity:
    /// весь домен собирается и проверяется как обычная библиотека классов.
    /// </summary>
    public sealed class DomainCoreTests
    {
        [Test]
        public void StateMachineRejectsInvalidTransition()
        {
            var machine = new SessionStateMachine();

            Assert.Throws<InvalidOperationException>(() => machine.MarkRunning());
            Assert.AreEqual(SessionState.Idle, machine.State);
        }

        [Test]
        public void StateMachineAllowsFullCycle()
        {
            var machine = new SessionStateMachine();

            machine.BeginConnecting();
            machine.MarkRunning();
            machine.BeginFinishing();
            machine.MarkFinished();

            Assert.AreEqual(SessionState.Finished, machine.State);
        }

        [Test]
        public void BufferRejectsEventsWhenFull()
        {
            var buffer = new TelemetryBuffer(1);

            Assert.IsTrue(buffer.TryRecord(new TelemetryEvent(EventType.Action, "door", 1.0)));
            Assert.IsFalse(buffer.TryRecord(new TelemetryEvent(EventType.Action, "door", 2.0)));
            Assert.AreEqual(1, buffer.PendingCount);
        }

        [Test]
        public void BufferRejectsDecreasingTime()
        {
            var buffer = new TelemetryBuffer(8);
            buffer.TryRecord(new TelemetryEvent(EventType.Action, "door", 5.0));

            Assert.Throws<InvalidOperationException>(
                () => buffer.TryRecord(new TelemetryEvent(EventType.Action, "door", 4.0)));
        }

        [Test]
        public void DrainKeepsOrderWithinBatch()
        {
            var buffer = new TelemetryBuffer(8);
            buffer.TryRecord(new TelemetryEvent(EventType.Action, "первый", 1.0));
            buffer.TryRecord(new TelemetryEvent(EventType.Action, "второй", 2.0));
            buffer.TryRecord(new TelemetryEvent(EventType.Action, "третий", 3.0));

            var batch = buffer.Drain(2);

            Assert.AreEqual(2, batch.Count);
            Assert.AreEqual("первый", batch[0].TargetObject);
            Assert.AreEqual("второй", batch[1].TargetObject);
            Assert.AreEqual(1, buffer.PendingCount);
        }

        [Test]
        public void InterpolatorMovesTowardTarget()
        {
            var interpolator = new EnvironmentInterpolator(1.0);
            var current = new EnvironmentSnapshot(20.0, 100.0, 0.0);
            var target = new EnvironmentSnapshot(80.0, 10.0, 500.0);

            EnvironmentSnapshot next = interpolator.Step(current, target, 0.5);

            Assert.Greater(next.TemperatureCelsius, 20.0);
            Assert.Less(next.TemperatureCelsius, 80.0);
            Assert.Greater(next.VisibilityMeters, 10.0);
            Assert.Less(next.VisibilityMeters, 100.0);
        }

        [Test]
        public void EndSessionCommandCompletesSessionState()
        {
            var state = new TrainingSessionState();
            state.Begin("scenario-1", 180.0, EnvironmentSnapshot.Calm);

            new EndSessionCommand(4, DateTimeOffset.UnixEpoch, 80.0, 90.0, 86.0).ApplyTo(state);

            Assert.IsTrue(state.IsCompleted);
            Assert.AreEqual(86.0, state.Result.Combined, 1e-9);
        }

        [Test]
        public void EnvironmentCommandChangesTargetOnly()
        {
            var state = new TrainingSessionState();
            state.Begin("scenario-1", 180.0, EnvironmentSnapshot.Calm);

            new UpdateEnvironmentCommand(2, DateTimeOffset.UnixEpoch, new EnvironmentSnapshot(90.0, 1.0, 500.0))
                .ApplyTo(state);

            Assert.AreEqual(90.0, state.TargetEnvironment.TemperatureCelsius, 1e-9);
            Assert.AreEqual(20.0, state.CurrentEnvironment.TemperatureCelsius, 1e-9);
        }
    }
}