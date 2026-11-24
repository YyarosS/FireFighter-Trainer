using System;
using Firefighter.Application.Ports;
using UnityEngine;

namespace Firefighter.Infrastructure
{
    /// <summary>
    /// Часы на основе Unity: монотонное время кадра — для телеметрии,
    /// системное UTC — для журналов.
    ///
    /// <para><see cref="Time.unscaledTimeAsDouble"/> не зависит от
    /// <c>Time.timeScale</c>, поэтому пауза или замедление игры не искажают
    /// время события.</para>
    /// </summary>
    public sealed class UnityClock : IClock
    {
        public DateTimeOffset UtcNow => DateTimeOffset.UtcNow;

        public double MonotonicSeconds => Time.unscaledTimeAsDouble;
    }
}