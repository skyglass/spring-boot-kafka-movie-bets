package net.skycomposer.moviebets.common;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import lombok.SneakyThrows;
import net.skycomposer.moviebets.bet.BetTestDataService;
import net.skycomposer.moviebets.client.KafkaClient;
import net.skycomposer.moviebets.config.MockHelper;

public abstract class E2eTest {

    @Value("${security.oauth2.username}")
    private String securityOauth2Username;

    @Value("${security.oauth2.password}")
    private String securityOauth2Password;

    @Autowired
    private MockHelper mockHelper;

    @Autowired
    private BetTestDataService moviebetsTestDataService;

    @Autowired
    private KafkaClient kafkaClient;

    @BeforeEach
    @SneakyThrows
    void cleanup() {
        //TODO: make sure that topics are created in kafka
        /*kafkaClient.clearMessages("bet-commands");
        kafkaClient.clearMessages("bet-events");
        kafkaClient.clearMessages("bet-settle");
        kafkaClient.clearMessages("customer-commands");
        kafkaClient.clearMessages("customer-events");
        kafkaClient.clearMessages("market-commands");
        kafkaClient.clearMessages("market-events");*/
        mockHelper.mockCredentials(securityOauth2Username, securityOauth2Password);
        moviebetsTestDataService.resetDatabase();
        //TimeUnit.MILLISECONDS.sleep(Duration.ofSeconds(1).toMillis());
    }
}
