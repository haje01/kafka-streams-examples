#
# 빌드 단계 
#
FROM maven:3.8.1-openjdk-11-slim as build 
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests

#
# 패키지 단계
#
FROM openjdk:11-jre-slim
WORKDIR /app 
COPY --from=build /app/target/kse-multistr-0.0.5-SNAPSHOT.jar .
CMD ["java", "-jar", "kse-multistr-0.0.5-SNAPSHOT.jar"]
