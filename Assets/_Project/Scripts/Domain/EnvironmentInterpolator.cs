using System;

namespace Firefighter.Domain
{
    /// <summary>
    /// Сглаживание изменений среды.
    ///
    /// <para>Сервер присылает новое состояние дискретно, а в VR резкая смена
    /// тумана или освещения воспринимается как рывок. Интерполятор
    /// экспоненциально приближает отображаемое состояние к целевому —
    /// это чистая математика без обращений к Unity.</para>
    /// </summary>
    public sealed class EnvironmentInterpolator
    {
        private readonly double _smoothingSeconds;

        public EnvironmentInterpolator(double smoothingSeconds = 1.5)
        {
            if (double.IsNaN(smoothingSeconds) || smoothingSeconds <= 0.0)
            {
                throw new ArgumentOutOfRangeException(nameof(smoothingSeconds), "Время сглаживания должно быть положительным");
            }

            _smoothingSeconds = smoothingSeconds;
        }

        public double SmoothingSeconds => _smoothingSeconds;

        /// <summary>Один шаг сглаживания за <paramref name="deltaSeconds"/>.</summary>
        public EnvironmentSnapshot Step(EnvironmentSnapshot current, EnvironmentSnapshot target, double deltaSeconds)
        {
            if (current == null)
            {
                throw new ArgumentNullException(nameof(current));
            }

            if (target == null)
            {
                throw new ArgumentNullException(nameof(target));
            }

            if (deltaSeconds <= 0.0)
            {
                return current;
            }

            double alpha = 1.0 - Math.Exp(-deltaSeconds / _smoothingSeconds);
            return current.Toward(target, alpha);
        }
    }
}