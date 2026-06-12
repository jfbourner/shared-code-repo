import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.Map;
import java.util.Properties;

public class ForceOffset {
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        try (AdminClient admin = AdminClient.create(props)) {
            var offsets = Map.of(
                    new TopicPartition("your-topic", 0),
                    new OffsetAndMetadata(99999L)  // well beyond actual LEO
            );

            admin.alterConsumerGroupOffsets("your-streams-app-id", offsets)
                    .all()
                    .get();

            System.out.println("Done — offset forced to 99999");
        }
    }
}