package com.jpmc.midascore.foundation;// Fixed package to match your folder

import com.jpmc.midascore.foundation.Transaction; // Fixed import to match where Transaction actually is
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionListener { // Class name now matches filename 'TransactionListener'

    // Updated .class reference to match the new class name
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionListener.class);

    @KafkaListener(topics = "${general.kafka-topic}")
    public void listen(Transaction transaction) {
        LOGGER.info("Received Transaction: {}", transaction);
    }
}