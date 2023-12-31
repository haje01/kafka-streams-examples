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
import com.github.haje01.kafka.streams.examples.HashSplitTopology;
import com.github.haje01.kafka.streams.examples.serdes.JsonNodeSerde;


public class HashSplitTest {

    private static String SOURCE_TOPIC = "source";
    private static String SINK_TOPIC = "sink";
    private static String SINK_TOPIC2 = "sink2";

    private HashSplitTopology hashSplitTopology;
    private TopologyTestDriver testDriver;
    private TestInputTopic<String, JsonNode> sourceTopic;
    private TestOutputTopic<String, String> sinkTopic;
    private TestOutputTopic<String, JsonNode> sinkTopic2;
    private JsonNodeSerde jsonNodeSerde;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        // 설정
        var props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");

        // 토폴로지
        hashSplitTopology = new HashSplitTopology(SOURCE_TOPIC, SINK_TOPIC, SINK_TOPIC2);
        var topology = hashSplitTopology.createTopology();
        testDriver = new TopologyTestDriver(topology, props);

        // Serde 와 토픽
        jsonNodeSerde = new JsonNodeSerde();
        sourceTopic = testDriver.createInputTopic(
            SOURCE_TOPIC, Serdes.String().serializer(), jsonNodeSerde.serializer());
        sinkTopic = testDriver.createOutputTopic(
            SINK_TOPIC, Serdes.String().deserializer(), Serdes.String().deserializer());
        sinkTopic2 = testDriver.createOutputTopic(
            SINK_TOPIC2, Serdes.String().deserializer(), jsonNodeSerde.deserializer());
    } 

    @Test
    @DisplayName("주어진 JSON 메시지에서 userIdHash + userId 및 userIdHash + 내용의 두 토픽으로 분리.")
    void testScenario1() {
        // JSON 데이터
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode input = objectMapper.createObjectNode();
        input.put("user_id", "user1");
        input.put("message", "some chat message");
        ObjectNode input2 = objectMapper.createObjectNode();
        input2.put("user_id", "user2");
        input2.put("message", "other chat message");

        // When
        sourceTopic.pipeInput("user1", input);
        sourceTopic.pipeInput("user2", input2);

        // Then 
        var output = sinkTopic.readRecord();
        var key = output.getKey();
        var value = output.getValue();
        System.out.println("key: " + key);
        System.out.println("value: " + value);

        // Assertions
        assertEquals("b3daa77b4c04", key);
        assertEquals("user1", value);

        var output2 = sinkTopic2.readRecord();
        var key2 = output2.getKey();
        var value2 = output2.getValue();
        System.out.println("key2: " + key2);
        System.out.println("value2: " + value2);

        // Assertions
        assertEquals("b3daa77b4c04", key2);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("message", "some chat message");
        assertEquals(root, value2);
    }
}
