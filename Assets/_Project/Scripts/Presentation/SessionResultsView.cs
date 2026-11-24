using Firefighter.Domain;
using TMPro;
using UnityEngine;

namespace Firefighter.Presentation
{
    /// <summary>Экран итогов тренировки: рейтинги и качественная оценка сервера.</summary>
    public sealed class SessionResultsView : MonoBehaviour
    {
        [SerializeField]
        private GameBootstrapper bootstrapper;

        [SerializeField]
        private GameObject panel;

        [SerializeField]
        private TMP_Text summaryText;

        private void Awake()
        {
            if (bootstrapper == null)
            {
                bootstrapper = FindFirstObjectByType<GameBootstrapper>();
            }

            if (bootstrapper != null)
            {
                bootstrapper.ResultReceived += Show;
            }

            if (panel != null)
            {
                panel.SetActive(false);
            }
        }

        private void OnDestroy()
        {
            if (bootstrapper != null)
            {
                bootstrapper.ResultReceived -= Show;
            }
        }

        private void Show(TrainingResult result)
        {
            if (panel != null)
            {
                panel.SetActive(true);
            }

            if (summaryText != null)
            {
                summaryText.text =
                    $"Скорость: {result.Speed:F1}\n" +
                    $"Точность: {result.Accuracy:F1}\n" +
                    $"Итог: {result.Combined:F1}\n" +
                    $"Оценка: {result.GradeLabel}";
            }
        }
    }
}