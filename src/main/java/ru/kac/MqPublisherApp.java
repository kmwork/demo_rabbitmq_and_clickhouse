package ru.kac;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MqPublisherApp {

    static final int MESSAGE_COUNT = 7000;


    public static void main(String[] args) throws Exception {
        publishMessagesInBatch();
    }

    static void publishMessagesInBatch() throws Exception {
        try (Connection connection = MqUtils.createMqConnection()) {
            Channel ch = connection.createChannel();
            String queue = MqUtils.getQueue();
            Map<String, Object> arguments = MqUtils.getMqArguments();
            ch.queueDeclare(queue, true, false, false, arguments);

            ch.confirmSelect();

            int batchSize = 100;

            long start = System.nanoTime();
            for (int i = 0; i < MESSAGE_COUNT; i++) {
                long value = System.nanoTime();


                String body = String.format("{\n" +
                        "\"key\": %d,\n" +
                        "\"value\": %d \n" +
                        "}", value, value);
                ch.basicPublish("", queue, null, body.getBytes());
            }

            long end = System.nanoTime();
            String msq = String.format("Published %,d messages in batch in %,d ms%n", MESSAGE_COUNT, Duration.ofNanos(end - start).toMillis());
            log.info(msq);
        }
    }

}
