package com.jackbourner;

package com.example.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"test-topic"})
@DirtiesContext
public class KafkaOffsetCommitTest {

    private static final Logger log = LoggerFactory.getLogger(KafkaOffsetCommitTest.class);
    private static final String TOPIC = "test-topic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @SpyBean
    private YourMessageProcessor messageProcessor; // Replace with your actual processor

    @Test
    public void testOffsetCommittedWhenExceptionThrown() throws InterruptedException {
        // Mock the processor to throw an exception
        doThrow(new RuntimeException("Mocked exception"))
                .when(messageProcessor).process("test-message");

        // Create consumer to verify offset
        Consumer<String, String> consumer = consumerFactory.createConsumer();
        TopicPartition partition = new TopicPartition(TOPIC, 0);
        consumer.assign(Collections.singletonList(partition));

        try {
            // Get initial offset
            long initialOffset = consumer.position(partition);
            log.info("Initial offset: {}", initialOffset);

            // Send message
            kafkaTemplate.send(TOPIC, "test-message");
            kafkaTemplate.flush();

            // Wait for processing
            Thread.sleep(2000);

            // Verify offset advanced (message was committed despite exception)
            long finalOffset = consumer.position(partition);
            log.info("Final offset: {}", finalOffset);

            assertEquals(initialOffset + 1, finalOffset,
                    "Offset should be committed even when exception thrown");

            // Verify processor was called
            verify(messageProcessor, times(1)).process("test-message");

        } finally {
            consumer.close();
        }
    }
}
