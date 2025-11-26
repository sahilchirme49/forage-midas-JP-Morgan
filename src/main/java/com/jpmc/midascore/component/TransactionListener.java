package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive; // Import the new class
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate; // Import RestTemplate

@Component
public class TransactionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionListener.class);
    private final RestTemplate restTemplate = new RestTemplate(); // Initialize RestTemplate

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;

    @KafkaListener(topics = "${general.kafka-topic}")
    public void listen(Transaction transaction) {
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());

        if (sender != null && recipient != null && sender.getBalance() >= transaction.getAmount()) {

            // --- NEW LOGIC STARTS HERE ---

            // 1. Call the Incentive API
            Incentive incentive = restTemplate.postForObject(
                    "http://localhost:8080/incentive",
                    transaction,
                    Incentive.class
            );

            float incentiveAmount = incentive.getAmount();

            // 2. Update Balances
            // Sender loses the transaction amount
            sender.setBalance(sender.getBalance() - transaction.getAmount());

            // Recipient gets transaction amount + incentive
            recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);

            userRepository.save(sender);
            userRepository.save(recipient);

            // 3. Save Record (Pass the incentive to the constructor)
            TransactionRecord record = new TransactionRecord(
                    sender,
                    recipient,
                    transaction.getAmount(),
                    incentiveAmount
            );
            transactionRecordRepository.save(record);
            // --- NEW LOGIC ENDS HERE ---

            // HELPER FOR FINAL ANSWER: Check "wilbur"
            if (recipient.getName().equals("wilbur")) {
                LOGGER.info("WILBUR RECEIVED MONEY. NEW BALANCE: {}", recipient.getBalance());
            }
        }
    }
}