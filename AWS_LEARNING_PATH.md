# AWS Deployment Learning Path — Bilkul Simple Hindi Me

Is file ko order me padho. Ek step samajh aur complete hue bina next step par mat jao.

## Pehle poori story samjho

Tumne Java me app banayi:

```text
Code → Hello World API
```

Company me app ko directly laptop se internet par nahi chalate. Usko package karke cloud par run karte hain:

```text
Java Code
   ↓
Docker Image
   ↓
ECR (image store karne ki jagah)
   ↓
ECS (image ko run karne ki jagah)
   ↓
Users API use karte hain
```

AWS par practice karne se paise lag sakte hain. Isliye hum **Floci** use kar rahe hain.

```text
Floci = tumhare laptop ke andar fake/local AWS
```

Yeh real AWS jaisi commands samajhne ke liye hai, lekin local aur free hai.

---

# Step 0 — Abhi kya chal raha hai?

## Kya hai?

Abhi tumhare Docker Compose me do containers chal rahe hain:

```text
1. hello-world = tumhari Spring Boot app
2. floci       = local AWS practice environment
```

## Kyun?

App Docker me chalegi, aur future me agar app S3, SQS, DynamoDB ya ECS use karegi to Floci se baat karegi.

## Check kaise karna hai?

```bash
docker compose ps
```

## Kya dikhna chahiye?

Do services `Up`/`healthy` dikhni chahiye:

```text
hello-world
floci
```

## API check

```bash
curl http://localhost:8082/hello
```

Expected:

```text
Hello World
```

## Health check kya hota hai?

```bash
curl http://localhost:8082/actuator/health/readiness
```

Expected:

```json
{"status":"UP"}
```

Iska matlab: app start ho chuki hai aur requests receive karne ke liye ready hai.

---

# Step 1 — Docker kya hai?

## Simple example

Socho tumhari Java app ek notebook hai. Har laptop me Java version, libraries aur settings alag ho sakti hain.

Docker app ko ek sealed box me pack karta hai:

```text
Docker Image = Java + app + libraries + required settings
```

Ab woh same box tumhare laptop, test server aur AWS—har jagah same chalega.

## Humne kya banaya?

File: `Dockerfile`

Yeh Docker ko bolti hai:

1. Maven se Java app build karo.
2. JAR file banao.
3. Chhoti runtime image me JAR copy karo.
4. App run karo.

## Kya command hai?

```bash
docker build -t hello-world:v1 .
```

## Isse kya hoga?

Tumhari app ki ek Docker image banegi jiska naam hai:

```text
hello-world:v1
```

Check:

```bash
docker image ls hello-world
```

## Is step ka real-company meaning

Developer code likhta hai → CI test chalata hai → Docker image banata hai.

---

# Step 2 — ECR kya hai?

## Simple example

Docker image ko ek file/package samjho. AWS ko woh image chahiye jise usse run karna hai.

```text
ECR = Docker images rakhne ka AWS locker/storage
```

Jaise GitHub code store karta hai, waise ECR Docker images store karta hai.

## Pehle Floci ko AWS CLI se connect karo

Terminal me run karo:

```bash
export AWS_ENDPOINT_URL=http://localhost:4566
export AWS_DEFAULT_REGION=us-east-1
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
```

## Isse kya hota hai?

AWS CLI ko hum bol rahe hain:

```text
Real AWS ke paas mat jao.
Laptop ke Floci ke paas jao.
```

`test` credentials fake hain aur sirf local Floci ke liye hain.

## ECR repository banao

```bash
aws ecr create-repository --repository-name hello-world
```

## Isse kya hoga?

Floci ke andar `hello-world` naam ki khaali image storage jagah banegi.

```text
Before: ECR me kuch nahi
After:  hello-world naam ka locker
```

## Check karo

```bash
aws ecr describe-repositories --repository-names hello-world
```

---

# Step 3 — Docker image ko ECR me push karna

## Kya kar rahe hain?

Step 1 me image tumhare laptop ke Docker me bani thi.

```text
hello-world:v1
```

Ab usi image ko ECR locker me copy karenge.

```text
Laptop Docker image → Floci ECR
```

## Kyun?

ECS direct tumhare source code ko run nahi karta. ECS Docker image ko run karta hai.

## Image URI kya hai?

Yeh ECR ke andar image ka full address hai.

Is project ke current Floci setup me use karo:

```bash
export IMAGE_URI=localhost:5100/000000000000/us-east-1/hello-world
```

## Commands

```bash
docker build -t hello-world:v1 .
docker tag hello-world:v1 $IMAGE_URI:v1
docker push $IMAGE_URI:v1
```

## Isse kya hoga?

```text
Before:
Laptop Docker: hello-world:v1
Floci ECR:     empty

After:
Laptop Docker: hello-world:v1
Floci ECR:     hello-world:v1
```

## Verify

```bash
aws ecr list-images --repository-name hello-world
```

`v1` dikhe to step complete hai.

---

# Step 4 — ECS kya hai?

## Simple example

```text
ECR = locker me image rakhta hai
ECS = locker se image nikal kar app run karta hai
```

Real AWS me ECS Docker image ko AWS server/Fargate par run karega.

Floci me ECS Docker image ko tumhare laptop ke Docker me run karega.

## ECS cluster kya hota hai?

```text
Cluster = containers run karne ki jagah/group
```

Command:

```bash
aws ecs create-cluster --cluster-name hello-cluster
```

Isse Floci ke andar `hello-cluster` naam ka container-running group banega.

---

# Step 5 — Task definition kya hai?

## Simple example

Task definition = ECS ke liye instructions.

```text
Kaunsi image chalani hai?
Kitni memory deni hai?
Kaunsa port expose karna hai?
Environment variables kya hain?
```

Real project me file `ecs/task-definition.json` isi ka example hai.

Local learning ke liye command se simple task definition register karo:

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

## Isse kya hoga?

ECS ke paas app ko run karne ki recipe save ho jayegi.

Abhi app run nahi hui hai. Sirf recipe bani hai.

---

# Step 6 — ECS task run karo

## Command

```bash
aws ecs run-task \
  --cluster hello-cluster \
  --task-definition hello-world-local \
  --count 1
```

## Isse kya hoga?

Floci ECS ECR wali Docker image ko run karega.

```text
ECR image
   ↓
ECS task
   ↓
New running Spring Boot container
```

## Check

```bash
docker ps
curl http://localhost:8081/hello
```

Expected:

```text
Hello World
```

## `8081` aur `8082` alag kyun hain?

| URL | Kaunsi app? |
| --- | --- |
| `http://localhost:8082/hello` | Docker Compose se direct chal rahi app |
| `http://localhost:8081/hello` | Floci ECS se chal rahi same app |

Is step me hum prove karte hain ki app ECS-style deployment se bhi run ho sakti hai.

---

# Step 7 — Test fail ho to kya hota hai?

Company workflow:

```text
Developer code change karta hai
   ↓
Tests run hote hain
   ↓
Tests fail?  → Deploy nahi hoga
Tests pass?  → Docker image banti hai
   ↓
Image ECR me push hoti hai
   ↓
ECS deploy karta hai
```

Tumhare project me Java tests chalane ke liye:

```bash
mvn verify
```

Output me `BUILD SUCCESS` aana chahiye.

---

# Step 8 — GitHub ka role

GitHub = code rakhne ki place + team collaboration.

```text
Code laptop par likha
   ↓
Git commit
   ↓
GitHub push
   ↓
GitHub Actions tests chalata hai
```

Tumhare project ki GitHub Action future real AWS flow ke liye bani hai.

Lekin GitHub runner tumhare laptop ke Floci ko access nahi kar sakta.

```text
Floci = local learning
GitHub Actions = test automation
Real AWS = actual production deployment
```

---

# Step 9 — Real AWS par jaane par kya change hoga?

Flow same rahega:

```text
Code → Docker → ECR → ECS → API
```

Lekin yeh cheezein change hongi:

| Local Floci | Real AWS |
| --- | --- |
| `http://localhost:4566` | AWS service endpoints |
| `test` credentials | IAM Roles / OIDC |
| Laptop Docker | ECS Fargate |
| No cost | AWS billing + limits |
| `localhost` URL | Load Balancer + HTTPS + Domain |

Isliye pehle Floci me flow samjho. Baad me small AWS account par same flow practice karna.

---

# Aaj tumhe kya karna hai?

Sirf **Step 0 aur Step 1** karo:

```bash
docker compose ps
curl http://localhost:8082/hello
docker build -t hello-world:v1 .
docker image ls hello-world
```

Yeh samajh lo ki Docker image kya hoti hai.

Uske baad mujhe yeh do outputs bhejna:

```text
1. docker compose ps
2. docker image ls hello-world
```

Phir hum **sirf Step 2 (ECR)** karenge. Ek time par ek hi concept.
