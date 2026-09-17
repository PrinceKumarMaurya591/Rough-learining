# S3 सीखने का पूरा लोकल सेटअप

इस project में Amazon S3 को local machine पर सीखने के लिए **Floci** इस्तेमाल हो रहा है। Floci AWS S3-compatible endpoint देता है, इसलिए Java code वही AWS SDK use करता है जो real AWS में चलेगा। Local practice के लिए credentials `test/test` हैं और कोई AWS bill नहीं आएगा।

## 1. Architecture

```text
curl / AWS CLI
        |
        v
Spring Boot app :8082
        |
        | AWS SDK for Java 2.x
        v
Floci S3 API :4566
        |
        v
learning-bucket
```

Important files:

| File | काम |
| --- | --- |
| `compose.yaml` | Floci और Spring Boot containers चलाता है |
| `src/main/java/com/example/helloworld/S3Config.java` | S3 client configure करता है |
| `src/main/java/com/example/helloworld/S3StorageService.java` | Bucket और object operations रखता है |
| `src/main/java/com/example/helloworld/S3Controller.java` | Practice के लिए REST API देता है |
| `src/main/resources/application.yml` | Bucket, region और endpoint configuration |

## 2. Start करना

Docker और Docker Compose installed होने चाहिए। Project root में:

```bash
docker compose up -d --build
docker compose ps
```

Expected services:

```text
floci        localhost:4566
hello-world  localhost:8082
```

Health check:

```bash
curl http://localhost:8082/actuator/health/readiness
```

Application start होने पर `learning-bucket` अपने-आप create होता है। यह behavior
`S3_AUTO_CREATE_BUCKET=true` से controlled है।

## 3. Host से AWS CLI configure करना

हर नए terminal में local endpoint और dummy credentials set करें:

```bash
export AWS_ENDPOINT_URL=http://localhost:4566
export AWS_DEFAULT_REGION=us-east-1
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
```

AWS CLI installed न हो तो अपने OS के अनुसार AWS CLI v2 install करें। Verify:

```bash
aws sts get-caller-identity --endpoint-url "$AWS_ENDPOINT_URL"
aws s3api list-buckets --endpoint-url "$AWS_ENDPOINT_URL"
```

Expected bucket में `learning-bucket` दिखना चाहिए। AWS CLI में `--endpoint-url`
लगाना जरूरी है; बिना इसके command real AWS को call कर सकती है।

## 4. Bucket commands

Bucket list:

```bash
aws s3api list-buckets --endpoint-url "$AWS_ENDPOINT_URL"
```

Bucket details:

```bash
aws s3api head-bucket \
  --bucket learning-bucket \
  --endpoint-url "$AWS_ENDPOINT_URL"
```

अगर auto-create बंद करके manually बनाना हो:

```bash
docker compose down
S3_AUTO_CREATE_BUCKET=false docker compose up -d --build
aws s3api create-bucket \
  --bucket learning-bucket \
  --endpoint-url "$AWS_ENDPOINT_URL"
```

## 5. Object upload, list, download और delete

### AWS CLI से

```bash
printf 'S3 se hello' > sample.txt

aws s3 cp sample.txt \
  s3://learning-bucket/notes/sample.txt \
  --endpoint-url "$AWS_ENDPOINT_URL"

aws s3 ls s3://learning-bucket/ \
  --recursive \
  --endpoint-url "$AWS_ENDPOINT_URL"

aws s3 cp \
  s3://learning-bucket/notes/sample.txt downloaded.txt \
  --endpoint-url "$AWS_ENDPOINT_URL"

cat downloaded.txt

aws s3 rm \
  s3://learning-bucket/notes/sample.txt \
  --endpoint-url "$AWS_ENDPOINT_URL"
```

### Spring Boot API से

API bucket का नाम configuration से लेती है। Object key में folders लिखना केवल
key naming है; S3 में traditional folders नहीं होते।

Upload:

```bash
curl -X PUT \
  -H 'Content-Type: text/plain' \
  --data 'Hello from Spring Boot' \
  http://localhost:8082/api/s3/objects/notes/from-api.txt
```

List:

```bash
curl http://localhost:8082/api/s3/objects
```

Download:

```bash
curl http://localhost:8082/api/s3/objects/notes/from-api.txt
```

Delete:

```bash
curl -X DELETE \
  http://localhost:8082/api/s3/objects/notes/from-api.txt
```

## 6. Java code कैसे काम करता है?

`S3Config` AWS SDK का `S3Client` बनाता है:

- `AWS_ENDPOINT_URL` set होने पर requests Floci को जाती हैं।
- `forcePathStyle(true)` local S3-compatible emulators के लिए जरूरी है।
- `AWS_DEFAULT_REGION` और credentials environment variables से आते हैं।

`S3StorageService` ये operations wrap करता है:

| Java operation | S3 operation |
| --- | --- |
| `ensureBucket()` | `HeadBucket` और जरूरत पर `CreateBucket` |
| `put()` | `PutObject` |
| `get()` | `GetObject` |
| `list()` | `ListObjectsV2` |
| `delete()` | `DeleteObject` |

## 7. Configuration बदलना

Default values local learning के लिए हैं:

```yaml
app:
  s3:
    bucket: learning-bucket
    region: us-east-1
    endpoint: http://localhost:4566
```

Container के अंदर endpoint `http://floci:4566` है, इसलिए `compose.yaml` में
`AWS_ENDPOINT_URL=http://floci:4566` दिया गया है। Host से चलाते समय endpoint
`http://localhost:4566` होना चाहिए।

Real AWS में `AWS_ENDPOINT_URL` खाली रखें और credentials को source code या YAML
में hard-code न करें। IAM role, Secrets Manager या environment-based credential
provider इस्तेमाल करें।

## 8. Useful troubleshooting

Logs:

```bash
docker compose logs -f floci
docker compose logs -f hello-world
```

अगर app bucket create नहीं कर पा रही है:

```bash
docker compose ps
curl http://localhost:4566/health
docker compose logs hello-world
```

अगर पुराने local data को साफ करके फिर शुरू करना हो:

```bash
docker compose down -v
docker compose up -d --build
```

`down -v` Floci का local bucket data delete कर देगा।

## 9. Stop और tests

```bash
docker compose down
mvn test
mvn package
```

## 10. Real AWS से मुख्य अंतर

Local Floci learning और automated testing के लिए है। Production में:

- public bucket access बंद रखें;
- least-privilege IAM policy लगाएँ;
- object names और metadata का design करें;
- encryption, versioning और lifecycle rules configure करें;
- बड़े files के लिए presigned URLs या multipart upload इस्तेमाल करें;
- credentials को Git में कभी commit न करें।