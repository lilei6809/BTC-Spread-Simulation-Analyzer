package com.lanlan.mock.mainpricegen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class MasterPriceGenerator {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Random rand;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private static final String LOG_MARKER = "********";

    private Double basePrice;

    @Value("${base-price-topic}")
    private String basePriceTopic;

    public MasterPriceGenerator(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        rand = new Random();
        basePrice = 30000.0;
    }

    @Scheduled(fixedRate = 1000) // 每秒生成一个价格
    public void generateBasePrice() {
        basePrice += (rand.nextDouble() * 1000 - 500);

        Map<String, Object> priceData = new HashMap<>();
        priceData.put("basePrice", basePrice);
        priceData.put("timestamp", System.currentTimeMillis());

        try {
            ObjectMapper mapper = new ObjectMapper();
            String message = mapper.writeValueAsString(priceData);
            kafkaTemplate.send(basePriceTopic, "base-price", message);

            LOGGER.info(LOG_MARKER + "base-price: " + message);

        } catch (JsonProcessingException e) {
            LOGGER.error(LOG_MARKER + "base-price: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


}
