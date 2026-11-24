using System;

namespace Firefighter.Domain
{
    /// <summary>Строка таблицы лидеров, полученная от сервера.</summary>
    public sealed class LeaderboardRowInfo
    {
        public LeaderboardRowInfo(
            int place,
            string traineeName,
            double speed,
            double accuracy,
            double combined,
            double elapsedSeconds)
        {
            if (place < 1)
            {
                throw new ArgumentOutOfRangeException(nameof(place), "Место начинается с 1");
            }

            Place = place;
            TraineeName = traineeName ?? string.Empty;
            Speed = speed;
            Accuracy = accuracy;
            Combined = combined;
            ElapsedSeconds = elapsedSeconds;
        }

        public int Place { get; }

        public string TraineeName { get; }

        public double Speed { get; }

        public double Accuracy { get; }

        public double Combined { get; }

        public double ElapsedSeconds { get; }
    }
}