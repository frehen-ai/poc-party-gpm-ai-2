# poc-party-gpm-ai

A project for experimenting with GPM persistence model that supports foreign key 
constraints, record history and state events.

## Database server setup

Environment variables for local database setup are located in [.env.properties](.env.properties) and is automatically read by 
the Spring configuration. It contains only dummy values for local development and is checked in.

The `.env` file is reserved for private secrets (e.g. `GH_TOKEN`). It is git-ignored and must never be committed.
Since Docker Compose reads `.env` by default, it is always pointed at `.env.properties` instead:

- Spring passes `--env-file=.env.properties` to Docker Compose (`spring.docker.compose.arguments` in
  [application.yaml](src/main/resources/application.yaml)).
- On the command line, `source set-env` exports the variables, which take precedence over `.env`.

The environment variables define the following user:

- `$POSTGRES_USER`: admin user for the Postgres database server and for the GPM service

There is currently no separate user for the GPM Service.

To start and stop the database via docker compose: 

```
source set-env
docker compose up -d && docker compose ps
docker compose down
```

The database server is running on port `$POSTGRES_PORT`, 15432 by default.

To test database connection from the command line, run

```
source set-env
PGPASSWORD=$POSTGRES_PASSWORD psql -h localhost -p $POSTGRES_PORT -U $POSTGRES_USER -d $POSTGRES_DB
```

To create or recreate the GPM schema from the commandline via Flyway:

```
source set-env
flyway clean migrate
```

## Running the application

To start the application:

```
./gradlew bootRun
```

No `source ./set-env` needed: Spring reads `.env.properties` itself and passes it to Docker Compose.

`spring-boot-docker-compose` is on the `dev` classpath. With property `spring.docker.compose.enabled: true`
and no DB running, Spring creates and starts the `compose.yaml` postgres, waits until it accepts connections, then 
Flyway migrates schema `gpm`. On shutdown Spring stops the container again. With DB already running 
(`docker compose up -d`), Spring detects it and leaves it alone.

Serves on the default spring server port, **http://localhost:8080**.

## Tests — self-contained via Testcontainers

`@SpringBootIntegrationTest` imports `PostgresContainerConfiguration`, which starts its
own `postgres:15.3` container and rebinds `gpm.datasource.url/username/password` via a
`DynamicPropertyRegistrar`.

Tests therefore need **no** external database and **no** `source ./set-env`. A working docker daemon is the only 
prerequisite.

```
./gradlew clean test                                          # all
./gradlew test --tests "no.frehen.pocpartygpm.test.context.DatabaseIntegrationTest"
./gradlew test --tests "no.frehen.pocpartygpm.test.context.DatabaseIntegrationTest.can_access_database"
```


