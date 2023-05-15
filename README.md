# Logging-lib

Library to handle logging logic

- Add integration to stream some logs on KAFKA
	- Set `logging.kafka.enable` by `true` to enable Kafka logging
	- Logging events will be published on topic "**audit_log_topic**"
	- All API request response logs will be published
	- To publish custom log add `LoggingConstants.EVENT_LOG_MARKER` to your log message

- Add logbook to have unique request and response logs for APIs

**Request**

	`2023-04-10 13:39:38.892 TRACE [counter,21f7a7fd9f029b24,21f7a7fd9f029b24] 91289 --- [nio-8080-exec-2] org.zalando.logbook.Logbook              : {"Marker":"##API_CALL##","app name":"BAZAAR","origin":"remote","api-type":"request","correlationId":"21f7a7fd9f029b24","protocol":"HTTP/1.1","remote":"0:0:0:0:0:0:0:1","method":"POST","uri":"http://localhost:8080/counter/increment/2","headers":"\"accept: */*\"|\"accept-encoding: gzip, deflate, br\"|\"connection: keep-alive\"|\"content-length: 28\"|\"content-type: application/json\"|\"host: localhost:8080\"|\"postman-token: db1c02f8-9ba8-49b2-8a7f-403a3d2e55ac\"|\"user-agent: PostmanRuntime/7.31.3\"","body":"{\"type\":\"STORE_FOLLOWER\"}"}`

**Response**

`2023-04-10 13:39:39.111 TRACE [counter,21f7a7fd9f029b24,21f7a7fd9f029b24] 91289 --- [nio-8080-exec-2] org.zalando.logbook.Logbook              : {"Marker":"##API_CALL##","app name":"BAZAAR","origin":"local","api-type":"response","correlationId":"21f7a7fd9f029b24","duration":259,"protocol":"HTTP/1.1","status":200,"headers":"\"Connection: keep-alive\"|\"Content-Type: application/json\"|\"Date: Mon, 10 Apr 2023 11:39:39 GMT\"|\"Keep-Alive: timeout=60\"|\"Transfer-Encoding: chunked\"","body":"{\"status\":200,\"timestamp\":\"2023-04-10T13:39:39.1059\",\"data\":{\"id\":\"SF-2\",\"type\":\"STORE_FOLLOWER\",\"count\":1,\"objectId\":\"2\",\"objectType\":\"STORE\"}}"}`
- Add `sleuth` integration to have calls id identifier

`[counter,21f7a7fd9f029b24,21f7a7fd9f029b24]`


## Technologies
1. Java 11+
2. Spring
2. Kafka
3. [Lombok](https://projectlombok.org/) is an annotation-based helper that saves you (or me, really) from creating many getters, setters, and constructors.

## Deployment

To publish package as private repo on GitHub

- Generate token in GitHub side
- Define the user-name and token in mvn setting
```xml
<servers>
	<server>
	<id>github</id>
	<username>user-name</username>
	<password>github-token</password>
	</server>

</servers>

```
- Run mvn deploy command to publish the lib


## USAGE

- Add lib dependency to your POM or gradle file
**Maven**
```xml
<dependency>
		<groupId>com.addon</groupId>
		<artifactId>logging-lib</artifactId>
		<version>1.0.0</version>
</dependency>
```
**Gradle**
```kotlin
implementation 'com.addon:logging-lib:1.0.0'
```

- Make sure blow props config added to application.yml file

```yaml
application:
  service-name: COUNTER_SERVICE
  service-number: 10
logging:
  topic: audit_log_topic
  kafka:
  	enable: false
  level:
	org.zalando.logbook: TRACE
  	org.apache.kafka: OFF
spring:
  kafka:
  	properties:
	  bootstrap:
	  	servers: ${KAFKA_HOST}:${KAFKA_PORT}
  	producer:
	  key-serializer: org.apache.kafka.common.serialization.StringSerializer
	  value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

logbook:
  exclude:
  	urls:
	  - "**/swagger-ui.html"
	  - "**/swagger-ui/index.html"
	  - "**/swagger-ui/**"
	  - "**/swagger-resources/**"
	  - "**/v3/api-docs/**"
	  - "**/health"
  obfuscate:
  	headers:
	  - Authorization
	  - X-Secret
  	parameters:
  	  - password
	  - token
  filter.enabled: true
  secure-filter.enabled: true
  write:
  	max-body-size: ${LOGBOOK_WRITE_MAX_BODY_SIZE:100000}
  	level: ${LOGBOOK_WRITE_LEVEL:INFO}
```

- `logbook.exclude`: has the values you want to exclude form lib logging
- `urls` -> urls paths you want to ignore
- `body` -> body variables you want to ignore
- `logbook.obfuscate`: has the values you want to obfuscate while lib logging
- `headers` -> urls paths you want to ignore
- `parameters` -> request parameters
- `paths`
