package com.github.haje01.kafka.streams.examples;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;

import com.github.haje01.kafka.streams.examples.serdes.JsonNodeSerde;

public class FilterKeyTopology {

    public String sourceTopic;
    public String sinkTopic;
    public String keyField;
    public String filterField;
    public String filterValue;

    public FilterKeyTopology(String sourceTopic, String sinkTopic, String filterField, String filterValue, String keyField) {
        this.sourceTopic = sourceTopic;
        this.sinkTopic = sinkTopic;
        this.filterField = filterField;
        this.filterValue = filterValue;
        this.keyField = keyField;
    }

    public Topology createTopology() {
        var streamsBuilder = new StreamsBuilder();
        var jsonNodeSerde = new JsonNodeSerde();
        var kStream = streamsBuilder.stream(
            sourceTopic, Consumed.with(Serdes.String(), jsonNodeSerde))
            // 조건에 맞는 메시지만 필터링
            .filter((key, value) -> {
                String val = value.get(filterField).asText();
                return filterValue.equals(val);
            })
            // 키 지정 
            .selectKey((key, value) -> value.get(keyField).asText());
        kStream.to(sinkTopic, Produced.with(Serdes.String(), jsonNodeSerde));
        return streamsBuilder.build();
    }
}
