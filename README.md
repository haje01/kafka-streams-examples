# kafka-streams-examples

쿠버네티스 환경에서 Kafka Streams 예제들을 보여준다. 
- 자체 Kafka 클러스터 환경
- 가상의 로그 생성기 
- Maven 으로 Java 코드 빌드 및 의존성 해결 
- SLF4J + Logback 으로 로깅
- JUnit 으로 단위 테스트 

폴더 구조는 아래와 같다.

```bash
configs/       # 프로파일별 설정 파일들
helm/          # Helm 차트
filterkey/     # 필터링 + 키지정 예제
hashsplit/     # 해슁 + 스트림 분리 예제 
multistr/      # 멀티 스트림 예제 
healthlog/     # 헬스 체크 + 로깅 예제 
producer/      # 가짜 로그 생성기
skaffold.yaml  # Skaffold 설정 파일
```

`filterkey`, `hashsplit`, `multistr`, `healthlog` 가 각각 하나의 스트림즈 앱 예제이다. 

> 앞으로 다음과 같은 스트림즈 예제가 추가될 수 있다.
> - 스트림 - 스트림을 조인하는 `joinstrstr`
> - 스트림 - 테이블을 조인하는 `joinstrtbl`
> - 테이블 - 테이블을 조인하는 `jointbltbl`
> - 스트림 - 전역 테이블을 조인하는 `joinstrgtbl`

## 변수값 

Helm 차트 기본 변수값은 아래와 같다. 

```yaml
# 카프카 설정
kafka: 
  # 토픽 기본 파티션 수
  numPartitions: 2
  srcTopic: default-src
  srcTopic2: ""

# 로그 생성기 설정
producer:
  enabled: true
  image:
    registry: ""
    repository: "library/kse-producer"
    tag: 0.0.5
  # 로그 생성 타입
  type: default

# filterkey 예제 
filterkey:
  enabled: false
  # 파드 수
  replicas: 2
  image:
    registry: ""
    repository: "library/kse-filterkey"
    tag: 0.0.5
  sinkTopic: filterkey-sink

# hashsplit 예제 
hashsplit:
  enabled: false
  # 파드 수
  replicas: 1
  image:
    registry: ""
    repository: "library/kse-hashsplit"
    tag: 0.0.5
  sinkTopic: hashsplit-sink
  sinkTopic2: hashsplit-sink2

# multistr 예제 
multistr:
  enabled: false 
  # 파드 수
  replicas: 1
  image:
    registry: ""
    repository: "library/kse-multistr"
    tag: 0.0.5
  sinkTopic: multistr-sink
  sinkTopic2: multistr-sink2

# healthlog 예제 
healthlog:
  enabled: false 
  # 파드 수
  replicas: 1
  image:
    registry: ""
    repository: "library/kse-healthlog"
    tag: 0.0.5
  sinkTopic: healthlog-sink
  sinkTopic2: healthlog-sink2
```

## 로그 생성기 (producer)

로그 생성기는 대상 토픽에 아래와 같은 가상의 로그를 발생시킨다 (기본 100개).

```json
{"user_id": "ID29938", "timestamp": 1688460924.2553785, "datetime": "2023-07-04 08:55:24.255", "log_level": "WARN", "message": "Choose this best true white movie Democrat major Democrat wide seat race."}
```

- 로그 레벨 (`log_level`) 은 `INFO`, `WARN`, `ERROR` 의 세 가지 종류이다.
- 로그 생성기는 매 예제별로 따로 만들 필요가 없을 듯 하여 `type` 정보를 통해 예제 앱이 필요로 하는 로그 패턴을 생성하도록 구현한다.

## 필터 + 키 (filterkey) 예제

- 소스 토픽 (`filterkey-src`) 에는 JSON 형태의 문자열인 메시지가 있음
- 그것을 읽어와 JSON 으로 파싱 후
- 특정 필드의 값이 조건에 맞는 (`log_level` 이 `ERROR` 인) 것만 선택하고
- 지정된 필드 (`user_id`) 를 Key 로 싱크 토픽 (`filterkey-sink`) 에 저장
- 기본 `replicas` 값은 `2`

개발용 실행 
```bash
skaffold dev -p filterkey
```

producer 의 로그에 남은 `Errors` 수와, 싱크 토픽 (`filterkey-sink`) 의 메시지 수가 일치하면 성공한 것이다.

## 카를 해슁하고 두 스트림으로 나누는 (hashsplit) 예제

개인 정보 보호 등의 이유로 하나의 스트림에서 키 정보를 해슁하여 다음의 두 스트림을 나누는 예제이다.
- 스트림 A: 유저 ID 해쉬, 유저 ID
- 스트림 B: 유저 ID 해쉬, 메시지 본문 

개발용 실행 
```bash
skaffold dev -p hashsplit
```

## 멀티 스트림 (multistr) 예제

- 하나의 Kafka Streams 앱에서 두 개의 소스 스트림을 읽어와 각각의 싱크 스트림에 저장하는 예제
- 예제는 처리하는 과정이 비슷하기에 하나의 토폴로지를 이용하여 구현했다.
- 만약 처리과정이 다른 경우라면 토폴로지를 나누어서 이용하는 것이 바람직하다.
- 각 토폴로지별로 App ID 가 부여되고, 그것은 컨슈머 그룹 ID 로 사용되기에 체크 포인팅을 공유한다.

개발용 실행 
```bash
skaffold dev -p multistr
```

## 헬스 체크 및 로깅 (healthlog) 예제 

쿠버네티스의 `livenessProbe` 를 위한 헬스 체크 서버 구현 및 slf4j 와 logback 을 이용한 로깅을 보여주는 예제이다.

개발용 실행 
```bash
skaffold dev -p healthlog
```
