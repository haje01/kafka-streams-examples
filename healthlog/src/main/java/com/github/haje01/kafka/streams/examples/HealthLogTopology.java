package com.github.haje01.kafka.streams.examples;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.KStream;

public class HealthLogTopology {

    public String sourceTopic;
    public String sinkTopic;

    public HealthLogTopology(String sourceTopic, String sinkTopic) {
        this.sourceTopic = sourceTopic;
        this.sinkTopic = sinkTopic;
    }

    public Topology createTopology() {
        var streamsBuilder = new StreamsBuilder();
        var sourceStream = streamsBuilder.stream(sourceTopic, Consumed.with(Serdes.String(), Serdes.String()));

        KStream<String, String> stream = sourceStream.mapValues(value -> "Processed " + value);
        stream.to(sinkTopic, Produced.with(Serdes.String(), Serdes.String()));
        return streamsBuilder.build();
    }
}
