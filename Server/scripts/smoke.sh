#!/usr/bin/env bash
#
# Дымовой тест REST API серверной части.
#
# Поднимает сценарий целиком: регистрация стажёра → создание сценария →
# старт тренировки → пакет событий → завершение → таблица лидеров.
# Числа должны совпасть с процедурной реализацией (V ≈ 27.22, A = 100,
# R ≈ 70.89 при Δt = 131 с и нормативе 180 с).
#
# Использование:
#   ./gradlew :web:bootRun        # в отдельном терминале
#   ./scripts/smoke.sh [BASE_URL]

set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"

json_field() {
  python3 -c "import json,sys; data=json.load(sys.stdin); print(data$1)"
}

echo "== Сервер: $BASE_URL"

trainee_id=$(curl -sS -X POST "$BASE_URL/api/trainees" \
  -H 'Content-Type: application/json' \
  -d '{"fullName":"Иванов И.И.","studyGroup":"ИКБО-30-24","vrDeviceId":"Quest-3-01"}' \
  | json_field "['id']")
echo "стажёр:      $trainee_id"

scenario_id=$(curl -sS -X POST "$BASE_URL/api/scenarios" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Возгорание в серверной","description":"Дым в стойке","temperatureCelsius":60,"visibilityMeters":5,"toxinConcentrationPpm":120,"timeLimitSeconds":180}' \
  | json_field "['id']")
echo "сценарий:    $scenario_id"

session_id=$(curl -sS -X POST "$BASE_URL/api/sessions" \
  -H 'Content-Type: application/json' \
  -d "{\"traineeId\":\"$trainee_id\",\"scenarioId\":\"$scenario_id\"}" \
  | json_field "['sessionId']")
echo "тренировка:  $session_id"

echo "команды:     $(curl -sS "$BASE_URL/api/sessions/$session_id/commands?after=0")"

curl -sS -o /dev/null -w "события:     HTTP %{http_code}\n" -X POST "$BASE_URL/api/sessions/$session_id/events" \
  -H 'Content-Type: application/json' \
  -d '{"events":[
        {"type":"ACTION","targetObject":"extinguisher","atSeconds":21.5},
        {"type":"ACTION","targetObject":"door","atSeconds":48.0},
        {"type":"ACTION","targetObject":"hose","atSeconds":96.4},
        {"type":"ACTION","targetObject":"victim","atSeconds":131.0}
      ]}'

echo "результат:   $(curl -sS -X POST "$BASE_URL/api/sessions/$session_id/finish")"
echo "лидеры:      $(curl -sS "$BASE_URL/api/leaderboard?scenarioId=$scenario_id&limit=3")"

echo "== Готово"