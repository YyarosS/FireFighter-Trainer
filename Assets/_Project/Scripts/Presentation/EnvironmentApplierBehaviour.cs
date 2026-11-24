using Firefighter.Domain;
using UnityEngine;

namespace Firefighter.Presentation
{
    /// <summary>
    /// Отображение среды: туман по видимости, яркость света и дым по
    /// концентрации токсинов.
    ///
    /// <para>Значения берутся из сглаженного состояния, которое уже
    /// интерполировано доменным слоем. Компонент — чистый адаптер:
    /// никаких решений о том, какой должна быть среда, он не принимает.</para>
    /// </summary>
    public sealed class EnvironmentApplierBehaviour : MonoBehaviour
    {
        [SerializeField]
        private GameBootstrapper bootstrapper;

        [SerializeField]
        [Tooltip("Направленный свет сцены (необязательно)")]
        private Light directionalLight;

        [SerializeField]
        [Tooltip("Система частиц дыма (необязательно)")]
        private ParticleSystem smoke;

        [SerializeField]
        [Tooltip("Плотность тумана при нулевой видимости")]
        private float maxFogDensity = 0.12f;

        [SerializeField]
        [Tooltip("Видимость, при которой туман считается отсутствующим, м")]
        private float clearVisibilityMeters = 100f;

        [SerializeField]
        [Tooltip("Концентрация токсинов, при которой дым работает на полную мощность, ppm")]
        private float maxToxinPpm = 500f;

        private void Awake()
        {
            if (bootstrapper == null)
            {
                bootstrapper = FindFirstObjectByType<GameBootstrapper>();
            }
        }

        private void Update()
        {
            EnvironmentSnapshot environment = bootstrapper?.Coordinator?.Session.CurrentEnvironment;
            if (environment == null)
            {
                return;
            }

            // Доменные значения хранятся в double, Unity работает с float —
            // приведение выполняется только на границе адаптера.
            float visibility = (float)environment.VisibilityMeters;
            float haziness = Mathf.Clamp01(1f - visibility / clearVisibilityMeters);
            RenderSettings.fog = haziness > 0.01f;
            RenderSettings.fogDensity = Mathf.Lerp(0f, maxFogDensity, haziness);

            if (directionalLight != null)
            {
                directionalLight.intensity = Mathf.Lerp(1.2f, 0.3f, haziness);
            }

            if (smoke != null)
            {
                float toxins = (float)environment.ToxinConcentrationPpm;
                float intensity = Mathf.Clamp01(toxins / maxToxinPpm);
                ParticleSystem.EmissionModule emission = smoke.emission;
                emission.rateOverTime = Mathf.Lerp(0f, 40f, intensity);
            }
        }
    }
}