package net.skycomposer.betting.customer.service;

import lombok.RequiredArgsConstructor;
import net.skycomposer.betting.common.domain.dto.customer.Customer;
import net.skycomposer.betting.common.domain.dto.customer.WalletData;
import net.skycomposer.betting.common.domain.dto.customer.WalletResponse;
import net.skycomposer.betting.customer.dao.entity.CustomerEntity;
import net.skycomposer.betting.customer.dao.entity.WalletRequestEntity;
import net.skycomposer.betting.customer.dao.repository.CustomerRepository;
import net.skycomposer.betting.customer.dao.repository.WalletRequestRepository;
import net.skycomposer.betting.customer.exception.CustomerInsufficientFundsException;
import net.skycomposer.betting.customer.exception.CustomerNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private static final String FUNDS_ADDED_SUCCESSFULLY = "Funds successfully increased from %.4f to %.4f";

    private static final String FUNDS_REMOVED_SUCCESSFULLY = "Funds successfully decreased from %.4f to %.4f";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CustomerRepository customerRepository;

    private final WalletRequestRepository walletRequestRepository;

    @Override
    @Transactional
    public WalletResponse addFunds(String customerId, String requestId, BigDecimal funds) {
        CustomerEntity customerEntity = saveEntityIfNotExists(customerId, funds);
        BigDecimal currentBalance = customerEntity.getBalance();
        BigDecimal newBalance = currentBalance.add(funds);
        if (walletRequestRepository.existsById(UUID.fromString(requestId))) {
            String message = String.format("Duplicate addFunds request for customer %s, requestId = %s", customerId, requestId);
            logger.warn(message);
            return new WalletResponse(message,
                    customerEntity.getUsername(),
                    currentBalance);
        } else {
            customerEntity.setBalance(newBalance);
            customerRepository.save(customerEntity);
            walletRequestRepository.save(new WalletRequestEntity(UUID.fromString(requestId)));
            return new WalletResponse(FUNDS_ADDED_SUCCESSFULLY.formatted(currentBalance, newBalance),
                    customerEntity.getUsername(),
                    newBalance);
        }
    }

    @Override
    @Transactional
    public WalletResponse removeFunds(String customerId, String requestId, BigDecimal funds) {
        CustomerEntity customerEntity = customerRepository.findByUsername(customerId).get();
        if (customerEntity == null) {
            throw new CustomerNotFoundException(customerId);
        }
        BigDecimal currentBalance = customerEntity.getBalance();
        if (walletRequestRepository.existsById(UUID.fromString(requestId))) {
            String message = String.format("Duplicate removeFunds request for customer %s, requestId = %s", customerId, requestId);
            logger.warn(message);
            return new WalletResponse(message,
                    customerEntity.getUsername(),
                    currentBalance);
        } else {
            BigDecimal newBalance = currentBalance.subtract(funds);
            if (newBalance.signum() == -1) {
                throw new CustomerInsufficientFundsException(customerId, funds, currentBalance);
            }
            customerEntity.setBalance(newBalance);
            customerRepository.save(customerEntity);
            walletRequestRepository.save(new WalletRequestEntity(UUID.fromString(requestId)));
            return new WalletResponse(FUNDS_REMOVED_SUCCESSFULLY.formatted(currentBalance, newBalance),
                    customerEntity.getUsername(),
                    newBalance);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Customer findCustomerById(String customerId) {
        CustomerEntity customerEntity = customerRepository.findByUsername(customerId).get();
        if (customerEntity == null) {
            throw new CustomerNotFoundException(customerId);
        }
        return Customer.builder()
                .username(customerEntity.getUsername())
                .fullName(customerEntity.getFullName())
                .balance(customerEntity.getBalance())
                .build();
    }

    private CustomerEntity saveEntityIfNotExists(String customerId, BigDecimal balance) {
        Optional<CustomerEntity> customerOptional = customerRepository.findByUsername(customerId);
        if (customerOptional.isPresent()) {
            return customerOptional.get();
        }
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setUsername(customerId);
        customerEntity.setFullName(customerId);
        customerEntity.setBalance(balance);
        customerRepository.save(customerEntity);
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
    public WalletData findWalletById(String customerId) {
        Customer customer = findCustomerById(customerId);
        return new WalletData(customerId, customer.getBalance());
    }

    @Override
    public List<WalletData> findAllWallets() {
        return customerRepository.findAll().stream()
                .map(entity -> new WalletData(entity.getUsername(), entity.getBalance()))
                .collect(Collectors.toList());
    }
}
