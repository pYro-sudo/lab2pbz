package by.losik.service;

import by.losik.entity.Customer;
import by.losik.repository.CustomerRepository;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.quarkus.cache.CacheKey;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@ApplicationScoped
@Slf4j
public class CustomerService extends BaseService<Customer, CustomerRepository> {

    @Inject
    CustomerRepository customerRepository;

    @Override
    protected String getEntityName() {
        return "Customer";
    }

    @Override
    protected String getCachePrefix() {
        return "customer";
    }

    @Inject
    public void setRepository(CustomerRepository repository) {
        this.repository = repository;
    }

    @CacheResult(cacheName = "customer-by-legal-status")
    public Uni<List<Customer>> findByLegalEntityStatus(@CacheKey Boolean isLegalEntity) {
        log.info("Finding customers by legal entity status: {}", isLegalEntity);
        return customerRepository.findByLegalEntityStatus(isLegalEntity)
                .onItem().transform(customers -> {
                    log.debug("Found {} customers with legal entity status: {}", customers.size(), isLegalEntity);
                    return customers;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding customers by legal entity status: {}", isLegalEntity, throwable));
    }

    @CacheResult(cacheName = "customer-by-bank-name")
    public Uni<List<Customer>> findByBankName(@CacheKey String bankName) {
        log.info("Finding customers by bank name: {}", bankName);
        return customerRepository.findByBankName(bankName)
                .onItem().transform(customers -> {
                    log.debug("Found {} customers with bank name: {}", customers.size(), bankName);
                    return customers;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding customers by bank name: {}", bankName, throwable));
    }

    @CacheResult(cacheName = "customer-by-document")
    public Uni<List<Customer>> findByDocument(@CacheKey String documentSeries, @CacheKey String documentNumber) {
        log.info("Finding customers by document: series={}, number={}", documentSeries, documentNumber);
        return customerRepository.findByDocument(documentSeries, documentNumber)
                .onItem().transform(customers -> {
                    log.debug("Found {} customers with document: series={}, number={}",
                            customers.size(), documentSeries, documentNumber);
                    return customers;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding customers by document: series={}, number={}",
                                documentSeries, documentNumber, throwable));
    }

    public Uni<List<Customer>> findByLegalEntityPaginated(Boolean isLegalEntity, int pageIndex, int pageSize) {
        log.info("Finding customers by legal entity paginated, isLegalEntity: {}, pageIndex: {}, pageSize: {}",
                isLegalEntity, pageIndex, pageSize);
        return customerRepository.findByLegalEntityPaginated(isLegalEntity, pageIndex, pageSize)
                .onItem().transform(customers -> {
                    log.debug("Found {} customers for legal entity paginated query", customers.size());
                    return customers;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding customers by legal entity paginated, isLegalEntity: {}, pageIndex: {}, pageSize: {}",
                                isLegalEntity, pageIndex, pageSize, throwable));
    }

    @CacheResult(cacheName = "customer-count-by-legal-status")
    public Uni<Long> countByLegalEntityStatus(@CacheKey Boolean isLegalEntity) {
        log.info("Counting customers by legal entity status: {}", isLegalEntity);
        return customerRepository.countByLegalEntityStatus(isLegalEntity)
                .onItem().transform(count -> {
                    log.debug("Found {} customers with legal entity status: {}", count, isLegalEntity);
                    return count;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error counting customers by legal entity status: {}", isLegalEntity, throwable));
    }

    @CacheResult(cacheName = "customer-exists-by-document")
    public Uni<Boolean> existsByDocument(@CacheKey String documentSeries, @CacheKey String documentNumber) {
        log.info("Checking if customer exists by document: series={}, number={}", documentSeries, documentNumber);
        return customerRepository.existsByDocument(documentSeries, documentNumber)
                .onItem().transform(exists -> {
                    log.debug("Customer exists by document: series={}, number={}: {}",
                            documentSeries, documentNumber, exists);
                    return exists;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error checking if customer exists by document: series={}, number={}",
                                documentSeries, documentNumber, throwable));
    }

    public Uni<Integer> updateCustomerAddress(Long id, String newAddress) {
        log.info("Updating customer address, id: {}, newAddress: {}", id, newAddress);
        return customerRepository.updateCustomerAddress(id, newAddress)
                .onItem().transform(updatedCount -> {
                    log.info("Updated address for {} customers with id: {}", updatedCount, id);
                    invalidateRelatedCaches();
                    return updatedCount;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error updating customer address, id: {}, newAddress: {}",
                                id, newAddress, throwable));
    }

    public Uni<Integer> updateBankDetails(Long id, String bankName, String bankAccount) {
        log.info("Updating bank details for customer id: {}, bankName: {}, bankAccount: {}",
                id, bankName, bankAccount);
        return customerRepository.updateBankDetails(id, bankName, bankAccount)
                .onItem().transform(updatedCount -> {
                    log.info("Updated bank details for {} customers with id: {}", updatedCount, id);
                    invalidateRelatedCaches();
                    return updatedCount;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error updating bank details for customer id: {}, bankName: {}, bankAccount: {}",
                                id, bankName, bankAccount, throwable));
    }

    @CacheResult(cacheName = "customer-legal-entities-with-bank")
    public Uni<List<Customer>> findLegalEntitiesWithBankAccounts() {
        log.info("Finding legal entities with bank accounts");
        return customerRepository.findLegalEntitiesWithBankAccounts()
                .onItem().transform(customers -> {
                    log.debug("Found {} legal entities with bank accounts", customers.size());
                    return customers;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding legal entities with bank accounts", throwable));
    }

    @CacheResult(cacheName = "customer-individuals-with-docs")
    public Uni<List<Customer>> findIndividualsWithDocuments() {
        log.info("Finding individuals with documents");
        return customerRepository.findIndividualsWithDocuments()
                .onItem().transform(customers -> {
                    log.debug("Found {} individuals with documents", customers.size());
                    return customers;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding individuals with documents", throwable));
    }

    public Uni<Long> getPageCountByName(String namePattern, int pageSize) {
        log.info("Calculating page count by name pattern: {}, page size: {}", namePattern, pageSize);
        return customerRepository.getPageCountByName(namePattern, pageSize)
                .onItem().transform(pageCount -> {
                    log.debug("Page count by name pattern: {} for page size: {}", pageCount, pageSize);
                    return pageCount;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error calculating page count by name pattern: {}",
                                namePattern, throwable));
    }

    public Uni<Integer> markAllLegalEntitiesWithBank(String bankName) {
        log.info("Marking all legal entities with bank name: {}", bankName);
        return customerRepository.markAllLegalEntitiesWithBank(bankName)
                .onItem().transform(updatedCount -> {
                    log.info("Marked {} legal entities with bank name: {}", updatedCount, bankName);
                    invalidateRelatedCaches();
                    return updatedCount;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error marking all legal entities with bank name: {}", bankName, throwable));
    }

    @CacheResult(cacheName = "customer-stats-by-legal-entity")
    public Uni<List<Customer>> findCustomerStatsByLegalEntity() {
        log.info("Finding customer statistics by legal entity");
        return customerRepository.findCustomerStatsByLegalEntity()
                .onItem().transform(stats -> {
                    log.debug("Found customer statistics by legal entity: {} entries", stats.size());
                    return stats;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding customer statistics by legal entity", throwable));
    }

    // Override base methods for customer-specific behavior
    @Override
    public Uni<Customer> save(Customer customer) {
        log.info("Saving customer: {}", customer.getName());
        return super.save(customer)
                .onItem().invoke(savedCustomer ->
                        log.info("Successfully saved customer: {} with id: {}",
                                savedCustomer.getName(), savedCustomer.getId()));
    }

    @Override
    public Uni<Customer> update(Customer customer) {
        log.info("Updating customer: {} with id: {}", customer.getName(), customer.getId());
        return super.update(customer)
                .onItem().invoke(updatedCustomer ->
                        log.info("Successfully updated customer: {} with id: {}",
                                updatedCustomer.getName(), updatedCustomer.getId()));
    }

    @Scheduled(cron = "0 0 * * * ?")
    @CacheInvalidateAll(cacheName = "customer-by-id")
    @CacheInvalidateAll(cacheName = "customer-by-name")
    @CacheInvalidateAll(cacheName = "customer-all-sorted")
    @CacheInvalidateAll(cacheName = "customer-exists-by-name")
    @CacheInvalidateAll(cacheName = "customer-exists-by-document")
    @CacheInvalidateAll(cacheName = "customer-count-all")
    @CacheInvalidateAll(cacheName = "customer-by-legal-status")
    @CacheInvalidateAll(cacheName = "customer-by-bank-name")
    @CacheInvalidateAll(cacheName = "customer-by-document")
    @CacheInvalidateAll(cacheName = "customer-count-by-legal-status")
    @CacheInvalidateAll(cacheName = "customer-legal-entities-with-bank")
    @CacheInvalidateAll(cacheName = "customer-individuals-with-docs")
    @CacheInvalidateAll(cacheName = "customer-stats-by-legal-entity")
    public void scheduledCacheInvalidation() {
        log.debug("Scheduled cache invalidation for CustomerService");
    }

    @Override
    protected void invalidateRelatedCaches() {
        scheduledCacheInvalidation();
    }
}