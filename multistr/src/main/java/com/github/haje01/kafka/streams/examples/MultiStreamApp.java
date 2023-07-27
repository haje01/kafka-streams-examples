package com.github.haje01.kafka.streams.examples;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;


public class MultiStreamApp {

    private static final Logger logger = LoggerFactory.getLogger(MultiStreamApp.class);

    public static void main(final String[] args) {
        String broker = System.getenv("KAFKA_BROKER");
        String sourceTopic = System.getenv("KAFKA_SOURCE_TOPIC");
        String sourceTopic2 = System.getenv("KAFKA_SOURCE_TOPIC2");
        String sinkTopic = System.getenv("KAFKA_SINK_TOPIC");
        String sinkTopic2 = System.getenv("KAFKA_SINK_TOPIC2");
        logger.info("KAFKA_BROKER: " + broker);
        logger.info("KAFKA_SOURCE_TOPIC: " + sourceTopic);
        logger.info("KAFKA_SOURCE_TOPIC2: " + sourceTopic2);
        logger.info("KAFKA_SINK_TOPIC: " + sinkTopic);
        logger.info("KAFKA_SINK_TOPIC2: " + sinkTopic2);

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "multistr-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, broker);


        /*
         * 토폴로지 안에서 두 개의 소스 스트림을 가져와 싱크 스트림으로 보낸다.
         * 이 경우 두 스트림의 처리가 비슷하여 괜찮으나, 만약 처리가 상당히 다른 경우라면
         * 별도의 토폴로지를 이용하는 것이 좋다. 각 토폴로지는 독자 App ID 를 이용하고, 그것은
         * 컨슈머 그룹 ID 로 동작하여 같은 체크포인팅을 공유한다.
         */

        var multiStrTopology = new MultiStreamTopology(sourceTopic, sinkTopic, sourceTopic2, sinkTopic2);
        var topology = multiStrTopology.createTopology();
        var streams = new KafkaStreams(topology, props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
