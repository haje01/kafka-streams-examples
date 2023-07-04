# kafka-streams-examples

쿠버네티스 환경에서 Kafka Streams 예제를 보여준다.

폴더 구조는 아래와 같다.

```bash
configs/       # 프로파일별 설정 파일들
helm/          # Helm 차트
filterkey/     # filter & key 예제
producer/      # 가짜 로그 생성기
skaffold.yaml  # Skaffold 설정 파일
```

## 로그 생성기 

로그 생성기 (producer) 는 아래와 같은 가상의 로그를 발생시킨다 (기본 100개).

```json
```

## filterkey 예제

- 소스 토픽에는 JSON 형태의 문자열인 메시지가 있음
- 그것을 읽어와 JSON 으로 파싱 후
- 특정 필드의 값이 조건에 맞는 (`log_level` 이 `ERROR` 인) 것만 선택하고
- 지정된 필드 (`user_id`) 를 Key 로 싱크 토픽에 저장

개발용 실행 
```bash
skaffold dev -p filterkey
```

producer 의 로그에 남은 `Errors` 수와, 카프카의 `filterkey-sink` 토픽의 메시지 수가 일치하면 잘 동작한 것이다.