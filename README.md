# Hello World Spring Boot API

A minimal REST API that responds with `Hello World `.

## Run

```bash
mvn spring-boot:run
```

Then open [http://localhost:8080/hello](http://localhost:8080/hello), or run:

```bash
curl http://localhost:8080/hello
```

## Test

```bash
mvn test
```

## Production workflow

The GitHub Actions workflow runs `mvn verify` for every pull request. A merge to
`main` runs the tests again, builds a Docker image, pushes an immutable
commit-SHA tag to Amazon ECR, and updates an Amazon ECS/Fargate service only
after those steps succeed.

### Run the production container locally

```bash
docker compose up --build
curl http://localhost:8080/hello
curl http://localhost:8080/actuator/health
```

### One-time AWS and GitHub setup

1. Create an ECR repository, ECS/Fargate cluster and ECS service behind an
   Application Load Balancer. Configure the load balancer health check as
   `/actuator/health/readiness` on port `8080`.
2. Create the CloudWatch log group `/ecs/hello-world` and an ECS task execution
   role named `ecsTaskExecutionRole` (or update `ecs/task-definition.json`).
3. Configure GitHub Environment `production` with required reviewers.
4. Add the repository secret `AWS_ROLE_TO_ASSUME` for a GitHub OIDC role. Add
   repository variables `AWS_REGION`, `ECR_REPOSITORY`, `ECS_CLUSTER`, and
   `ECS_SERVICE`.
5. Create a GitHub repository, add it as `origin`, push the `main` branch, then
   merge through pull requests. The deployment workflow begins only on `main`.

Floci is useful to emulate AWS services in local development and CI. It is not
a production hosting service, so this application does not deploy *to* Floci.

## Learn S3 locally

The project includes a complete Floci-backed S3 example with bucket and object
CRUD endpoints. Follow [S3_LEARNING_GUIDE.md](S3_LEARNING_GUIDE.md) for setup,
AWS CLI commands, curl examples, configuration, and troubleshooting.

SQS practice is isolated in the `com.example.helloworld.sqs` package. Follow
[SQS_LEARNING_GUIDE.md](SQS_LEARNING_GUIDE.md) for queue, producer, consumer,
delete, AWS CLI, and troubleshooting steps.

SNS practice is isolated in the `com.example.helloworld.sns` package. Follow
[SNS_LEARNING_GUIDE.md](SNS_LEARNING_GUIDE.md) for topics, publishing,
subscriptions, AWS CLI, and SNS-to-SQS concepts.

Secrets Manager and Parameter Store practice is isolated in the
`com.example.helloworld.secretsmanager` and
`com.example.helloworld.parameterstore` packages. Follow
[SECRETS_AND_PARAMETERS_GUIDE.md](SECRETS_AND_PARAMETERS_GUIDE.md) for local
Floci commands, REST APIs, AWS CLI, and service differences.

CloudWatch metrics and Kinesis stream practice are isolated in the
`com.example.helloworld.cloudwatch` and `com.example.helloworld.kinesis`
packages. Follow [CLOUDWATCH_KINESIS_LEARNING_GUIDE.md](CLOUDWATCH_KINESIS_LEARNING_GUIDE.md)
for local REST and AWS CLI examples.
