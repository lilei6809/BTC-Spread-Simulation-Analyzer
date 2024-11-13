package com.lanlan.mock.mainpricegen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanlan.mock.core.events.BasePriceCreateEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class MasterPriceGenerator {

    private final KafkaTemplate<String, BasePriceCreateEvent> kafkaTemplate;
    private final Random rand;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private static final String LOG_MARKER = "********";

    private Double basePrice;

    @Value("${base-price-topic}")
    private String basePriceTopic;

    public MasterPriceGenerator(KafkaTemplate<String, BasePriceCreateEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        rand = new Random();
        basePrice = 30000.0;
    }

    @Scheduled(fixedRate = 1000) // 每秒生成一个价格
    public void generateBasePrice() {
        basePrice += (rand.nextDouble() * 1000 - 500);


        BasePriceCreateEvent basePriceCreateEvent = new BasePriceCreateEvent();
        basePriceCreateEvent.setPrice(basePrice);
        basePriceCreateEvent.setTimestamp(System.currentTimeMillis());

        ProducerRecord<String, BasePriceCreateEvent> producerRecord = new ProducerRecord<>(basePriceTopic, "base-price", basePriceCreateEvent);
        producerRecord.headers().add("priceId", UUID.randomUUID().toString().getBytes());

        LOGGER.info(LOG_MARKER + "base-price: " + basePriceCreateEvent);
        CompletableFuture<SendResult<String, BasePriceCreateEvent>> future = kafkaTemplate.send(basePriceTopic, "base-price", basePriceCreateEvent);

        future.thenAccept(sendResult -> {
            ProducerRecord<String, BasePriceCreateEvent> record = sendResult.getProducerRecord();
            LOGGER.info("{} Async Msg sent successfully. {},  Event: {}",
                    LOG_MARKER,
                    sendResult.getRecordMetadata(),
                    record.value()  // ProductCreateEvent
            );
        })
                .exceptionally(exception -> {
                    LOGGER.error(LOG_MARKER + "exception: " + exception.getMessage());
                    return null;
                });

    }


}
