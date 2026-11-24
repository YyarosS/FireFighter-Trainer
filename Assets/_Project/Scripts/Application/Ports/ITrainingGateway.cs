using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using Firefighter.Domain;

namespace Firefighter.Application.Ports
{
    /// <summary>
    /// Порт взаимодействия с сервером тренировок.
    ///
    /// <para>Прикладной слой не знает ни о UnityWebRequest, ни о формате
    /// JSON: он описывает только операции протокола. Реализация —
    /// <c>UnityTrainingGateway</c> в инфраструктурном слое.</para>
    /// </summary>
    public interface ITrainingGateway
    {
        /// <summary>Запустить сеанс тренировки на сервере.</summary>
        Task<SessionStartInfo> StartSessionAsync(string traineeId, string scenarioId, CancellationToken cancellationToken);

        /// <summary>Отправить пакет событий телеметрии.</summary>
        Task SendEventsAsync(string sessionId, IReadOnlyList<TelemetryEvent> events, CancellationToken cancellationToken);

        /// <summary>Получить команды сервера с номером больше <paramref name="afterSequence"/>.</summary>
        Task<IReadOnlyList<ServerCommand>> FetchCommandsAsync(string sessionId, long afterSequence, CancellationToken cancellationToken);

        /// <summary>Завершить сеанс и получить итоговую оценку.</summary>
        Task<TrainingResult> FinishSessionAsync(string sessionId, CancellationToken cancellationToken);

        /// <summary>Получить каталог сценариев.</summary>
        Task<IReadOnlyList<ScenarioInfo>> GetScenariosAsync(CancellationToken cancellationToken);

        /// <summary>Получить таблицу лидеров по сценарию.</summary>
        Task<IReadOnlyList<LeaderboardRowInfo>> GetLeaderboardAsync(string scenarioId, int limit, CancellationToken cancellationToken);
    }
}