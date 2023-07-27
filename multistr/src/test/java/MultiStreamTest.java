import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Properties;
import java.security.NoSuchAlgorithmException;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.test.TestRecord;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.haje01.kafka.streams.examples.MultiStreamTopology;


public class MultiStreamTest {

    private static String SOURCE_TOPIC = "source";
    private static String SOURCE_TOPIC2 = "source2";
    private static String SINK_TOPIC = "sink";
    private static String SINK_TOPIC2 = "sink2";

    private MultiStreamTopology multiStreamTopology;
    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> sourceTopic;
    private TestInputTopic<String, String> sourceTopic2;
    private TestOutputTopic<String, String> sinkTopic;
    private TestOutputTopic<String, String> sinkTopic2;

    @BeforeEach
    void setUp() {
        // 설정
        var props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");

        // 토폴로지
        multiStreamTopology = new MultiStreamTopology(SOURCE_TOPIC, SINK_TOPIC, SOURCE_TOPIC2, SINK_TOPIC2);
        var topology = multiStreamTopology.createTopology();
        testDriver = new TopologyTestDriver(topology, props);

        // 토픽
        sourceTopic = testDriver.createInputTopic(
            SOURCE_TOPIC, Serdes.String().serializer(), Serdes.String().serializer());
        sourceTopic2 = testDriver.createInputTopic(
            SOURCE_TOPIC2, Serdes.String().serializer(), Serdes.String().serializer());
        sinkTopic = testDriver.createOutputTopic(
            SINK_TOPIC, Serdes.String().deserializer(), Serdes.String().deserializer());
        sinkTopic2 = testDriver.createOutputTopic(
            SINK_TOPIC2, Serdes.String().deserializer(), Serdes.String().deserializer());
    } 

    @Test
    @DisplayName("주어진 두 토픽을 각각 처리하여 두 싱크 토픽에 저장.")
    void testScenario1() {
        // When
        sourceTopic.pipeInput("user1", "message 1");
        sourceTopic2.pipeInput("user2", "message 2");

        // Then 
        var output = sinkTopic.readRecord();
        var key = output.getKey();
        var value = output.getValue();
        System.out.println("key: " + key);
        System.out.println("value: " + value);

        var output2 = sinkTopic2.readRecord();
        var key2 = output2.getKey();
        var value2 = output2.getValue();
        System.out.println("key2: " + key2);
        System.out.println("value2: " + value2);

        // Assertions
        assertEquals("user1", key);
        assertEquals("Processed (1) message 1", value);
        assertEquals("user2", key2);
        assertEquals("Processed (2) message 2", value2);
    }
}
