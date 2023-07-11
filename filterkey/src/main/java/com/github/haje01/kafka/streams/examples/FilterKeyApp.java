package com.github.haje01.kafka.streams.examples;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;


public class FilterKeyApp {

    private static final Logger logger = LoggerFactory.getLogger(FilterKeyApp.class);

    public static void main(final String[] args) {
        String broker = System.getenv("KAFKA_BROKER");
        String sourceTopic = System.getenv("KAFKA_SOURCE_TOPIC");
        String sinkTopic = System.getenv("KAFKA_SINK_TOPIC");
        logger.info("KAFKA_BROKER: " + broker);
        logger.info("KAFKA_SOURCE_TOPIC: " + sourceTopic);
        logger.info("KAFKA_SINK_TOPIC: " + sinkTopic);

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "filterkey-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, broker);

        var filterKeyTopology = new FilterKeyTopology(sourceTopic, sinkTopic, "log_level", "ERROR", "user_id");
        var topology = filterKeyTopology.createTopology();
        var streams = new KafkaStreams(topology, props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
