# Spring Boot Deployment Guide — Local Floci to Production

Yeh file aapke project ka complete deployment flow samjhati hai.

```
Code → Test → Docker Image → ECR → ECS → Health Check → CI/CD → Production
```

## 1. Abhi aapke project me kya hai?

| Component | Kaam |
| --- | --- |
| Spring Boot app | `/hello` REST API deta hai. |
| Dockerfile | Java app ko portable Docker image banata hai. |
| Docker Compose | App aur Floci ko ek command se start karta hai. |
| Floci | Laptop par free AWS emulator: ECR, ECS, S3, DynamoDB etc. |
| GitHub Actions | Pull request par test; real AWS setup ke baad image build aur deploy. |

Floci **real AWS nahi** hai. Yeh AWS seekhne aur local testing ke liye hai. Isme koi AWS bill nahi aata.

## 2. Daily development workflow

Project folder me yeh command run karo:

```bash
docker compose up -d --build
```

Is command se do containers start honge:

```text
hello-world  → Spring Boot application
floci        → Local AWS emulator
```

Check:

```bash
docker compose ps
curl http://localhost:8082/hello
curl http://localhost:8082/actuator/health/readiness
```

Expected output:

```text
Hello World
{"status":"UP"}
```

Logs dekhne ke liye:

```bash
docker compose logs -f hello-world
docker compose logs -f floci
```

Stop karne ke liye:

```bash
docker compose down
```

`8082` port use ho raha hai kyunki host ka `8080` port pehle se busy tha. Container ke andar app ab bhi port `8080` par hi chalti hai.

## 3. Code change ke baad quality check

Har code change ke baad:

```bash
mvn verify
docker compose up -d --build
curl http://localhost:8082/actuator/health/readiness
```

Rule:

```text
Tests fail → code fix karo → tests pass → image build karo → deploy karo
```

## 4. AWS CLI ko Floci se connect karo

Har new terminal me yeh variables run karo:

```bash
export AWS_ENDPOINT_URL=http://localhost:4566
export AWS_DEFAULT_REGION=us-east-1
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
```

Verify:

```bash
aws sts get-caller-identity
aws ecs list-clusters
```

Yeh commands real AWS ke same hain. Sirf endpoint Floci ka hai, isliye yeh free aur local hain.

## 5. Docker image build

```bash
docker build -t hello-world:v1 .
docker image ls hello-world
```

Image ke andar:

- Java 21 runtime
- Spring Boot JAR
- non-root user `spring`
- readiness health check

## 6. Local ECR: image registry practice

ECR ko Docker images rakhne ki AWS service samjho.

### Create repository

```bash
aws ecr create-repository --repository-name hello-world
```

### Get the image URI

```bash
aws ecr describe-repositories \
  --repository-names hello-world \
  --query 'repositories[0].repositoryUri' \
  --output text
```

Floci version ke hisaab se output aisa ho sakta hai:

```text
localhost:5000/000000000000/us-east-1/hello-world
```

Is installed Floci version me backing registry host port `5100` par exposed hai.
Isliye is project ke liye `IMAGE_URI` yeh use karo:

```bash
export IMAGE_URI=localhost:5100/000000000000/us-east-1/hello-world
```

### Tag and push

```bash
docker tag hello-world:v1 $IMAGE_URI:v1
docker push $IMAGE_URI:v1
```

Verify:

```bash
aws ecr list-images --repository-name hello-world
```

Production rule: image ko `latest` se deploy mat karo. Git commit SHA ya release version use karo, for example `v1.0.0` or `a1b2c3d`.

## 7. Local ECS: container run practice

ECS AWS ka container-runner service hai. Real AWS ECS me Fargate/EC2 par containers run hote hain; Floci iske liye local Docker container run karta hai.

### Create ECS cluster

```bash
aws ecs create-cluster --cluster-name hello-cluster
```

### Register task definition

`IMAGE_URI` ko Section 6 wala actual value rakhna hai.

```bash
aws ecs register-task-definition \
  --family hello-world-local \
  --network-mode bridge \
  --container-definitions "[
    {
      \"name\": \"hello-world\",
      \"image\": \"$IMAGE_URI:v1\",
      \"memory\": 512,
      \"cpu\": 256,
      \"portMappings\": [
        { \"containerPort\": 8080, \"hostPort\": 8081, \"protocol\": \"tcp\" }
      ]
    }
  ]"
```

### Run the ECS task

```bash
aws ecs run-task \
  --cluster hello-cluster \
  --task-definition hello-world-local \
  --count 1
```

### Verify task and API

```bash
aws ecs list-tasks --cluster hello-cluster
docker ps
curl http://localhost:8081/hello
```

Note: yeh ECS practice task aur Docker Compose app alag hain. Compose app `8082` par hai; ECS task `8081` par expose hota hai.

## 8. Health checks ka use

| Endpoint | Meaning |
| --- | --- |
| `/hello` | Business API working hai. |
| `/actuator/health` | Overall application health. |
| `/actuator/health/readiness` | Traffic receive karne ke liye app ready hai. |

Production load balancer ko readiness endpoint check karna chahiye. Agar health check fail ho, ECS traffic na bheje aur unhealthy task replace kare.

## 9. Git workflow

Industry workflow:

```text
main branch
  └─ feature branch banao
       └─ code + tests
            └─ Pull Request
                 └─ GitHub Actions tests pass
                      └─ Review + merge into main
                           └─ Docker image build/push
                                └─ ECS deployment
```

Useful commands:

```bash
git checkout -b feature/add-message
git status
git add .
git commit -m "Add message endpoint"
git push origin feature/add-message
```

GitHub par Pull Request create karo. Tests green hone ke baad `main` me merge karo.

## 10. GitHub Actions CI/CD

File: `.github/workflows/ci-cd.yml`

Current pipeline:

1. Pull request ya `main` push par `mvn verify`.
2. `main` par tests pass hue to Docker image build.
3. Image Amazon ECR me push.
4. ECS service new image ke saath update.
5. ECS service stable hone ka wait.

### Important

GitHub-hosted Actions runner aapke laptop ke Floci ko access nahi kar sakta.

Isliye:

- Floci: local learning/testing ke liye.
- GitHub Actions test job: abhi bhi run kar sakta hai.
- Real ECR/ECS deploy job: tab use karo jab real AWS account aur setup ho.

Local Floci deploy ko GitHub Actions se run karna ho to self-hosted runner aapke laptop par configure karna padega. Beginner ke liye abhi zaroori nahi hai.

## 11. Real AWS production setup — jab ready ho

Real AWS use karne se pehle Budget Alert zaroor create karo. Free Tier bhi limits cross hone par charge kar sakta hai.

### Required AWS resources

```text
AWS Account
  ├─ ECR Repository
  ├─ ECS Cluster (Fargate)
  ├─ ECS Service
  ├─ Task Execution IAM Role
  ├─ VPC + public/private subnets
  ├─ Security Groups
  ├─ Application Load Balancer
  ├─ Target Group: /actuator/health/readiness
  ├─ CloudWatch Log Group
  └─ GitHub OIDC IAM Role
```

### GitHub settings required

Create GitHub Environment: `production`.

Repository secret:

```text
AWS_ROLE_TO_ASSUME
```

Repository variables:

```text
AWS_REGION
ECR_REPOSITORY
ECS_CLUSTER
ECS_SERVICE
```

GitHub OIDC use karo; permanent `AWS_ACCESS_KEY_ID` aur `AWS_SECRET_ACCESS_KEY` GitHub secrets me rakhna avoid karo.

## 12. Production checklist

Deploy se pehle:

- [ ] `mvn verify` successful
- [ ] Docker image locally build hoti hai
- [ ] API and readiness endpoint work karte hain
- [ ] Image tag immutable hai (commit SHA/version)
- [ ] Secrets environment variables/Secrets Manager me hain, code/Git me nahi
- [ ] HTTPS + custom domain configured hai
- [ ] Load balancer health check readiness endpoint use karta hai
- [ ] Logs CloudWatch me hain
- [ ] Monitoring/alerts enabled hain
- [ ] Database backup aur restore plan hai
- [ ] Least-privilege IAM roles hain
- [ ] Deployment rollback plan hai

## 13. Aapka recommended learning roadmap

1. `docker compose` ko ache se samjho.
2. ECR me image push karo.
3. ECS task run karo.
4. ECS service aur desired count samjho.
5. S3 integration add karo.
6. DynamoDB CRUD API banao.
7. SQS queue + async worker add karo.
8. Terraform se Floci resources banao.
9. Same Terraform real AWS Free Tier par deploy karo.
10. GitHub Actions + real ECR/ECS pipeline enable karo.

## 14. Most useful commands

```bash
# Start app + Floci
docker compose up -d --build

# Stop stack
docker compose down

# Application logs
docker compose logs -f hello-world

# Floci logs
docker compose logs -f floci

# Java tests
mvn verify

# Check API
curl http://localhost:8082/hello

# Check health
curl http://localhost:8082/actuator/health/readiness

# Check local ECS resources
aws ecs list-clusters
aws ecs list-tasks --cluster hello-cluster
```
