# Серверная часть тренажёра (Firefighter Server)

Java 21, Gradle 9.9, Spring Boot 4.2. Многомодульный проект: слои DDD
разделены модулями сборки, а не соглашением.

## Модули

| Модуль | Назначение | Зависимости |
|---|---|---|
| `domain` | сущности, объекты-значения, политики оценки (алгоритмы 1–4), команды, порты репозиториев | нет |
| `application` | сценарии использования, DTO, порты `Clock` и `IdGenerator` | `domain` |
| `infrastructure` | in-memory репозитории, системные часы, UUID, `DomainConfig` | `application`, Spring Context |
| `web` | REST-контроллеры, обработка ошибок, `FirefighterServerApplication` | `application`, `infrastructure`, Spring Boot Web MVC |

**Правило зависимостей:** Spring отсутствует в classpath модулей
`domain` и `application`. Импорт фреймворка там не скомпилируется —
граница проверяется сборкой, а не дисциплиной.

## Сборка и тесты

```bash
cd Server
./gradlew build          # компиляция всех модулей + тесты
./gradlew :domain:test   # только тесты домена
```

Тесты: JUnit 5 + AssertJ, без Mockito (используются ручные фейки).
Домен проверяет те же шесть сценариев, что процедурная реализация
этапа 3, — совпадение результатов подтверждает эквивалентность.

## Запуск

```bash
./gradlew :web:bootRun
# сервер на http://localhost:8080
```

Дымовой тест всего протокола (нужен запущенный сервер):

```bash
./scripts/smoke.sh
```

Ожидаемые числа: `speed ≈ 27.22`, `accuracy = 100`, `combined ≈ 70.89`
для Δt = 131 с и норматива 180 с.

## REST API

| Метод | Путь | Назначение |
|---|---|---|
| POST | `/api/trainees` | регистрация стажёра |
| GET | `/api/trainees/{id}/sessions` | история тренировок |
| POST | `/api/scenarios` | создание сценария |
| GET | `/api/scenarios` | каталог сценариев |
| POST | `/api/sessions` | старт тренировки |
| POST | `/api/sessions/{id}/events` | пакет событий телеметрии |
| POST | `/api/sessions/{id}/finish` | завершение и оценка |
| GET | `/api/sessions/{id}` | состояние сеанса |
| GET | `/api/sessions/{id}/commands?after=N` | команды после номера N |
| POST | `/api/sessions/{id}/environment` | изменение среды |
| GET | `/api/leaderboard?scenarioId=…&limit=…` | таблица лидеров |

Ошибки — Problem Details (RFC 9457): 404, 409, 400.

## Дальнейшие этапы

* этап 5 — физическая модель PostgreSQL: JPA-реализации портов заменяют
  in-memory адаптеры, домен и приложение не меняются;
* аутентификация инструктора и стажёра;
* вынос оценки в фоновую обработку при росте нагрузки.

Полное описание модели и протокола — в [`../Docs/architecture.md`](../Docs/architecture.md).