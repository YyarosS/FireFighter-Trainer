using System;
using System.Collections.Generic;

namespace Firefighter.Domain
{
    /// <summary>
    /// Буфер событий телеметрии.
    ///
    /// <para>События копятся в памяти и отправляются пакетами, чтобы не
    /// выполнять сетевой запрос на каждое действие. Буфер ограничен по
    /// размеру: при переполнении событие не принимается и вызывающий код
    /// получает <c>false</c> — молчаливая потеря телеметрии исключена.</para>
    /// </summary>
    public sealed class TelemetryBuffer
    {
        private readonly List<TelemetryEvent> _pending = new List<TelemetryEvent>();
        private readonly int _capacity;
        private double _lastAtSeconds = -1.0;

        public TelemetryBuffer(int capacity = 256)
        {
            if (capacity < 1)
            {
                throw new ArgumentOutOfRangeException(nameof(capacity), "Ёмкость буфера должна быть положительной");
            }

            _capacity = capacity;
        }

        public int PendingCount => _pending.Count;

        public int Capacity => _capacity;

        public bool TryRecord(TelemetryEvent telemetryEvent)
        {
            if (telemetryEvent == null)
            {
                throw new ArgumentNullException(nameof(telemetryEvent));
            }

            if (_pending.Count >= _capacity)
            {
                return false;
            }

            if (telemetryEvent.AtSeconds < _lastAtSeconds)
            {
                throw new InvalidOperationException(
                    "Время события не может уменьшаться: журнал телеметрии монотонен");
            }

            _lastAtSeconds = telemetryEvent.AtSeconds;
            _pending.Add(telemetryEvent);
            return true;
        }

        /// <summary>Забрать не более <paramref name="maxCount"/> событий, сохранив порядок.</summary>
        public IReadOnlyList<TelemetryEvent> Drain(int maxCount)
        {
            if (maxCount <= 0)
            {
                return Array.Empty<TelemetryEvent>();
            }

            int count = Math.Min(maxCount, _pending.Count);
            List<TelemetryEvent> batch = _pending.GetRange(0, count);
            _pending.RemoveRange(0, count);
            return batch;
        }

        public void Clear()
        {
            _pending.Clear();
            _lastAtSeconds = -1.0;
        }
    }
}