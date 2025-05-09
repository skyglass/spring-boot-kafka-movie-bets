package net.skycomposer.moviebets.bet.service.handler;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.bet.service.BetService;
import net.skycomposer.moviebets.common.dto.bet.events.BetSettledEvent;
import net.skycomposer.moviebets.common.dto.customer.events.FundsSettledEvent;

@Component
@KafkaListener(topics = "${bet.settle-job.topic.name}", groupId = "${spring.kafka.consumer.bet-settle-job.group-id}")
@RequiredArgsConstructor
public class BetSettleJobHandler {

    private final BetService betService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @KafkaHandler
    public void handleEvent(@Payload FundsSettledEvent event) {
        betSettled(event.getBetId(), event.getMarketId());
    }

    @KafkaHandler
    public void handleEvent(@Payload BetSettledEvent event) {
        betSettled(event.getBetId(), event.getMarketId());
    }

    private void betSettled(UUID betId, UUID marketId) {
        betService.updateMarketSettleCount(betId, marketId);
    }

}
