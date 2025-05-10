package net.skycomposer.moviebets.customer.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.skycomposer.moviebets.common.dto.customer.Customer;
import net.skycomposer.moviebets.common.dto.customer.WalletData;
import net.skycomposer.moviebets.common.dto.customer.WalletResponse;
import net.skycomposer.moviebets.common.dto.customer.commands.AddFundsCommand;
import net.skycomposer.moviebets.common.dto.customer.commands.RemoveFundsCommand;
import net.skycomposer.moviebets.customer.dao.entity.CustomerEntity;
import net.skycomposer.moviebets.customer.dao.entity.WalletRequestEntity;
import net.skycomposer.moviebets.customer.dao.repository.CustomerRepository;
import net.skycomposer.moviebets.customer.dao.repository.WalletRequestRepository;
import net.skycomposer.moviebets.customer.exception.CustomerInsufficientFundsException;
import net.skycomposer.moviebets.customer.exception.CustomerNotFoundException;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final String FUNDS_ADDED_SUCCESSFULLY = "Funds successfully increased from %.4f to %.4f";

    private static final String FUNDS_REMOVED_SUCCESSFULLY = "Funds successfully decreased from %.4f to %.4f";

    private static final String FUNDS_QUEUED_FOR_ADDING_SUCCESSFULLY = "Funds successfully queued for increasing from %.4f to %.4f";

    private static final String FUNDS_QUEUED_FOR_REMOVAL_SUCCESSFULLY = "Funds successfully queued for decreasing from %.4f to %.4f";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CustomerRepository customerRepository;

    private final WalletRequestRepository walletRequestRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String customerCommandsTopicName;

    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            WalletRequestRepository walletRequestRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${customer.commands.topic.name}") String customerCommandsTopicName
    ) {
        this.customerRepository = customerRepository;
        this.walletRequestRepository = walletRequestRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.customerCommandsTopicName = customerCommandsTopicName;
    }

    @Override
    @Transactional
    public WalletResponse addFundsAsync(String customerId, UUID requestId, BigDecimal funds) {
        CustomerEntity customerEntity = createEntityIfNotExists(customerId);
        BigDecimal currentBalance = customerEntity.getBalance();
        BigDecimal newBalance = currentBalance.add(funds);
        if (walletRequestRepository.existsByRequestId(requestId)) {
            String message = String.format("Duplicate addFunds request for customer %s, requestId = %s", customerId, requestId);
            logger.warn(message);
            return new WalletResponse(message,
                    customerEntity.getUsername(),
                    currentBalance);
        } else {
            AddFundsCommand addFundsCommand = new AddFundsCommand(
                    customerId,
                    requestId,
                    funds);
            kafkaTemplate.send(customerCommandsTopicName, addFundsCommand.getCustomerId(), addFundsCommand);
            var message = FUNDS_QUEUED_FOR_ADDING_SUCCESSFULLY.formatted(currentBalance, newBalance);
            logger.info(message);
            return new WalletResponse(message, customerId, newBalance);
        }
    }

    @Override
    @Transactional
    public WalletResponse addFunds(String customerId, UUID requestId, BigDecimal funds) {
        CustomerEntity customerEntity = createEntityIfNotExists(customerId);
        BigDecimal currentBalance = customerEntity.getBalance();
        BigDecimal newBalance = currentBalance.add(funds);
        if (walletRequestRepository.existsByRequestId(requestId)) {
            String message = String.format("Duplicate addFunds request for customer %s, requestId = %s", customerId, requestId);
            logger.warn(message);
            return new WalletResponse(message,
                    customerEntity.getUsername(),
                    currentBalance);
        } else {
            customerEntity.setBalance(newBalance);
            customerRepository.save(customerEntity);
            walletRequestRepository.save(
                    WalletRequestEntity
                    .builder()
                    .requestId(requestId)
                    .build());
            var message = FUNDS_ADDED_SUCCESSFULLY.formatted(currentBalance, newBalance);
            logger.info(message);
            return new WalletResponse(message, customerId, newBalance);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse removeFundsAsync(String customerId, UUID requestId, BigDecimal funds) {
        CustomerEntity customerEntity = createEntityIfNotExists(customerId);
        BigDecimal currentBalance = customerEntity.getBalance();
        BigDecimal newBalance = currentBalance.subtract(funds);
        if (walletRequestRepository.existsByRequestId(requestId)) {
            String message = String.format("Duplicate removeFunds request for customer %s, requestId = %s", customerId, requestId);
            logger.warn(message);
            return new WalletResponse(message,
                    customerEntity.getUsername(),
                    currentBalance);
        } else {
            RemoveFundsCommand removeFundsCommand = new RemoveFundsCommand(
                    customerId,
                    requestId,
                    funds);
            kafkaTemplate.send(customerCommandsTopicName, customerId, removeFundsCommand);
            var message = FUNDS_QUEUED_FOR_REMOVAL_SUCCESSFULLY.formatted(currentBalance, newBalance);
            logger.info(message);
            return new WalletResponse(message, customerId, newBalance);
        }
    }

    @Override
    @Transactional
    public WalletResponse removeFunds(String customerId, UUID requestId, BigDecimal funds) {
        CustomerEntity customerEntity = createEntityIfNotExists(customerId);
        BigDecimal currentBalance = customerEntity.getBalance();
        BigDecimal newBalance = currentBalance.subtract(funds);
        if (newBalance.signum() == -1) {
            throw new CustomerInsufficientFundsException(customerId, funds, currentBalance);
        }
        if (walletRequestRepository.existsByRequestId(requestId)) {
            String message = String.format("Duplicate removeFunds request for customer %s, requestId = %s", customerId, requestId);
            logger.warn(message);
            return new WalletResponse(message,
                    customerEntity.getUsername(),
                    currentBalance);
        } else {
            customerEntity.setBalance(newBalance);
            customerRepository.save(customerEntity);
            walletRequestRepository.save(WalletRequestEntity.builder()
                    .requestId(requestId)
                    .build());
            var message = FUNDS_REMOVED_SUCCESSFULLY.formatted(currentBalance, newBalance);
            logger.info(message);
            return new WalletResponse(message, customerId, newBalance);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Customer findCustomerById(String customerId) {
        CustomerEntity customerEntity = customerRepository.findByUsername(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        return Customer.builder()
                .username(customerEntity.getUsername())
                .fullName(customerEntity.getFullName())
                .balance(customerEntity.getBalance())
                .build();
    }

    private CustomerEntity createEntityIfNotExists(String customerId) {
        Optional<CustomerEntity> customerOptional = customerRepository.findByUsername(customerId);
        if (customerOptional.isPresent()) {
            return customerOptional.get();
        }
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setUsername(customerId);
        customerEntity.setFullName(customerId);
        customerEntity.setBalance(BigDecimal.ZERO);
        return customerEntity;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return customerRepository.findAll().stream()
                .map(entity -> new Customer(entity.getId(), entity.getUsername(), entity.getFullName(), entity.getBalance()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WalletData findWalletById(String customerId) {
        Customer customer = findCustomerById(customerId);
        return new WalletData(customerId, customer.getBalance());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletData> findAllWallets() {
        return customerRepository.findAll().stream()
                .map(entity -> new WalletData(entity.getUsername(), entity.getBalance()))
                .collect(Collectors.toList());
    }
}
