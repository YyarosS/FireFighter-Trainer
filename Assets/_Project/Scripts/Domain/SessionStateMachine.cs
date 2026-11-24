using System;
using System.Collections.Generic;

namespace Firefighter.Domain
{
    /// <summary>
    /// Конечный автомат клиентского сеанса.
    ///
    /// <para>Класс не зависит от Unity: его можно проверить обычными
    /// модульными тестами. Все допустимые переходы описаны в одном месте,
    /// поэтому «зависшее» состояние невозможно получить случайно.</para>
    /// </summary>
    public sealed class SessionStateMachine
    {
        private static readonly Dictionary<SessionState, SessionState[]> Allowed =
            new Dictionary<SessionState, SessionState[]>
            {
                { SessionState.Idle, new[] { SessionState.Connecting } },
                { SessionState.Connecting, new[] { SessionState.Running, SessionState.Failed } },
                { SessionState.Running, new[] { SessionState.Finishing, SessionState.Failed } },
                { SessionState.Finishing, new[] { SessionState.Finished, SessionState.Failed } },
                { SessionState.Finished, new SessionState[0] },
                { SessionState.Failed, new[] { SessionState.Connecting } }
            };

        /// <summary>Текущее состояние.</summary>
        public SessionState State { get; private set; } = SessionState.Idle;

        /// <summary>Причина последней ошибки, если сеанс завершился неудачей.</summary>
        public string FailureReason { get; private set; } = string.Empty;

        /// <summary>Можно ли принимать события телеметрии в текущем состоянии.</summary>
        public bool CanAcceptTelemetry => State == SessionState.Running;

        /// <summary>Ожидается ли ответ сервера (запросы не дублируются).</summary>
        public bool IsBusy => State == SessionState.Connecting || State == SessionState.Finishing;

        public void BeginConnecting()
        {
            TransitionTo(SessionState.Connecting);
            FailureReason = string.Empty;
        }

        public void MarkRunning()
        {
            TransitionTo(SessionState.Running);
        }

        public void BeginFinishing()
        {
            TransitionTo(SessionState.Finishing);
        }

        public void MarkFinished()
        {
            TransitionTo(SessionState.Finished);
        }

        public void MarkFailed(string reason)
        {
            if (State == SessionState.Finished)
            {
                throw new InvalidOperationException("Завершённый сеанс нельзя перевести в состояние ошибки");
            }

            FailureReason = reason ?? string.Empty;
            TransitionTo(SessionState.Failed);
        }

        private void TransitionTo(SessionState next)
        {
            if (Array.IndexOf(Allowed[State], next) < 0)
            {
                throw new InvalidOperationException(
                    $"Недопустимый переход состояния: {State} → {next}");
            }

            State = next;
        }
    }
}