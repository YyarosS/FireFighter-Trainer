using System;

namespace Firefighter.Domain
{
    /// <summary>
    /// Состояние моделируемой среды: температура, видимость и концентрация
    /// токсинов. Неизменяемый объект-значение с теми же границами, что и
    /// на сервере, — некорректное состояние среды нельзя даже создать.
    /// </summary>
    public sealed class EnvironmentSnapshot
    {
        public static readonly EnvironmentSnapshot Calm = new EnvironmentSnapshot(20.0, 100.0, 0.0);

        public EnvironmentSnapshot(double temperatureCelsius, double visibilityMeters, double toxinConcentrationPpm)
        {
            RequireRange(temperatureCelsius, -50.0, 150.0, "Температура, °C");
            RequireRange(visibilityMeters, 0.0, 1000.0, "Видимость, м");
            RequireRange(toxinConcentrationPpm, 0.0, 10000.0, "Концентрация токсинов, ppm");

            TemperatureCelsius = temperatureCelsius;
            VisibilityMeters = visibilityMeters;
            ToxinConcentrationPpm = toxinConcentrationPpm;
        }

        public double TemperatureCelsius { get; }

        public double VisibilityMeters { get; }

        public double ToxinConcentrationPpm { get; }

        /// <summary>
        /// Промежуточное состояние между текущим и целевым.
        /// <paramref name="alpha"/> = 0 — текущее, 1 — целевое.
        /// </summary>
        public EnvironmentSnapshot Toward(EnvironmentSnapshot target, double alpha)
        {
            if (target == null)
            {
                throw new ArgumentNullException(nameof(target));
            }

            double t = Clamp(alpha, 0.0, 1.0);
            return new EnvironmentSnapshot(
                Lerp(TemperatureCelsius, target.TemperatureCelsius, t),
                Lerp(VisibilityMeters, target.VisibilityMeters, t),
                Lerp(ToxinConcentrationPpm, target.ToxinConcentrationPpm, t));
        }

        private static double Lerp(double from, double to, double t)
        {
            return from + (to - from) * t;
        }

        private static double Clamp(double value, double min, double max)
        {
            if (value < min)
            {
                return min;
            }

            return value > max ? max : value;
        }

        private static void RequireRange(double value, double min, double max, string field)
        {
            if (double.IsNaN(value) || value < min || value > max)
            {
                throw new ArgumentOutOfRangeException(
                    field, $"{field} должна быть в диапазоне [{min}; {max}], получено: {value}");
            }
        }
    }
}