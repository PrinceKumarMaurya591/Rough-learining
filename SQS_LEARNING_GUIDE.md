# SQS सीखने का लोकल सेटअप

इस project में Amazon SQS सीखने के लिए Floci का local SQS emulator इस्तेमाल हो
रहा है। Java code AWS SDK for Java 2.x का उपयोग करता है, इसलिए यही concepts real
AWS SQS में भी काम आएँगे।

## Package structure

पूरा SQS code इस अलग package में है:

```text
src/main/java/com/example/helloworld/sqs/
├── SqsConfig.java          # AWS SQS client
├── SqsService.java         # queue और message operations
├── SqsController.java      # REST practice API
├── SqsMessage.java         # received message response
└── SqsDeleteRequest.java   # delete request body
```

Configured queue का default नाम `learning-queue` है।

## 1. Start करना

```bash
docker compose up -d --build
docker compose ps
```

Floci `localhost:4566` पर और Spring Boot app `localhost:8082` पर चलेगी। SQS
के लिए app के अंदर endpoint `http://floci:4566` है।

## 2. REST API से queue और messages

### Queue create/get URL

यह idempotent operation है; queue पहले से हो तो उसका URL फिर मिल जाता है:

```bash
curl -X POST http://localhost:8082/api/sqs/queue
```

### Producer: message भेजना

```bash
curl -X POST \
  -H 'Content-Type: text/plain' \
  --data 'Hello from SQS producer' \
  http://localhost:8082/api/sqs/messages
```

Response में `MessageId` मिलेगा। Producer queue में message डालता है; वह
message तुरंत किसी consumer के लिए available रहता है।

दूसरा message:

```bash
curl -X POST \
  -H 'Content-Type: text/plain' \
  --data 'Order-101 process karna hai' \
  http://localhost:8082/api/sqs/messages
```

### Consumer: messages receive करना

```bash
curl 'http://localhost:8082/api/sqs/messages?maxMessages=10'
```

Response का example:

```json
[
  {
    "messageId": "...",
    "body": "Hello from SQS producer",
    "receiptHandle": "..."
  }
]
```

SQS में receive करने से message delete नहीं होता। वह थोड़े समय के लिए invisible
होता है, ताकि consumer उसे process कर सके। Response का `receiptHandle` delete
के लिए संभालकर रखें।

### Message delete करना

Processing सफल होने के बाद receipt handle के साथ delete करें:

```bash
curl -X DELETE \
  -H 'Content-Type: application/json' \
  -d '{"receiptHandle":"PASTE_RECEIPT_HANDLE_HERE"}' \
  http://localhost:8082/api/sqs/messages
```

Delete के बाद वही message दोबारा receive नहीं होगा। अगर processing fail हो,
तो delete मत करें; visibility timeout के बाद message फिर मिल सकता है।

## 3. AWS CLI से SQS practice

Host terminal में variables set करें:

```bash
export AWS_ENDPOINT_URL=http://localhost:4566
export AWS_DEFAULT_REGION=us-east-1
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
```

Queue URL लें:

```bash
QUEUE_URL=$(aws sqs create-queue \
  --queue-name learning-queue \
  --endpoint-url "$AWS_ENDPOINT_URL" \
  --query QueueUrl \
  --output text)
echo "$QUEUE_URL"
```

Send:

```bash
aws sqs send-message \
  --queue-url "$QUEUE_URL" \
  --message-body 'Message from AWS CLI' \
  --endpoint-url "$AWS_ENDPOINT_URL"
```

Receive:

```bash
aws sqs receive-message \
  --queue-url "$QUEUE_URL" \
  --max-number-of-messages 10 \
  --wait-time-seconds 1 \
  --endpoint-url "$AWS_ENDPOINT_URL"
```

Queue list:

```bash
aws sqs list-queues --endpoint-url "$AWS_ENDPOINT_URL"
```

## 4. Code flow

`SqsConfig`:

- region और credentials environment/configuration से लेता है;
- `AWS_ENDPOINT_URL` set होने पर request Floci को भेजता है;
- real AWS में endpoint override खाली रहेगा।

`SqsService`:

| Method | AWS operation | काम |
| --- | --- | --- |
| `ensureQueue()` | `CreateQueue` | queue बनाता या existing queue URL देता है |
| `send()` | `SendMessage` | message enqueue करता है |
| `receive()` | `ReceiveMessage` | messages को temporarily invisible करता है |
| `delete()` | `DeleteMessage` | successful processing के बाद message हटाता है |

## 5. SQS के जरूरी concepts

### Queue

Queue messages का durable buffer है। Producer और consumer को एक ही समय online
होने की जरूरत नहीं होती।

### Visibility timeout

Receive के बाद message बाकी consumers से temporarily hide होता है। Processing
पूरी होने पर delete करना जरूरी है।

### At-least-once delivery

SQS message कभी-कभी दोबारा deliver कर सकता है। Consumer code idempotent रखें,
जैसे order ID को दो बार process न होने दें।

### Long polling

`waitTimeSeconds=1` इस demo में short polling cost कम करने का basic example है।
Real application में empty responses कम करने के लिए long polling, commonly up
to 20 seconds, configure करें।

### Standard और FIFO

यह demo Standard queue use करता है। Standard queue high throughput देता है,
लेकिन strict ordering और exactly-once processing guarantee नहीं देता। Ordering
चाहिए तो `.fifo` queue, message group और deduplication settings सीखें।

## 6. Troubleshooting

Floci health:

```bash
curl http://localhost:4566/health
docker compose logs -f floci
docker compose logs -f hello-world
```

अगर API 404 दे, current image rebuild करें:

```bash
docker compose down
docker compose up -d --build --force-recreate
```

अगर message receive नहीं हो रहा, पहले send response और queue name check करें:

```bash
curl -X POST -H 'Content-Type: text/plain' \
  --data 'test message' \
  http://localhost:8082/api/sqs/messages
curl 'http://localhost:8082/api/sqs/messages?maxMessages=10'
```

## 7. Real AWS सावधानियाँ

Local credentials `test/test` केवल Floci के लिए हैं। Real AWS credentials code,
YAML या Git में hard-code न करें। IAM policy में केवल required SQS actions दें,
और production में dead-letter queue, encryption, visibility timeout तथा
CloudWatch monitoring configure करें।