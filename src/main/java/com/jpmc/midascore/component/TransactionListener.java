package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord; // OR UserEntity - Check your file name!
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionListener.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;

    @KafkaListener(topics = "${general.kafka-topic}")
    public void listen(Transaction transaction) {
        // 1. Retrieve Sender and Recipient
        // Note: Check if your UserRepository uses findByName or findById.
        // The transaction object usually provides names or IDs.
        // Assuming findById here based on typical ID usage:
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());

        // 2. Validate Transaction
        if (sender != null && recipient != null && sender.getBalance() >= transaction.getAmount()) {

            // 3. Update Balances
            sender.setBalance(sender.getBalance() - transaction.getAmount());
            recipient.setBalance(recipient.getBalance() + transaction.getAmount());

            // 4. Save Updates to Database
            userRepository.save(sender);
            userRepository.save(recipient);

            // 5. Record the Transaction
            TransactionRecord record = new TransactionRecord(sender, recipient, transaction.getAmount());
            transactionRecordRepository.save(record);

            LOGGER.info("Transaction processed: {} -> {}", sender.getName(), recipient.getName());

            // HELPER FOR FINAL TASK: Log waldorf's balance
            if (sender.getName().equals("waldorf")) {
                LOGGER.info("WALDORF SENT MONEY. NEW BALANCE: {}", sender.getBalance());
            }
            if (recipient.getName().equals("waldorf")) {
                LOGGER.info("WALDORF RECEIVED MONEY. NEW BALANCE: {}", recipient.getBalance());
            }

        } else {
            LOGGER.info("Invalid Transaction: {}", transaction);
        }
    }
}