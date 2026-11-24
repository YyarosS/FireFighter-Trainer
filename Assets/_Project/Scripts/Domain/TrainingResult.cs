using System;

namespace Firefighter.Domain
{
    /// <summary>
    /// Итоговый результат тренировки, полученный от сервера.
    ///
    /// <para>Клиент не вычисляет рейтинги сам, поэтому подмена результата
    /// на стороне клиента невозможна.</para>
    /// </summary>
    public sealed class TrainingResult
    {
        public TrainingResult(double speed, double accuracy, double combined, string gradeLabel)
        {
            RequireRating(speed, nameof(speed));
            RequireRating(accuracy, nameof(accuracy));
            RequireRating(combined, nameof(combined));

            Speed = speed;
            Accuracy = accuracy;
            Combined = combined;
            GradeLabel = gradeLabel ?? string.Empty;
        }

        public double Speed { get; }

        public double Accuracy { get; }

        public double Combined { get; }

        /// <summary>Качественная оценка, рассчитанная сервером (например, «хорошо»).</summary>
        public string GradeLabel { get; }

        private static void RequireRating(double value, string field)
        {
            if (double.IsNaN(value) || value < 0.0 || value > 100.0)
            {
                throw new ArgumentOutOfRangeException(field, "Рейтинг должен принадлежать отрезку [0; 100]");
            }
        }
    }
}