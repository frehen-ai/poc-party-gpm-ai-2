# Running the project (agent notes)

Supplements `README.md`. Prereqs: Docker running, Java 25, `flyway` and `psql` CLIs on PATH.
Run all commands from the project root.

Settings live in `.env.properties`. `.env` (if present) holds private secrets: never read, print, or commit it.
Plain `docker compose` reads `.env`, so always `source ./set-env` first or pass `--env-file .env.properties`.
Run each block below in a single shell call (`set-env` must stay in effect). DB host port: 15432.

## Database + Flyway
```
source ./set-env
docker compose up -d --wait
until pg_isready -h localhost -p $POSTGRES_PORT -q; do sleep 1; done   # no healthcheck in compose.yaml: "Healthy" only means container started
flyway clean migrate                                                   # expect "Successfully applied 1 migration" (or more)
docker compose down                                                    # stop
```

## Application
No `set-env` needed.
```
(./gradlew bootRun > /tmp/bootrun.log 2>&1 &)
ready=; for i in $(seq 90); do [ "$(curl -s -o /dev/null -w '%{http_code}' localhost:8080)" = 404 ] && ready=1 && break; sleep 2; done
[ -n "$ready" ] || { echo "NOT READY"; tail -50 /tmp/bootrun.log; }   # usually ready in ~10 s; gives up after 3 min
pid=$(lsof -ti tcp:8080 -sTCP:LISTEN); kill $pid                        # stop
while kill -0 $pid 2>/dev/null; do sleep 1; done                        # wait until Spring has stopped the DB
docker compose --env-file .env.properties down                          # remove the stopped DB container
docker compose --env-file .env.properties ps -a -q                      # expect no output
```
- Starts the DB itself via docker compose if not running, and stops (not removes) it on shutdown.
- If the DB from the Flyway section is still running, the app reuses it and leaves it running.
- No HTTP endpoints exist, so `404` means ready (also log line `Started PocPartyGpmApplicationKt`).
- After `kill`, Gradle reports `BUILD FAILED`; that is expected.

## Tests
`./gradlew clean test` (Testcontainers, no `set-env` or running DB needed).
`BUILD SUCCESSFUL` means all tests passed. Details for failures: `build/test-results/test/*.xml`.
