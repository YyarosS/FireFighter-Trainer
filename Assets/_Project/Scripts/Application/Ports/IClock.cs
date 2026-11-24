using System;

namespace Firefighter.Application.Ports
{
    /// <summary>
    /// Порт источника времени.
    ///
    /// <para><see cref="MonotonicSeconds"/> не зависит от системных часов
    /// и не прыгает при их переводе — именно по нему считается время
    /// события телеметрии.</para>
    /// </summary>
    public interface IClock
    {
        /// <summary>Текущее время UTC (для журналов).</summary>
        DateTimeOffset UtcNow { get; }

        /// <summary>Монотонное время в секундах (для длительности тренировки).</summary>
        double MonotonicSeconds { get; }
    }
}