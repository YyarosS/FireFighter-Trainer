using System;
using System.IO;
using System.Linq;
using System.Text;
using UnityEditor;
using UnityEditor.SceneManagement;
using UnityEngine;
using UnityEngine.SceneManagement;

namespace Firefighter.Editor
{
    /// <summary>
    /// Служебные операции обслуживания проекта: выполняются из меню
    /// «Firefighter → Обслуживание» или в batchmode через
    /// <c>-executeMethod Firefighter.Editor.ProjectMaintenance.Метод</c>.
    ///
    /// <para>Скрипт находится в сборке с <c>includePlatforms: Editor</c>,
    /// поэтому в сборку приложения не попадает.</para>
    /// </summary>
    public static class ProjectMaintenance
    {
        private const string ScenePath = "Assets/_Project/Scenes/TrainingScene.unity";
        private const string TemplateSettingsFolder = "Assets/Settings/Project Configuration";

        /// <summary>
        /// Демонстрационные объекты шаблона VR, которые не используются
        /// в тренажёре: обучающие карточки, видеоплеер, бластер и тотемы.
        /// </summary>
        private static readonly string[] DemoSceneObjects =
        {
            "CoachingCardRoot",
            "Tutorial Player",
            "Affordance Callout",
            "Affordance Callouts Left",
            "Affordance Callouts Right",
            "Blaster",
            "Totem1",
            "Totem2 Variant"
        };

        /// <summary>
        /// Полная подготовка сцены к работе: удаление демонстрационных
        /// объектов и шаблонов сцен. Точка входа для batchmode.
        /// </summary>
        public static void PrepareTrainingScene()
        {
            RemoveDemoObjects();
            DeleteSceneTemplates();
        }

        [MenuItem("Firefighter/Обслуживание/1. Выгрузить иерархию сцены")]
        public static void DumpHierarchy()
        {
            Scene scene = EditorSceneManager.OpenScene(ScenePath, OpenSceneMode.Single);
            var builder = new StringBuilder();
            builder.AppendLine("Сцена: " + ScenePath);
            builder.AppendLine("Корневых объектов: " + scene.rootCount);
            builder.AppendLine();

            foreach (GameObject root in scene.GetRootGameObjects())
            {
                WriteTransform(builder, root.transform, 0);
            }

            string path = Path.Combine(Directory.GetCurrentDirectory(), "Logs", "scene_hierarchy.txt");
            Directory.CreateDirectory(Path.GetDirectoryName(path));
            File.WriteAllText(path, builder.ToString(), Encoding.UTF8);
            Debug.Log("[ProjectMaintenance] Иерархия записана: " + path);
        }

        [MenuItem("Firefighter/Обслуживание/2. Удалить демонстрационные объекты")]
        public static void RemoveDemoObjects()
        {
            Scene scene = EditorSceneManager.OpenScene(ScenePath, OpenSceneMode.Single);
            int removed = 0;

            foreach (GameObject root in scene.GetRootGameObjects().ToArray())
            {
                if (!IsDemoObject(root.name))
                {
                    continue;
                }

                UnityEngine.Object.DestroyImmediate(root);
                removed++;
            }

            foreach (GameObject root in scene.GetRootGameObjects())
            {
                foreach (Transform child in root.GetComponentsInChildren<Transform>(true).ToArray())
                {
                    if (child == null || !IsDemoObject(child.name))
                    {
                        continue;
                    }

                    UnityEngine.Object.DestroyImmediate(child.gameObject);
                    removed++;
                }
            }

            EditorSceneManager.MarkSceneDirty(scene);
            EditorSceneManager.SaveScene(scene);
            AssetDatabase.SaveAssets();
            Debug.Log("[ProjectMaintenance] Удалено демонстрационных объектов: " + removed);
        }

        [MenuItem("Firefighter/Обслуживание/4. Удалить шаблоны сцен")]
        public static void DeleteSceneTemplates()
        {
            if (!Directory.Exists(TemplateSettingsFolder))
            {
                return;
            }

            string[] templates = Directory.GetFiles(TemplateSettingsFolder, "*.scenetemplate");
            foreach (string template in templates)
            {
                AssetDatabase.DeleteAsset(template.Replace('\\', '/'));
            }

            AssetDatabase.Refresh();
            Debug.Log("[ProjectMaintenance] Удалено шаблонов сцен: " + templates.Length);
        }

        [MenuItem("Firefighter/Обслуживание/5. Проверить сцену")]
        public static void ValidateScene()
        {
            Scene scene = EditorSceneManager.OpenScene(ScenePath, OpenSceneMode.Single);
            var report = new StringBuilder();
            int objects = 0;
            int missingScripts = 0;
            int missingPrefabs = 0;
            int nullMaterials = 0;

            foreach (GameObject root in scene.GetRootGameObjects())
            {
                foreach (Transform transform in root.GetComponentsInChildren<Transform>(true))
                {
                    objects++;

                    foreach (Component component in transform.GetComponents<Component>())
                    {
                        if (component != null)
                        {
                            continue;
                        }

                        missingScripts++;
                        report.AppendLine("MISSING SCRIPT: " + ObjectPath(transform));
                    }

                    if (PrefabUtility.GetPrefabInstanceStatus(transform.gameObject) == PrefabInstanceStatus.MissingAsset)
                    {
                        missingPrefabs++;
                        report.AppendLine("MISSING PREFAB: " + ObjectPath(transform));
                    }

                    var renderer = transform.GetComponent<Renderer>();
                    if (renderer == null)
                    {
                        continue;
                    }

                    foreach (Material material in renderer.sharedMaterials)
                    {
                        if (material == null)
                        {
                            nullMaterials++;
                            report.AppendLine("NULL MATERIAL: " + ObjectPath(transform));
                        }
                    }
                }
            }

            report.Insert(0,
                $"Объектов: {objects}; отсутствующих скриптов: {missingScripts}; " +
                $"отсутствующих префабов: {missingPrefabs}; пустых материалов: {nullMaterials}{Environment.NewLine}");

            string path = Path.Combine(Directory.GetCurrentDirectory(), "Logs", "scene_validation.txt");
            Directory.CreateDirectory(Path.GetDirectoryName(path));
            File.WriteAllText(path, report.ToString(), Encoding.UTF8);
            Debug.Log("[ProjectMaintenance] Проверка сцены завершена: " + path);
        }

        private static string ObjectPath(Transform transform)
        {
            var builder = new StringBuilder(transform.name);
            Transform current = transform.parent;
            while (current != null)
            {
                builder.Insert(0, current.name + "/");
                current = current.parent;
            }

            return builder.ToString();
        }

        private static bool IsDemoObject(string objectName)
        {
            return DemoSceneObjects.Any(demo => string.Equals(demo, objectName, StringComparison.Ordinal));
        }

        private static void WriteTransform(StringBuilder builder, Transform transform, int depth)
        {
            string indent = new string(' ', depth * 2);
            string components = string.Join(", ", transform.GetComponents<Component>()
                .Select(component => component == null ? "MISSING SCRIPT" : component.GetType().Name));

            builder.AppendLine($"{indent}{transform.name} [{components}]");

            for (int index = 0; index < transform.childCount; index++)
            {
                WriteTransform(builder, transform.GetChild(index), depth + 1);
            }
        }
    }
}