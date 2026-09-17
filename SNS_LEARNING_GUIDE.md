# SNS सीखने का लोकल सेटअप

इस project में Amazon SNS सीखने के लिए Floci का local SNS emulator इस्तेमाल हो
रहा है। SNS एक **pub/sub notification service** है: publisher topic पर message
publish करता है और topic के subscribers को वह message मिलता है।

## Package structure

SNS code अलग package में है:

```text
src/main/java/com/example/helloworld/sns/
├── SnsConfig.java             # AWS SNS client
├── SnsService.java            # topic, publish और subscribe operations
├── SnsController.java         # REST practice API
├── SnsPublishRequest.java     # publish body
└── SnsSubscribeRequest.java   # subscription body
```

Default topic का नाम `learning-topic` है।

## 1. Start करना

```bash
docker compose up -d --build
docker compose ps
```

Floci `localhost:4566` पर और Spring Boot app `localhost:8082` पर चलेगी। App के
अंदर SNS endpoint `http://floci:4566` है।

## 2. REST API से SNS practice

### Topic create करना

यह idempotent operation है; topic मौजूद हो तो वही ARN वापस मिलता है:

```bash
curl -X POST http://localhost:8082/api/sns/topic
```

Response से `TOPIC_ARN` set कर सकते हैं:

```bash
export TOPIC_ARN=$(curl --silent -X POST http://localhost:8082/api/sns/topic)
echo "$TOPIC_ARN"
```

### Subscription बनाना

SNS subscriber को protocol और endpoint चाहिए। Floci में HTTP endpoint learning
के लिए उपयोग कर सकते हैं:

```bash
curl -X POST \
  -H 'Content-Type: application/json' \
  -d '{"protocol":"http","endpoint":"http://example.com/notifications"}' \
  http://localhost:8082/api/sns/subscriptions
```

Available subscriptions:

```bash
curl http://localhost:8082/api/sns/subscriptions
```

Production में `email`, `https`, `sqs`, `lambda` जैसे protocols भी use होते हैं।
Email और कुछ HTTP subscriptions में confirmation step आता है।

### Message publish करना

```bash
curl -X POST \
  -H 'Content-Type: application/json' \
  -d '{"subject":"Learning","message":"Hello from SNS"}' \
  http://localhost:8082/api/sns/publish
```

Response में `MessageId` मिलेगा। SNS publisher को subscriber की implementation
जानने की जरूरत नहीं होती; topic fan-out संभालता है।

## 3. AWS CLI से SNS practice

Host terminal में local credentials set करें:

```bash
export AWS_ENDPOINT_URL=http://localhost:4566
export AWS_DEFAULT_REGION=us-east-1
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
```

Topic create करें:

```bash
TOPIC_ARN=$(aws sns create-topic \
  --name learning-topic \
  --endpoint-url "$AWS_ENDPOINT_URL" \
  --query TopicArn \
  --output text)
echo "$TOPIC_ARN"
```

Topic list:

```bash
aws sns list-topics --endpoint-url "$AWS_ENDPOINT_URL"
```

Publish:

```bash
aws sns publish \
  --topic-arn "$TOPIC_ARN" \
  --subject 'CLI Practice' \
  --message 'Message from AWS CLI' \
  --endpoint-url "$AWS_ENDPOINT_URL"
```

HTTP subscription:

```bash
aws sns subscribe \
  --topic-arn "$TOPIC_ARN" \
  --protocol http \
  --notification-endpoint http://example.com/notifications \
  --endpoint-url "$AWS_ENDPOINT_URL"
```

Subscriptions list:

```bash
aws sns list-subscriptions-by-topic \
  --topic-arn "$TOPIC_ARN" \
  --endpoint-url "$AWS_ENDPOINT_URL"
```

## 4. SNS और SQS साथ में

Real event architecture में SNS topic से SQS queue subscribe की जाती है। फिर:

```text
Publisher → SNS topic → SQS queue → Consumer
```

इससे एक event को कई independent consumers मिल सकते हैं। SQS guide में queue
और message operations देखें: [SQS_LEARNING_GUIDE.md](SQS_LEARNING_GUIDE.md).

## 5. Code flow

| Method | AWS operation | काम |
| --- | --- | --- |
| `ensureTopic()` | `CreateTopic` | topic बनाता या existing ARN देता है |
| `publish()` | `Publish` | topic पर notification भेजता है |
| `subscribe()` | `Subscribe` | endpoint को topic से जोड़ता है |
| `subscriptions()` | `ListSubscriptionsByTopic` | topic के subscribers दिखाता है |

## 6. Troubleshooting

```bash
curl http://localhost:4566/health
docker compose logs -f floci
docker compose logs -f hello-world
```

अगर API 404 दे:

```bash
docker compose down
docker compose up -d --build --force-recreate
```

अगर publish fail हो, पहले topic endpoint verify करें:

```bash
curl -X POST http://localhost:8082/api/sns/topic
```

## 7. Real AWS सावधानियाँ

Local `test/test` credentials केवल Floci के लिए हैं। Real AWS में credentials
hard-code न करें। IAM policy में केवल required SNS actions दें और production में
SQS dead-letter queues, retries, encryption तथा CloudWatch monitoring configure
करें।