using Firefighter.Domain;
using TMPro;
using UnityEngine;

namespace Firefighter.Presentation
{
    /// <summary>
    /// HUD тренировки: состояние сеанса, таймер и подсказка сервера.
    ///
    /// <para>Компонент только читает состояние координатора и не содержит
    /// игровой логики — он не может повлиять на оценку.</para>
    /// </summary>
    public sealed class TrainingHudController : MonoBehaviour
    {
        [SerializeField]
        private GameBootstrapper bootstrapper;

        [SerializeField]
        private TMP_Text statusText;

        [SerializeField]
        private TMP_Text timerText;

        [SerializeField]
        private TMP_Text hintText;

        private void Awake()
        {
            if (bootstrapper == null)
            {
                bootstrapper = FindFirstObjectByType<GameBootstrapper>();
            }
        }

        private void Update()
        {
            if (bootstrapper == null || bootstrapper.Coordinator == null)
            {
                return;
            }

            if (statusText != null)
            {
                statusText.text = StatusLabel(bootstrapper.Coordinator.State);
            }

            if (timerText != null)
            {
                TrainingSessionState session = bootstrapper.Coordinator.Session;
                timerText.text = $"{session.ElapsedSeconds:F0} / {session.TimeLimitSeconds:F0} с";
            }

            if (hintText != null)
            {
                hintText.text = bootstrapper.Coordinator.Session.LastHint;
            }
        }

        private static string StatusLabel(SessionState state)
        {
            switch (state)
            {
                case SessionState.Idle: return "Готов к тренировке";
                case SessionState.Connecting: return "Подключение…";
                case SessionState.Running: return "Тренировка идёт";
                case SessionState.Finishing: return "Расчёт оценки…";
                case SessionState.Finished: return "Тренировка завершена";
                case SessionState.Failed: return "Ошибка сеанса";
                default: return state.ToString();
            }
        }
    }
}