# FireFighter-Trainer

VR-тренажёр подготовки спасателей: Unity-клиент и серверная часть на Java.
Учебная практика РТУ МИРЭА, кафедра игровой индустрии (ИКБО-30-24).

## Структура репозитория

```
Assets/_Project/            содержимое проекта (только оно попадает в сборку)
  Scripts/Domain/           домен клиента, без UnityEngine (asmdef noEngineReferences)
  Scripts/Application/      координатор сеанса и порты, без UnityEngine
  Scripts/Infrastructure/   адаптеры Unity (UnityWebRequest, Time, Debug)
  Scripts/Presentation/     MonoBehaviour-адаптеры и HUD
  Scripts/Editor/           обслуживание проекта (только редактор)
  Scenes/TrainingScene      рабочая сцена (VR-риг, окружение, интерактивные объекты)
  Prefabs, Models, Materials, Textures, Audio, UI, Data, Resources
  Tests/EditMode/           NUnit-тесты домена и координатора
Assets/ThirdParty/          импортированные шаблон и сэмплы пакетов (XR Interaction Toolkit, XR Hands)
Assets/Settings, XR, XRI, CompositionLayers, TextMesh Pro   конфигурация фреймворков
Server/                     серверная часть: Gradle + Spring Boot, слои DDD
Docs/                       архитектура и UML-диаграммы
Tools/                      обслуживание: аудит ассетов, проверка ядра без Unity
```

## Как запустить

### Сервер

```bash
cd Server
./gradlew :web:bootRun      # http://localhost:8080
./scripts/smoke.sh          # проверка протокола (в другом терминале)
```

### Клиент

1. Открыть папку проекта в Unity 6000.6.7f1.
2. Открыть `Assets/_Project/Scenes/TrainingScene.unity`.
3. На объекте с `GameBootstrapper` указать `serverUrl`, `traineeId`,
   `scenarioId` (или оставить пустым и вызвать `StartSession()` из UI).
4. Play (в редакторе — с XR Device Simulator).

Без запущенного сервера клиент покажет ошибку подключения: сервер —
авторитет, локальной оценки не существует.

## Проверки

| Что | Команда |
|---|---|
| Сервер: сборка и тесты | `cd Server && ./gradlew build` |
| Клиентское ядро без Unity | `dotnet build Tools/ClientCoreCheck/ClientCoreCheck.csproj` |
| Тесты клиента в Unity | Window → General → Test Runner → EditMode |
| Аудит неиспользуемых ассетов | `python3 -I Tools/AssetAudit/prune_unreachable.py` (отчёт; `--apply` — удаление) |
| Диаграммы PNG | `cd Docs/uml && drawio -x -f png -s 2 -o <файл>.png <файл>.drawio` |

## Архитектура

* [`Docs/architecture.md`](Docs/architecture.md) — разделение на клиент и
  сервер, сущности, их данные и поведение, протокол, решения по ООП.
* [`Docs/uml/`](Docs/uml/) — диаграммы классов и компонентов (Draw.io + PNG).
* [`Server/README.md`](Server/README.md) — модули, API, запуск сервера.

Кратко: сервер-авторитет. Клиент собирает телеметрию и отображает команды,
сервер принимает решения и считает рейтинги (алгоритмы 1–4 аналитического
этапа). Общая логика отделена от Unity и Spring на уровне сборки:
`Assets/_Project/Scripts/Domain` и `Assets/_Project/Scripts/Application`
собраны с `noEngineReferences`, а Gradle-модули `domain` и `application`
не имеют Spring в classpath.