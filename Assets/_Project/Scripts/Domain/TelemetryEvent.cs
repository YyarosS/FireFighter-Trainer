using System;

namespace Firefighter.Domain
{
    /// <summary>
    /// Событие телеметрии: факт действия стажёра, который клиент отправляет
    /// на сервер. Клиент не решает, правильно ли действие, — он лишь
    /// фиксирует произошедшее (архитектура «сервер-авторитет»).
    /// </summary>
    public sealed class TelemetryEvent
    {
        public TelemetryEvent(EventType type, string targetObject, double atSeconds)
        {
            if (string.IsNullOrWhiteSpace(targetObject))
            {
                throw new ArgumentException("Объект события не может быть пустым", nameof(targetObject));
            }

            if (double.IsNaN(atSeconds) || atSeconds < 0.0)
            {
                throw new ArgumentOutOfRangeException(nameof(atSeconds), "Время события не может быть отрицательным");
            }

            Type = type;
            TargetObject = targetObject;
            AtSeconds = atSeconds;
        }

        public EventType Type { get; }

        public string TargetObject { get; }

        public double AtSeconds { get; }
    }
}