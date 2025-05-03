package net.skycomposer.moviebets.bet.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

        private final static Integer TOPIC_REPLICATION_FACTOR = 3;
        private final static Integer TOPIC_PARTITIONS = 3;

        @Value("${bet.events.topic.name}")
        private String betEventsTopicName;

        @Value("${market.commands.topic.name}")
        private String marketCommandsTopicName;

        @Value("${customer.commands.topic.name}")
        private String customerCommandsTopicName;

        @Value("${bet.commands.topic.name}")
        private String betCommandsTopicName;

        @Bean
        NewTopic createBetEventsTopic() {
            return TopicBuilder.name(betEventsTopicName)
                    .partitions(TOPIC_PARTITIONS)
                    .replicas(TOPIC_REPLICATION_FACTOR)
                    .build();
        }

        @Bean
        NewTopic createMarketCommandsTopic(){
            return TopicBuilder.name(marketCommandsTopicName)
                    .partitions(TOPIC_PARTITIONS)
                    .replicas(TOPIC_REPLICATION_FACTOR)
                    .build();
        }

        @Bean
        NewTopic createCustomerCommandsTopic() {
            return TopicBuilder.name(customerCommandsTopicName)
                    .partitions(TOPIC_PARTITIONS)
                    .replicas(TOPIC_REPLICATION_FACTOR)
                    .build();
        }

        @Bean
        NewTopic createBetCommandsTopic() {
            return TopicBuilder.name(betCommandsTopicName)
                    .partitions(TOPIC_PARTITIONS)
                    .replicas(TOPIC_REPLICATION_FACTOR)
                    .build();
        }
}
