package ru.kac;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtils {

    private static final int DEFAULT_MAP_SIZE = 128;

    private JsonUtils() {
    }

    @SneakyThrows
    public static Map<String, String> jsonToMap(String strJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> result = transformJsonToMapIterative(objectMapper.readTree(strJson));

        if (log.isTraceEnabled()) {
            log.trace("[Original-Json] {}", strJson);
            for (Map.Entry<String, String> en : result.entrySet()) {
                log.trace("[Item: {}] = {}", en.getKey(), en.getValue());
            }
        }
        return result;
    }

    private static class JsonNodeWrapper {

        public JsonNode node;
        public String prefix;

        public JsonNodeWrapper(JsonNode node, String prefix) {
            this.node = node;
            this.prefix = prefix;
        }

    }

    //------------ Transform jackson JsonNode to Map -Iterative version -----------
    private static Map<String, String> transformJsonToMapIterative(JsonNode node) {
        Map<String, String> jsonMap = new HashMap<>(DEFAULT_MAP_SIZE);
        LinkedList<JsonNodeWrapper> queue = new LinkedList<>();

        //Add root of json tree to Queue
        JsonNodeWrapper root = new JsonNodeWrapper(node, "");
        queue.offer(root);

        while (queue.size() != 0) {
            JsonNodeWrapper curElement = queue.poll();
            if (curElement.node.isObject()) {
                //Add all fields (JsonNodes) to the queue
                Iterator<Map.Entry<String, JsonNode>> fieldIterator = curElement.node.fields();
                while (fieldIterator.hasNext()) {
                    Map.Entry<String, JsonNode> field = fieldIterator.next();
                    String prefix = (curElement.prefix == null || curElement.prefix.trim().length() == 0) ? "" : curElement.prefix + ".";
                    queue.offer(new JsonNodeWrapper(field.getValue(), prefix + field.getKey()));
                }
            } else if (curElement.node.isArray()) {
                //Add all array elements(JsonNodes) to the Queue
                int i = 0;
                for (JsonNode arrayElement : curElement.node) {
                    queue.offer(new JsonNodeWrapper(arrayElement, curElement.prefix + "[" + i + "]"));
                    i++;
                }
            } else {
                //If basic type, then time to fetch the Property value
                jsonMap.put(curElement.prefix, curElement.node.asText());

            }
        }
        return jsonMap;
    }
}
