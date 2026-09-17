# CloudWatch और Kinesis का लोकल सेटअप

यह project Floci के shared AWS endpoint पर CloudWatch custom metrics और Kinesis
Data Streams के basic producer/consumer flows दिखाता है। Real AWS में यही SDK
operations काम आएँगे। पहले चलाएँ:

```bash
docker compose up -d --build
```

## CloudWatch custom metric

Metric publish करें:

```bash
curl -X POST http://localhost:8082/api/cloudwatch/metrics \
  -H 'Content-Type: application/json' \
  -d '{"metricName":"OrdersProcessed","value":3,"unit":"Count"}'
```

Namespace के metrics देखें:

```bash
curl http://localhost:8082/api/cloudwatch/metrics
```

`namespace` का default `LearningApp` है। CloudWatch metric तुरंत दिखाई न दे तो
कुछ seconds प्रतीक्षा करें। Production में dimensions, timestamps, alarms और
retention policy भी configure करें।

## Kinesis stream

Stream बनाएँ और उसका ARN लें:

```bash
curl -X POST http://localhost:8082/api/kinesis/stream
```

Record भेजें:

```bash
curl -X POST http://localhost:8082/api/kinesis/records \
  -H 'Content-Type: application/json' \
  -d '{"partitionKey":"orders","data":"Order-101"}'
```

Shard ID लें:

```bash
curl http://localhost:8082/api/kinesis/shards
```

पहली बार shard से पढ़ें:

```bash
curl 'http://localhost:8082/api/kinesis/records?shardId=shardId-000000000000'
```

Response में `nextShardIterator` मिलेगा। उसी iterator को अगली request में भेजकर
आगे पढ़ें:

```bash
curl --get http://localhost:8082/api/kinesis/records \
  --data-urlencode 'shardId=shardId-000000000000' \
  --data-urlencode 'iterator=PASTE_NEXT_SHARD_ITERATOR_HERE'
```

Kinesis में partition key records को shards में route करती है। Records को durable
रखने के लिए retention period होता है; consumer iterator से आगे बढ़ता है और
record को SQS की तरह delete नहीं करता।

## AWS CLI concepts

```bash
export AWS_ENDPOINT_URL=http://localhost:4566
export AWS_DEFAULT_REGION=us-east-1
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test

aws cloudwatch put-metric-data \
  --namespace LearningApp \
  --metric-data '[{"MetricName":"OrdersProcessed","Value":1,"Unit":"Count"}]' \
  --endpoint-url "$AWS_ENDPOINT_URL"

aws kinesis create-stream --stream-name learning-stream --shard-count 1 \
  --endpoint-url "$AWS_ENDPOINT_URL"
```

Local credentials केवल Floci के लिए हैं। Real AWS credentials Git, YAML या code
में hard-code न करें; IAM policy को required CloudWatch और Kinesis actions तक
सीमित रखें।