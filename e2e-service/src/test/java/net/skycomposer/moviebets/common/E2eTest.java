package net.skycomposer.moviebets.common;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import lombok.SneakyThrows;
import net.skycomposer.moviebets.bet.BetTestDataService;
import net.skycomposer.moviebets.bet.CustomerTestDataService;
import net.skycomposer.moviebets.bet.MarketTestDataService;
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
    private BetTestDataService betTestDataService;

    @Autowired
    private CustomerTestDataService customerTestDataService;

    @Autowired
    private MarketTestDataService marketTestDataService;

    @Autowired
    private KafkaClient kafkaClient;

    @BeforeEach
    @SneakyThrows
    void cleanup() {
        kafkaClient.clearMessages("bet-commands");
        kafkaClient.clearMessages("bet-settle");
        kafkaClient.clearMessages("bet-settle-job");
        kafkaClient.clearMessages("customer-commands");
        kafkaClient.clearMessages("customer-events");
        kafkaClient.clearMessages("customer-events-dlq");
        kafkaClient.clearMessages("market-commands");
        kafkaClient.clearMessages("market-close");
        mockHelper.mockCredentials(securityOauth2Username, securityOauth2Password);
        betTestDataService.resetDatabase();
        marketTestDataService.resetDatabase();
        customerTestDataService.resetDatabase();
        //TimeUnit.MILLISECONDS.sleep(Duration.ofSeconds(1).toMillis());
    }
}
