import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.haje01.kafka.streams.examples.FilterKeyTopology;
import com.github.haje01.kafka.streams.examples.serdes.JsonNodeSerde;


public class FilterKeyTest {

    private static String SOURCE_TOPIC = "source";
    private static String SINK_TOPIC = "sink";

    private FilterKeyTopology filterKeyTopology;
    private TopologyTestDriver testDriver;
    private TestInputTopic<String, JsonNode> sourceTopic;
    private TestOutputTopic<String, JsonNode> sinkTopic;
    private JsonNodeSerde jsonNodeSerde;

    @BeforeEach
    void setUp() {
        // 설정
        var props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");

        // 토폴로지
        filterKeyTopology = new FilterKeyTopology(SOURCE_TOPIC, SINK_TOPIC, "log_level", "ERROR", "user_id");
        var topology = filterKeyTopology.createTopology();
        testDriver = new TopologyTestDriver(topology, props);

        // Serde 와 토픽
        jsonNodeSerde = new JsonNodeSerde();
        sourceTopic = testDriver.createInputTopic(
            SOURCE_TOPIC, Serdes.String().serializer(), jsonNodeSerde.serializer());
        sinkTopic = testDriver.createOutputTopic(
            SINK_TOPIC, Serdes.String().deserializer(), jsonNodeSerde.deserializer());
    } 

    @Test
    @DisplayName("주어진 JSON 메시지에서 로그 레벨이 에러인 것은 새로운 키로 토픽에 저장하고, 아닌 것은 제외.")
    void testScenario1() {
        // JSON 데이터
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode input = objectMapper.createObjectNode();
        input.put("log_level", "ERROR");
        input.put("user_id", "user1");
        input.put("message", "something wrong");
        ObjectNode input2 = objectMapper.createObjectNode();
        input2.put("log_level", "INFO");
        input2.put("user_id", "user2");
        input2.put("message", "some information");

        // When
        sourceTopic.pipeInput(input);
        sourceTopic.pipeInput(input2);
        // Then 
        var output = sinkTopic.readRecord();
        var key = output.getKey();
        var values = output.getValue();
        // Assertions
        assertEquals("user1", key);
        assertEquals("user1", values.get("user_id").asText());
        assertEquals("ERROR", values.get("log_level").asText());
        assertEquals("something wrong", values.get("message").asText());
        assertThrows(java.util.NoSuchElementException.class, () -> {
            sinkTopic.readRecord();
        });
    }
}
