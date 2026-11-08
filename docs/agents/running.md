# Running the project (agent notes)

Supplements `README.md`. Prereqs: Docker running, Java 25, `flyway` and `psql` CLIs on PATH.
Run all commands from the project root. Each shell call needs `source ./set-env` first (except tests).

## Database + Flyway
```
source ./set-env
docker compose up -d --wait
until pg_isready -h localhost -p $POSTGRES_PORT -q; do sleep 1; done   # no healthcheck in compose.yaml: "Healthy" only means container started
flyway clean migrate                                                   # expect "Successfully applied 1 migration" (or more)
docker compose down                                                    # stop
```

## Application
```
source ./set-env
(./gradlew bootRun > /tmp/bootrun.log 2>&1 &)
until [ "$(curl -s -o /dev/null -w '%{http_code}' localhost:8080)" = 404 ]; do sleep 2; done   # ~10 s; cap at ~3 min
kill $(lsof -ti tcp:8080 -sTCP:LISTEN); sleep 5                                               # stop
```
- Starts the DB itself via docker compose if not running, and stops it again on shutdown.
- No HTTP endpoints exist, so `404` means ready (also log line `Started PocPartyGpmApplicationKt`).
- After `kill`, Gradle reports `BUILD FAILED`; that is expected.

## Tests
`./gradlew clean test` (Testcontainers, no `set-env` or running DB needed).
Results: one XML per test class in `build/test-results/test/`; sum the `tests`/`failures`/`errors` attributes.
