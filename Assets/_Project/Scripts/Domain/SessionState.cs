namespace Firefighter.Domain
{
    /// <summary>
    /// Состояние клиентского сеанса связи с сервером.
    ///
    /// <para>Состояние меняется только через <see cref="SessionStateMachine"/>,
    /// поэтому недопустимые переходы (например, отправка телеметрии до
    /// подключения) невозможны по построению.</para>
    /// </summary>
    public enum SessionState
    {
        /// <summary>Сеанс ещё не начат.</summary>
        Idle = 0,

        /// <summary>Выполняется запуск сеанса на сервере.</summary>
        Connecting = 1,

        /// <summary>Сеанс идёт: телеметрия принимается и отправляется.</summary>
        Running = 2,

        /// <summary>Отправлен запрос на завершение, ожидается оценка.</summary>
        Finishing = 3,

        /// <summary>Сеанс завершён, результат получен.</summary>
        Finished = 4,

        /// <summary>Сеанс прерван ошибкой.</summary>
        Failed = 5
    }
}