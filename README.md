# Getting Started

Clone the repository and setup JDK 21 locally, using `sdkman` is a good alternative.
Rename the `.env.example` file to be `.env`.

### Run Application for development

```bash
./gradlew bootRun
```

This will expose the REST endpoints available. Go to [http://localhost:8080/api/docs/swagger-ui](http://localhost:8080/api/docs/swagger-ui) to see the corresponding API docs (OpenAPI 3.0). 
Also, can check [http://localhost:8080/api/docs](http://localhost:8080/api/docs) to see Open API config JSON.
Use the basic auth credentials set in the `.env` file.

### JWT

Before to be able to generate a valid JWT token, a secret key needs to be defined in `.env`, recommended way is to use
the provided API endpoint `/api/v1/generateSecret` that will return a secret using the HS512 hash algorithm.

```text
HS512 is HMAC-SHA-512, and that produces digests that are 512 bits (64 bytes) long, so HS512 requires that you use a secret key that is at least 64 bytes long.
```

```bash
curl http://localhost:8080/api/v1/generateSecret
```

Getting valid JWT token by using `login` endpoint and user credentials defined in `.env` file.
The token then can be used in the authorization header for `/api/v1/*` endpoints.

```bash
curl  -d '{"username": MY_API_USERNAME, "password": MY_API_PASSWORD}' -H "Content-Type: application/json" http://localhost:8080/api/v1/authentication
```

```bash
curl  -d '{"numbers": [1, 2, 3]}' -H "Content-Type: application/json" -H "Authorization: Bearer JWT_TOKEN" http://localhost:8080/api/v1/calculateFactorial
```

#### Profiles

There are two spring profiles that can be activated for different cache strategies according to `.env` configuration.
By default, there are none cache strategy set.

* EhCache

```bash
./gradlew bootRun --args='--spring.profiles.active=ehcache'
```

* Redis

In order to use redis, the container needs to be created, refer to `Docker Compose` section.

```bash
./gradlew bootRun --args='--spring.profiles.active=redis'
```

## Technological Stack

* Oracle JDK 21 with preview features
* Spring Boot v3 (Spring Framework v6)
  * Spring Web (REST) 
  * Spring Security (JWT + basic auth)
  * Spring AOP
  * Spring Docs (OpenAPI 3.0)
  * Spring Observability, Micrometer(Prometheus) + Grafana
  * Spring Scheduling
* Gatling for loading tests
* Docker Compose
  * Prometheus
  * Grafana
  * Redis

### Java 21 and preview features

There are three features used in this project:
* anonymous variables
* string templates
* virtual threads with structured subtasks

### Security

* Basic auth is used for api docs and prometheus endpoint.
* JWT is used for api endpoints.

### AOP

It is used for the following cases:
* Logging REST request and params.
* Validate REST input values.
* Logging unexpected exceptions in services.
* Getting and store values in caches.
* Endpoints telemetry.

### Scheduling

Used to clean the cache every 30 minutes.

### Cache
Can be enabled by using different spring profiles.
* ehCache: In memory cache with limited entries configured. The size can be set by changing `concurrency.ehcache-size` in the `application.properties`
* Redis: Key-value store. Host and credentials defined in the `.env` file.

### Docker Compose

Run everything from dockerize environment.

#### SpringBoot

Building and register docker image

```bash
./gradlew bootBuildImage --imageName=hcussi/metrics-concurrency:0.0.1 --createdDate now
```

Testing it

```bash
docker run --name java-metrics-concurrency -p 8080:8080 --env-file .env --env JAVA_OPTS="--enable-preview" hcussi/metrics-concurrency:0.0.1 env
```

In order to use Redis, collect metrics with Prometheus and use Grafana dashboard execute the following command:

```bash
docker-compose -f docker-compose-concurrency.yml up -d
```

### Concurrency

There are two concurrency strategies that can be used to calculate mathematical operations, `?strategy=VIRTUAL` and `?strategy=THREADS`.

#### Virtual Threads vs Platform Threads

Platform Threads basically works as a wrapper for OS processes, the limitation of concurrency is limited to the number of cores. Virtual Threads works in the JVM level, and can be more efficient to share memory and parallelize IO operations.

### Functional Interface

Stream API in collections is the Java approach to use functional programming concepts in a OOP language.
`MathematicalOperation` functional interface is used as lambda expression in different math calculation in order to generalize the math algorithms.

### Telemetry

Spring actuator is used in order to use different metrics and collect them in Prometheus.
`actuator/*` endpoints are accessed by using basic authentication.

#### Prometheus

Consume the REST endpoint `actuator/prometheus` in order to collect the different metrics for the different endpoints.
It also uses the basic authentication to access the info.
Visit [http://localhost:9090](http://localhost:9090), no login credentials are required.

#### Grafana

A default datasource and dashboard has been provided in order to check the different metrics collected by Prometheus.
Visit [http://localhost:3000/login](http://localhost:3000/login) and login with default credentials `admin/admin`.

### Gatling

API local load testing has been defined to stress the different endpoints and to compare the different cache and calculation strategies.

```bash
./gradlew gatlingRun -DGATLING_TOKEN=My_JWT_Token
```

After running the gatling task the performance reports can be checked.

### GraphQl


### Native Images


### Kubernetes


