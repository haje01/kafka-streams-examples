package com.github.haje01.kafka.streams.examples;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.KStream;

public class MultiStreamTopology {

    public String sourceTopic;
    public String sourceTopic2;
    public String sinkTopic;
    public String sinkTopic2;

    public MultiStreamTopology(String sourceTopic, String sinkTopic, String sourceTopic2, String sinkTopic2) {
        this.sourceTopic = sourceTopic;
        this.sinkTopic = sinkTopic;
        this.sourceTopic2 = sourceTopic2;
        this.sinkTopic2 = sinkTopic2;
    }

    public Topology createTopology() {
        var streamsBuilder = new StreamsBuilder();
        var sourceStream = streamsBuilder.stream(sourceTopic, Consumed.with(Serdes.String(), Serdes.String()));
        var sourceStream2 = streamsBuilder.stream(sourceTopic2, Consumed.with(Serdes.String(), Serdes.String()));

        KStream<String, String> stream = sourceStream.mapValues(value -> "Processed (1) " + value);
        stream.to(sinkTopic, Produced.with(Serdes.String(), Serdes.String()));
        KStream<String, String> stream2 = sourceStream2.mapValues(value -> "Processed (2) " + value);
        stream2.to(sinkTopic2, Produced.with(Serdes.String(), Serdes.String()));
        return streamsBuilder.build();
    }
}
