# Secrets Manager और Parameter Store सीखने का लोकल सेटअप

इस project में दोनों AWS services को Floci पर local सीखने के लिए अलग packages
में रखा गया है:

```text
src/main/java/com/example/helloworld/
├── secretsmanager/   # secrets: passwords, API keys, tokens
└── parameterstore/   # configuration और parameters
```

दोनों clients AWS SDK for Java 2.x और वही Floci endpoint use करते हैं:
`http://localhost:4566`.

## 1. Start करना

```bash
docker compose up -d --build
docker compose ps
```

App `http://localhost:8082` पर चलेगी। Local credentials केवल Floci के लिए हैं:

```text
AWS_ACCESS_KEY_ID=test
AWS_SECRET_ACCESS_KEY=test
AWS_DEFAULT_REGION=us-east-1
```

## 2. Secrets Manager

Secrets Manager sensitive values के लिए है, जैसे database password, API token या
third-party secret. Secret का value application में read किया जाता है, लेकिन
इसे सामान्य configuration की तरह expose नहीं करना चाहिए।

### Secret create करना

```bash
curl -X POST \
  -H 'Content-Type: application/json' \
  -d '{"name":"demo-secret","secretString":"password-123"}' \
  http://localhost:8082/api/secrets
```

### Secret read करना

```bash
curl http://localhost:8082/api/secrets/demo-secret
```

### Secret update करना

```bash
curl -X PUT \
  -H 'Content-Type: application/json' \
  -d '{"secretString":"password-456"}' \
  http://localhost:8082/api/secrets/demo-secret
```

### Secret delete करना

यह local learning के लिए immediate delete करता है:

```bash
curl -X DELETE http://localhost:8082/api/secrets/demo-secret
```

### AWS CLI

```bash
export AWS_ENDPOINT_URL=http://localhost:4566
export AWS_DEFAULT_REGION=us-east-1
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test

aws secretsmanager create-secret \
  --name cli-secret \
  --secret-string 'cli-password' \
  --endpoint-url "$AWS_ENDPOINT_URL"

aws secretsmanager get-secret-value \
  --secret-id cli-secret \
  --endpoint-url "$AWS_ENDPOINT_URL"
```

## 3. Parameter Store

Parameter Store application configuration के लिए useful है, जैसे feature flag,
database URL, environment name या service endpoint. यह hierarchy support करता
है, इसलिए names `/my-app/dev/database-url` जैसे रख सकते हैं।

### String parameter create/update करना

```bash
curl -X POST \
  -H 'Content-Type: application/json' \
  -d '{"name":"/my-app/dev/url","value":"http://localhost:8080","secure":false}' \
  http://localhost:8082/api/parameters
```

यह API `secure: false` पर `String` और `secure: true` पर `SecureString` बनाती है।
`POST` दोबारा चलाने पर parameter overwrite होता है।

### Parameter read करना

Normal parameter:

```bash
curl 'http://localhost:8082/api/parameters/my-app/dev/url'
```

SecureString के लिए decryption explicitly request करें:

```bash
curl 'http://localhost:8082/api/parameters/my-app/dev/password?withDecryption=true'
```

### Parameter delete करना

```bash
curl -X DELETE http://localhost:8082/api/parameters/my-app/dev/url
```

### AWS CLI

```bash
aws ssm put-parameter \
  --name /my-app/dev/url \
  --value http://localhost:8080 \
  --type String \
  --overwrite \
  --endpoint-url "$AWS_ENDPOINT_URL"

aws ssm get-parameter \
  --name /my-app/dev/url \
  --endpoint-url "$AWS_ENDPOINT_URL"
```

SecureString example:

```bash
aws ssm put-parameter \
  --name /my-app/dev/password \
  --value 'password-123' \
  --type SecureString \
  --overwrite \
  --endpoint-url "$AWS_ENDPOINT_URL"

aws ssm get-parameter \
  --name /my-app/dev/password \
  --with-decryption \
  --endpoint-url "$AWS_ENDPOINT_URL"
```

## 4. दोनों services में अंतर

| सवाल | Secrets Manager | Parameter Store |
| --- | --- | --- |
| मुख्य उपयोग | Passwords, tokens, API keys | App configuration और hierarchy |
| Rotation | Native rotation workflows | Usually application/automation managed |
| Hierarchical names | Basic naming | `/app/env/name` hierarchy मजबूत है |
| Secret value पढ़ना | `GetSecretValue` | `GetParameter` |
| Update | `PutSecretValue` | `PutParameter` with overwrite |

Rule of thumb: password या token हो तो Secrets Manager; normal environment
configuration हो तो Parameter Store. Sensitive Parameter Store values के लिए
`SecureString` चुनें और `withDecryption` केवल authorized code में use करें।

## 5. Code flow

### Secrets Manager package

| Class | काम |
| --- | --- |
| `SecretsManagerConfig` | local/real AWS client बनाता है |
| `SecretsManagerService` | create, read, update, delete operations |
| `SecretsManagerController` | `/api/secrets` REST API |

### Parameter Store package

| Class | काम |
| --- | --- |
| `ParameterStoreConfig` | SSM client बनाता है |
| `ParameterStoreService` | put, get, delete operations |
| `ParameterStoreController` | `/api/parameters` REST API |

## 6. Troubleshooting

```bash
curl http://localhost:4566/health
docker compose logs -f floci
docker compose logs -f hello-world
```

अगर नए endpoints पर 404 आएँ:

```bash
docker compose down
docker compose up -d --build --force-recreate
```

अगर dependency import error दिखे तो project root से:

```bash
mvn -B test
```

## 7. Production सावधानियाँ

`test/test` credentials केवल local Floci के लिए हैं। Real AWS में credentials
code, YAML या Git में hard-code न करें। IAM role और least-privilege policies
use करें, Secrets Manager rotation configure करें, Parameter Store SecureString
के लिए KMS permissions दें, और secret values को logs में print न करें।