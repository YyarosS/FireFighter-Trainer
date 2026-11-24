using System;

namespace Firefighter.Domain
{
    /// <summary>
    /// Команда сервера VR-клиенту (архитектура «сервер-авторитет»).
    ///
    /// <para>Иерархия классов вместо строкового «типа команды»: каждая
    /// команда сама знает, как применить себя к состоянию сеанса
    /// (<see cref="ApplyTo"/>). Добавление новой команды — новый класс,
    /// а не правка разбора строк во всех местах.</para>
    /// </summary>
    public abstract class ServerCommand
    {
        protected ServerCommand(long sequence, DateTimeOffset issuedAt)
        {
            if (sequence < 1)
            {
                throw new ArgumentOutOfRangeException(nameof(sequence), "Номер команды начинается с 1");
            }

            Sequence = sequence;
            IssuedAt = issuedAt;
        }

        /// <summary>Порядковый номер команды внутри сеанса.</summary>
        public long Sequence { get; }

        /// <summary>Момент выдачи команды сервером.</summary>
        public DateTimeOffset IssuedAt { get; }

        /// <summary>Строковое имя типа команды (совпадает с контрактом сервера).</summary>
        public abstract string Type { get; }

        /// <summary>Применить команду к состоянию сеанса.</summary>
        public abstract void ApplyTo(TrainingSessionState state);
    }
}