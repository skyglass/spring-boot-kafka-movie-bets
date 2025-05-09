package net.skycomposer.moviebets.bet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;

import jakarta.persistence.EntityManagerFactory;

@Configuration
public class TransactionConfig {

        @Bean("kafkaTransactionManager")
        KafkaTransactionManager<String, Object> kafkaTransactionManager(ProducerFactory<String, Object> producerFactory) {
                return new KafkaTransactionManager<>(producerFactory);
        }

        @Bean("transactionManager")
        @Primary
        JpaTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory) {
                return new JpaTransactionManager(entityManagerFactory);
        }

}
