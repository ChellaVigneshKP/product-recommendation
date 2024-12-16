package com.bigdata.ecom.products.service;

import com.bigdata.ecom.products.model.ViewedProductEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class InteractionProducer {
    private final StreamBridge streamBridge;
    private static final Logger logger = LoggerFactory.getLogger(InteractionProducer.class);
    public InteractionProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Async
    public void sendViewedProductEvent(ViewedProductEvent event, String userId) {
        String uniqueKey = userId + "-" + event.getProductId() + "-" + System.currentTimeMillis();
        logger.info("Sending viewed product event for user: {}", uniqueKey);
        Message<ViewedProductEvent> message = MessageBuilder.
                withPayload(event).
                setHeader("userId", userId).
                setHeader(KafkaHeaders.KEY, uniqueKey.getBytes()).build();
        streamBridge.send("click-events-output", message);
        logger.info("Viewed product event sent for user: {}", userId);
    }

    @Async
    public void sendSearchEvent(String query) {
        streamBridge.send("search-events-output", query);
    }
}

