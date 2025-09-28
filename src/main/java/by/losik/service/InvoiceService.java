package by.losik.service;

import by.losik.entity.Invoice;
import by.losik.entity.Customer;
import by.losik.entity.Settlement;
import by.losik.repository.InvoiceRepository;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.quarkus.cache.CacheKey;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@ApplicationScoped
@Slf4j
public class InvoiceService extends BaseService<Invoice, InvoiceRepository> {

    @Inject
    InvoiceRepository invoiceRepository;

    @Override
    protected String getEntityName() {
        return "Invoice";
    }

    @Override
    protected String getCachePrefix() {
        return "invoice";
    }

    @Inject
    public void setRepository(InvoiceRepository repository) {
        this.repository = repository;
    }

    @CacheResult(cacheName = "invoice-by-customer")
    public Uni<List<Invoice>> findByCustomer(@CacheKey Customer customer) {
        log.info("Finding invoices by customer: {}", customer.getId());
        return invoiceRepository.findByCustomer(customer)
                .onItem().transform(invoices -> {
                    log.debug("Found {} invoices for customer: {}", invoices.size(), customer.getId());
                    return invoices;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoices by customer: {}", customer.getId(), throwable));
    }

    @CacheResult(cacheName = "invoice-by-customer-id")
    public Uni<List<Invoice>> findByCustomerId(@CacheKey Long customerId) {
        log.info("Finding invoices by customer id: {}", customerId);
        return invoiceRepository.findByCustomerId(customerId)
                .onItem().transform(invoices -> {
                    log.debug("Found {} invoices for customer id: {}", invoices.size(), customerId);
                    return invoices;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoices by customer id: {}", customerId, throwable));
    }

    @CacheResult(cacheName = "invoice-by-settlement")
    public Uni<List<Invoice>> findBySettlement(@CacheKey Settlement settlement) {
        log.info("Finding invoices by settlement: {}", settlement.getId());
        return invoiceRepository.findBySettlement(settlement)
                .onItem().transform(invoices -> {
                    log.debug("Found {} invoices for settlement: {}", invoices.size(), settlement.getId());
                    return invoices;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoices by settlement: {}", settlement.getId(), throwable));
    }

    @CacheResult(cacheName = "invoice-by-settlement-id")
    public Uni<List<Invoice>> findBySettlementId(@CacheKey Long settlementId) {
        log.info("Finding invoices by settlement id: {}", settlementId);
        return invoiceRepository.findBySettlementId(settlementId)
                .onItem().transform(invoices -> {
                    log.debug("Found {} invoices for settlement id: {}", invoices.size(), settlementId);
                    return invoices;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoices by settlement id: {}", settlementId, throwable));
    }

    @CacheResult(cacheName = "invoice-by-date")
    public Uni<List<Invoice>> findByDate(@CacheKey Date date) {
        log.info("Finding invoices by date: {}", date);
        return invoiceRepository.findByDate(date)
                .onItem().transform(invoices -> {
                    log.debug("Found {} invoices for date: {}", invoices.size(), date);
                    return invoices;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoices by date: {}", date, throwable));
    }

    @CacheResult(cacheName = "invoice-by-date-range")
    public Uni<List<Invoice>> findByDateRange(@CacheKey Date startDate, @CacheKey Date endDate) {
        log.info("Finding invoices by date range: {} to {}", startDate, endDate);
        return invoiceRepository.findByDateRange(startDate, endDate)
                .onItem().transform(invoices -> {
                    log.debug("Found {} invoices for date range: {} to {}", invoices.size(), startDate, endDate);
                    return invoices;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoices by date range: {} to {}", startDate, endDate, throwable));
    }

    public Uni<List<Invoice>> findByDateBefore(Date date) {
        log.info("Finding invoices before date: {}", date);
        return invoiceRepository.findByDateBefore(date)
                .onItem().transform(invoices -> {
                    log.debug("Found {} invoices before date: {}", invoices.size(), date);
                    return invoices;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoices before date: {}", date, throwable));
    }

    public Uni<List<Invoice>> findByDateAfter(Date date) {
        log.info("Finding invoices after date: {}", date);
        return invoiceRepository.findByDateAfter(date)
                .onItem().transform(invoices -> {
                    log.debug("Found {} invoices after date: {}", invoices.size(), date);
                    return invoices;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoices after date: {}", date, throwable));
    }

    @CacheResult(cacheName = "invoice-by-enterprise")
    public Uni<List<Invoice>> findByEnterprise(@CacheKey String enterprise) {
        log.info("Finding invoices by enterprise: {}", enterprise);
        return invoiceRepository.findByEnterprise(enterprise)
                .onItem().transform(invoices -> {
                    log.debug("Found {} invoices for enterprise: {}", invoices.size(), enterprise);
                    return invoices;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoices by enterprise: {}", enterprise, throwable));
    }

    public Uni<List<Invoice>> findByEnterpriseContaining(String enterprisePart) {
        log.info("Finding invoices by enterprise containing: {}", enterprisePart);
        return invoiceRepository.findByEnterpriseContaining(enterprisePart)
                .onItem().transform(invoices -> {
                    log.debug("Found {} invoices for enterprise containing: {}", invoices.size(), enterprisePart);
                    return invoices;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoices by enterprise containing: {}", enterprisePart, throwable));
    }

    public Uni<List<Invoice>> findByAmountGreaterThan(BigDecimal minAmount) {
        log.info("Finding invoices with amount greater than: {}", minAmount);
        return invoiceRepository.findByAmountGreaterThan(minAmount)
                .onItem().transform(invoices -> {
                    log.debug("Found {} invoices with amount > {}", invoices.size(), minAmount);
                    return invoices;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoices with amount greater than: {}", minAmount, throwable));
    }

    public Uni<List<Invoice>> findByAmountLessThan(BigDecimal maxAmount) {
        log.info("Finding invoices with amount less than: {}", maxAmount);
        return invoiceRepository.findByAmountLessThan(maxAmount)
                .onItem().transform(invoices -> {
                    log.debug("Found {} invoices with amount < {}", invoices.size(), maxAmount);
                    return invoices;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoices with amount less than: {}", maxAmount, throwable));
    }

    public Uni<List<Invoice>> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount) {
        log.info("Finding invoices with amount between: {} and {}", minAmount, maxAmount);
        return invoiceRepository.findByAmountBetween(minAmount, maxAmount)
                .onItem().transform(invoices -> {
                    log.debug("Found {} invoices with amount between {} and {}",
                            invoices.size(), minAmount, maxAmount);
                    return invoices;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoices with amount between: {} and {}",
                                minAmount, maxAmount, throwable));
    }

    public Uni<List<Invoice>> findByCustomerPaginated(Customer customer, int pageIndex, int pageSize) {
        log.info("Finding invoices by customer paginated, customer: {}, pageIndex: {}, pageSize: {}",
                customer.getId(), pageIndex, pageSize);
        return invoiceRepository.findByCustomerPaginated(customer, pageIndex, pageSize)
                .onItem().transform(invoices -> {
                    log.debug("Found {} invoices for customer paginated query", invoices.size());
                    return invoices;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoices by customer paginated, customer: {}, pageIndex: {}, pageSize: {}",
                                customer.getId(), pageIndex, pageSize, throwable));
    }

    public Uni<List<Invoice>> findByDateRangePaginated(Date startDate, Date endDate, int pageIndex, int pageSize) {
        log.info("Finding invoices by date range paginated, startDate: {}, endDate: {}, pageIndex: {}, pageSize: {}",
                startDate, endDate, pageIndex, pageSize);
        return invoiceRepository.findByDateRangePaginated(startDate, endDate, pageIndex, pageSize)
                .onItem().transform(invoices -> {
                    log.debug("Found {} invoices for date range paginated query", invoices.size());
                    return invoices;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoices by date range paginated, startDate: {}, endDate: {}, pageIndex: {}, pageSize: {}",
                                startDate, endDate, pageIndex, pageSize, throwable));
    }

    @CacheResult(cacheName = "invoice-total-revenue-by-customer")
    public Uni<Invoice> getTotalRevenueByCustomer(@CacheKey Customer customer) {
        log.info("Calculating total revenue by customer: {}", customer.getId());
        return invoiceRepository.getTotalRevenueByCustomer(customer)
                .onItem().transform(revenue -> {
                    log.debug("Total revenue for customer {}: {}", customer.getId(), revenue);
                    return revenue;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error calculating total revenue by customer: {}", customer.getId(), throwable));
    }

    public Uni<Long> countByCustomer(Customer customer) {
        log.info("Counting invoices by customer: {}", customer.getId());
        return invoiceRepository.countByCustomer(customer)
                .onItem().transform(count -> {
                    log.debug("Found {} invoices for customer: {}", count, customer.getId());
                    return count;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error counting invoices by customer: {}", customer.getId(), throwable));
    }

    public Uni<Long> countByDateRange(Date startDate, Date endDate) {
        log.info("Counting invoices by date range: {} to {}", startDate, endDate);
        return invoiceRepository.countByDateRange(startDate, endDate)
                .onItem().transform(count -> {
                    log.debug("Found {} invoices for date range: {} to {}", count, startDate, endDate);
                    return count;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error counting invoices by date range: {} to {}", startDate, endDate, throwable));
    }

    @CacheResult(cacheName = "invoice-average-amount")
    public Uni<Invoice> getAverageInvoiceAmount() {
        log.info("Calculating average invoice amount");
        return invoiceRepository.getAverageInvoiceAmount()
                .onItem().transform(average -> {
                    log.debug("Average invoice amount: {}", average);
                    return average;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error calculating average invoice amount", throwable));
    }

    @CacheResult(cacheName = "invoice-max-amount")
    public Uni<Invoice> getMaxInvoiceAmount() {
        log.info("Finding maximum invoice amount");
        return invoiceRepository.getMaxInvoiceAmount()
                .onItem().transform(maxAmount -> {
                    log.debug("Maximum invoice amount: {}", maxAmount);
                    return maxAmount;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding maximum invoice amount", throwable));
    }

    @CacheResult(cacheName = "invoice-min-amount")
    public Uni<Invoice> getMinInvoiceAmount() {
        log.info("Finding minimum invoice amount");
        return invoiceRepository.getMinInvoiceAmount()
                .onItem().transform(minAmount -> {
                    log.debug("Minimum invoice amount: {}", minAmount);
                    return minAmount;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding minimum invoice amount", throwable));
    }

    @CacheResult(cacheName = "invoice-exists-by-customer-date")
    public Uni<Boolean> existsByCustomerAndDate(@CacheKey Customer customer, @CacheKey Date date) {
        log.info("Checking if invoice exists by customer: {} and date: {}", customer.getId(), date);
        return invoiceRepository.existsByCustomerAndDate(customer, date)
                .onItem().transform(exists -> {
                    log.debug("Invoice exists by customer {} and date {}: {}", customer.getId(), date, exists);
                    return exists;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error checking if invoice exists by customer: {} and date: {}",
                                customer.getId(), date, throwable));
    }

    @CacheResult(cacheName = "invoice-exists-by-enterprise-date")
    public Uni<Boolean> existsByEnterpriseAndDate(@CacheKey String enterprise, @CacheKey Date date) {
        log.info("Checking if invoice exists by enterprise: {} and date: {}", enterprise, date);
        return invoiceRepository.existsByEnterpriseAndDate(enterprise, date)
                .onItem().transform(exists -> {
                    log.debug("Invoice exists by enterprise {} and date {}: {}", enterprise, date, exists);
                    return exists;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error checking if invoice exists by enterprise: {} and date: {}",
                                enterprise, date, throwable));
    }

    public Uni<Long> deleteByCustomer(Customer customer) {
        log.info("Deleting invoices by customer: {}", customer.getId());
        return invoiceRepository.deleteByCustomer(customer)
                .onItem().transform(count -> {
                    log.info("Deleted {} invoices for customer: {}", count, customer.getId());
                    invalidateRelatedCaches();
                    return count;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error deleting invoices by customer: {}", customer.getId(), throwable));
    }

    public Uni<Long> deleteByDateRange(Date startDate, Date endDate) {
        log.info("Deleting invoices by date range: {} to {}", startDate, endDate);
        return invoiceRepository.deleteByDateRange(startDate, endDate)
                .onItem().transform(count -> {
                    log.info("Deleted {} invoices for date range: {} to {}", count, startDate, endDate);
                    invalidateRelatedCaches();
                    return count;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error deleting invoices by date range: {} to {}", startDate, endDate, throwable));
    }

    public Uni<Integer> updateTotalAmount(Long id, BigDecimal newAmount) {
        log.info("Updating total amount for invoice id: {}, new amount: {}", id, newAmount);
        return invoiceRepository.updateTotalAmount(id, newAmount)
                .onItem().transform(updatedCount -> {
                    log.info("Updated total amount for {} invoices with id: {}", updatedCount, id);
                    invalidateRelatedCaches();
                    return updatedCount;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error updating total amount for invoice id: {}, new amount: {}",
                                id, newAmount, throwable));
    }

    public Uni<Integer> updateEnterprise(Long id, String newEnterprise) {
        log.info("Updating enterprise for invoice id: {}, new enterprise: {}", id, newEnterprise);
        return invoiceRepository.updateEnterprise(id, newEnterprise)
                .onItem().transform(updatedCount -> {
                    log.info("Updated enterprise for {} invoices with id: {}", updatedCount, id);
                    invalidateRelatedCaches();
                    return updatedCount;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error updating enterprise for invoice id: {}, new enterprise: {}",
                                id, newEnterprise, throwable));
    }

    public Uni<Integer> updateAmountsForCustomer(Customer customer, BigDecimal newAmount) {
        log.info("Updating amounts for customer: {} to new amount: {}", customer.getId(), newAmount);
        return invoiceRepository.updateAmountsForCustomer(customer, newAmount)
                .onItem().transform(updatedCount -> {
                    log.info("Updated amounts for {} invoices with customer: {}", updatedCount, customer.getId());
                    invalidateRelatedCaches();
                    return updatedCount;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error updating amounts for customer: {} to new amount: {}",
                                customer.getId(), newAmount, throwable));
    }

    @CacheResult(cacheName = "invoice-top-invoices")
    public Uni<List<Invoice>> findTopInvoicesByAmount(@CacheKey int limit) {
        log.info("Finding top {} invoices by amount", limit);
        return invoiceRepository.findTopInvoicesByAmount(limit)
                .onItem().transform(invoices -> {
                    log.debug("Found {} top invoices by amount", invoices.size());
                    return invoices;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding top {} invoices by amount", limit, throwable));
    }

    @CacheResult(cacheName = "invoice-recent-invoices")
    public Uni<List<Invoice>> findRecentInvoices(@CacheKey int days) {
        log.info("Finding recent invoices from last {} days", days);
        return invoiceRepository.findRecentInvoices(days)
                .onItem().transform(invoices -> {
                    log.debug("Found {} recent invoices from last {} days", invoices.size(), days);
                    return invoices;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding recent invoices from last {} days", days, throwable));
    }

    @CacheResult(cacheName = "invoice-without-items")
    public Uni<List<Invoice>> findInvoicesWithoutItems() {
        log.info("Finding invoices without items");
        return invoiceRepository.findInvoicesWithoutItems()
                .onItem().transform(invoices -> {
                    log.debug("Found {} invoices without items", invoices.size());
                    return invoices;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoices without items", throwable));
    }

    public Uni<Long> getPageCountByCustomer(Customer customer, int pageSize) {
        log.info("Calculating page count by customer: {}, page size: {}", customer.getId(), pageSize);
        return invoiceRepository.getPageCountByCustomer(customer, pageSize)
                .onItem().transform(pageCount -> {
                    log.debug("Page count by customer: {} for page size: {}", pageCount, pageSize);
                    return pageCount;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error calculating page count by customer: {}, page size: {}",
                                customer.getId(), pageSize, throwable));
    }

    public Uni<Long> getPageCountByDateRange(Date startDate, Date endDate, int pageSize) {
        log.info("Calculating page count by date range: {} to {}, page size: {}", startDate, endDate, pageSize);
        return invoiceRepository.getPageCountByDateRange(startDate, endDate, pageSize)
                .onItem().transform(pageCount -> {
                    log.debug("Page count by date range: {} for page size: {}", pageCount, pageSize);
                    return pageCount;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error calculating page count by date range: {} to {}, page size: {}",
                                startDate, endDate, pageSize, throwable));
    }

    // Override base methods for Invoice-specific behavior
    @Override
    public Uni<Invoice> save(Invoice invoice) {
        log.info("Saving invoice for customer: {}, date: {}",
                invoice.getCustomer().getId(), invoice.getInvoiceDate());
        return super.save(invoice)
                .onItem().invoke(savedInvoice ->
                        log.info("Successfully saved invoice with id: {}", savedInvoice.getId()));
    }

    @Override
    public Uni<Invoice> update(Invoice invoice) {
        log.info("Updating invoice with id: {}", invoice.getId());
        return super.update(invoice)
                .onItem().invoke(updatedInvoice ->
                        log.info("Successfully updated invoice with id: {}", updatedInvoice.getId()));
    }

    @Scheduled(cron = "* 0 0/5 * * * ")
    @CacheInvalidateAll(cacheName = "invoice-by-id")
    @CacheInvalidateAll(cacheName = "invoice-by-name")
    @CacheInvalidateAll(cacheName = "invoice-all-sorted")
    @CacheInvalidateAll(cacheName = "invoice-exists-by-name")
    @CacheInvalidateAll(cacheName = "invoice-exists-by-id")
    @CacheInvalidateAll(cacheName = "invoice-count-all")
    @CacheInvalidateAll(cacheName = "invoice-by-customer")
    @CacheInvalidateAll(cacheName = "invoice-by-customer-id")
    @CacheInvalidateAll(cacheName = "invoice-by-settlement")
    @CacheInvalidateAll(cacheName = "invoice-by-settlement-id")
    @CacheInvalidateAll(cacheName = "invoice-by-date")
    @CacheInvalidateAll(cacheName = "invoice-by-date-range")
    @CacheInvalidateAll(cacheName = "invoice-by-enterprise")
    @CacheInvalidateAll(cacheName = "invoice-exists-by-customer-date")
    @CacheInvalidateAll(cacheName = "invoice-exists-by-enterprise-date")
    @CacheInvalidateAll(cacheName = "invoice-total-revenue-by-customer")
    @CacheInvalidateAll(cacheName = "invoice-average-amount")
    @CacheInvalidateAll(cacheName = "invoice-max-amount")
    @CacheInvalidateAll(cacheName = "invoice-min-amount")
    @CacheInvalidateAll(cacheName = "invoice-top-invoices")
    @CacheInvalidateAll(cacheName = "invoice-recent-invoices")
    @CacheInvalidateAll(cacheName = "invoice-without-items")
    public void scheduledCacheInvalidation() {
        log.debug("Scheduled cache invalidation for InvoiceService");
    }

    @Override
    protected void invalidateRelatedCaches() {
        scheduledCacheInvalidation();
    }
}