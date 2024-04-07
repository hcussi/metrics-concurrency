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
* Kubernetes
  * AWS ECR
  * AWS EKS
  * AWS EC2

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

Building and register YOUR own docker image or use the public one located at https://hub.docker.com/r/hernancussi/metrics-concurrency 
If you want to build your own:

```bash
./gradlew bootBuildImage --imageName=[YOUR_ACCOUNT]/metrics-concurrency:0.0.1 --createdDate now
```

Testing it

```bash
docker run --name java-metrics-concurrency -p 8080:8080 --env-file .env --env JAVA_OPTS="--enable-preview" [YOUR_ACCOUNT]/metrics-concurrency:0.0.1 env
```

#### Local environment

In order to use Redis, collect metrics with Prometheus and use Grafana dashboard execute the following command:

```bash
docker-compose -f docker-compose-concurrency.yml up -d
```

### Concurrency strategies

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

### Kubernetes

`Kind`, `K3s`, `Docker Desktop` among others to test this locally. 
Be sure to stop docker containers created by `docker-compose`.

#### ConfigMap

Update `.env` first: 

```bash
REDIS_HOST=concurrency-redis-svc
```

Generate config map:
```bash
kubectl create configmap concurrency-config --from-env-file=.env
```

Check config map created:
```bash
kubectl describe configmaps concurrency-config
```

#### Pods & Load Balancers

Create pods:

```bash
kubectl apply -f deploy/deployment.yml
```

Check state:

```bash
kubectl get pods --watch
```

Create LB to expose services:

```bash
kubectl apply -f deploy/load-balancers.yml
```

```bash
kubectl get services
```

```bash
kubectl get deployments
```

Check for errors if it doesn't start correctly.

```bash
 kubectl describe pods
```

```bash
kubectl logs concurrency-java-[hash]
```

### AWS

Install and configure AWS CLI

More info:
- https://support.count.ly/hc/en-us/articles/4413310032793-Docker-and-Kubernetes-Cluster-Deployment-with-Amazon-Elastic-Kubernetes-Service-EKS

#### Amazon Container Elastic Registry

In the AWS Console create a container for the image `metrics-concurrency`, tag it and upload if following the steps provided by AWS guide.

#### Amazon IAM

Create VPC

```bash
aws cloudformation create-stack \
  --region [REGION] \
  --stack-name metrics-concurrency-eks-vpc-stack \
  --template-url https://s3.us-west-2.amazonaws.com/amazon-eks/cloudformation/2020-10-29/amazon-eks-vpc-private-subnets.yaml
```

Create IAM role

```bash
aws iam create-role \
  --role-name eksClusterRole \
  --assume-role-policy-document file://"deploy/aws/eks/cluster-trust-policy.json"
```

```bash
aws iam create-role \
  --role-name eKSNodeRole \
  --assume-role-policy-document file://"deploy/aws/eks/node-role-trust-policy.json"
```

```bash
aws iam attach-role-policy \
  --policy-arn arn:aws:iam::aws:policy/AmazonEKSClusterPolicy \
  --role-name eksClusterRole
```

```bash
aws iam attach-role-policy \
  --policy-arn arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy \
  --role-name eKSNodeRole
aws iam attach-role-policy \
  --policy-arn arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly \
  --role-name eKSNodeRole
aws iam attach-role-policy \
  --policy-arn arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy \
  --role-name eKSNodeRole
```

More info
- https://docs.aws.amazon.com/eks/latest/userguide/service_IAM_role.html#create-service-role

#### Amazon EKS

Named cluster `metrics-concurrency` and select IAM role created, VPC created, Security Group and make it private.

Configure this cluster to be used locally with `kubectl` by adding new context:

```bash
aws eks update-kubeconfig --region [REGION] --name metrics-concurrency
```

```bash
 kubectl config get-contexts
```

The namespace used by default will be `arn:aws:eks:[REGION]:[ACCOUNT_ID]:cluster/metrics-concurrency`.

Testing it
```bash
kubectl get svc
```

In AWS console add node group `metrics-concurrency-nodegroup` under Compute section, select IAM role created.
Using `t2.small` will prevent (this is for the limited IP addresses in the node):

```bash
kubectl describe pods
...
Events:
  Type     Reason            Age                  From               Message
  ----     ------            ----                 ----               -------
  Warning  FailedScheduling  12s (x2 over 5m20s)  default-scheduler  0/2 nodes are available: 2 Too many pods. preemption: 0/2 nodes are available: 2 No preemption victims found for incoming pod.
```

More info
- https://docs.aws.amazon.com/eks/latest/userguide/getting-started-console.html

Rename the `deploy/aws/eks/.env.example` file to be `deploy/aws/eks/.env` and generate config map:
```bash
kubectl create configmap concurrency-config --from-env-file=deploy/aws/eks/.env
```

Create pods:
```bash
kubectl apply -f deploy/aws/eks/deployment.yml
```

```bash
kubectl get pods --watch
```

Navigate to all API endpoints using `EXTERNAL-IP` from

```bash
kubectl get svc
```

Navigate to [Health endpoint](http://AWS_ACCOUNT_ID.us-east-2.elb.amazonaws.com/actuator/health) 
