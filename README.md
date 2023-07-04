# kafka-streams-examples

쿠버네티스 환경에서 Kafka Streams 예제를 보여준다.

## keyed 예제

- 소스 토픽에는 JSON 형태의 문자열인 메시지가 있음
- 그것을 읽어와 JSON 으로 파싱하고, 지정된 필드를 Key 로 싱크 토픽에 저장

```bash
skaffold dev -p keyed
```
