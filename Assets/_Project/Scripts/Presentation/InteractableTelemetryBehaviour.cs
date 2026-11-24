using Firefighter.Domain;
using EventType = Firefighter.Domain.EventType;
using UnityEngine;

namespace Firefighter.Presentation
{
    /// <summary>
    /// Отправка события телеметрии из сцены.
    ///
    /// <para>Компонент не зависит от XR Interaction Toolkit: его методы
    /// вызываются из UnityEvent интерактивных объектов (огнетушитель,
    /// дверь, пострадавший). Поэтому замена контроллеров или рук в XR
    /// не затрагивает телеметрию.</para>
    /// </summary>
    public sealed class InteractableTelemetryBehaviour : MonoBehaviour
    {
        [SerializeField]
        private GameBootstrapper bootstrapper;

        [SerializeField]
        [Tooltip("Имя объекта в журнале событий, например «огнетушитель»")]
        private string targetObject = "interactable";

        private void Awake()
        {
            if (bootstrapper == null)
            {
                bootstrapper = FindFirstObjectByType<GameBootstrapper>();
            }
        }

        /// <summary>Корректное действие стажёра.</summary>
        public void ReportAction()
        {
            Report(EventType.Action);
        }

        /// <summary>Нарушение техники безопасности.</summary>
        public void ReportSafetyViolation()
        {
            Report(EventType.SafetyViolation);
        }

        /// <summary>Использовано неверное оборудование.</summary>
        public void ReportWrongEquipment()
        {
            Report(EventType.WrongEquipment);
        }

        /// <summary>Пропущен обязательный этап.</summary>
        public void ReportMissedStage()
        {
            Report(EventType.MissedStage);
        }

        private void Report(EventType type)
        {
            if (bootstrapper == null)
            {
                Debug.LogWarning("[Firefighter] GameBootstrapper не найден, событие не отправлено");
                return;
            }

            bootstrapper.RecordEvent(type, targetObject);
        }
    }
}