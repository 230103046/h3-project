package kz.h3project.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String TOPIC_APPOINTMENTS = "appointment-events";

    @Bean
    public NewTopic appointmentEventsTopic() {
        return TopicBuilder.name(TOPIC_APPOINTMENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
