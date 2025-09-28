package by.losik.service;

import by.losik.entity.Invoice;
import by.losik.entity.InvoiceItem;
import by.losik.entity.Product;
import by.losik.repository.InvoiceItemRepository;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@ApplicationScoped
@Slf4j
public class InvoiceItemService extends BaseService<InvoiceItem, InvoiceItemRepository> {

    @Inject
    InvoiceItemRepository invoiceItemRepository;

    @Override
    protected String getEntityName() {
        return "InvoiceItem";
    }

    @Override
    protected String getCachePrefix() {
        return "invoice-item";
    }

    @Inject
    public void setRepository(InvoiceItemRepository repository) {
        this.repository = repository;
    }

    @CacheResult(cacheName = "invoice-item-by-invoice")
    public Uni<List<InvoiceItem>> findByInvoice(@CacheKey Invoice invoice) {
        log.info("Finding invoice items by invoice: {}", invoice.getId());
        return invoiceItemRepository.findByInvoice(invoice)
                .onItem().transform(items -> {
                    log.debug("Found {} invoice items for invoice: {}", items.size(), invoice.getId());
                    return items;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoice items by invoice: {}", invoice.getId(), throwable));
    }

    @CacheResult(cacheName = "invoice-item-by-invoice-id")
    public Uni<List<InvoiceItem>> findByInvoiceId(@CacheKey Long invoiceId) {
        log.info("Finding invoice items by invoice id: {}", invoiceId);
        return invoiceItemRepository.findByInvoiceId(invoiceId)
                .onItem().transform(items -> {
                    log.debug("Found {} invoice items for invoice id: {}", items.size(), invoiceId);
                    return items;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoice items by invoice id: {}", invoiceId, throwable));
    }

    @CacheResult(cacheName = "invoice-item-by-product")
    public Uni<List<InvoiceItem>> findByProduct(@CacheKey Product product) {
        log.info("Finding invoice items by product: {}", product.getId());
        return invoiceItemRepository.findByProduct(product)
                .onItem().transform(items -> {
                    log.debug("Found {} invoice items for product: {}", items.size(), product.getId());
                    return items;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoice items by product: {}", product.getId(), throwable));
    }

    @CacheResult(cacheName = "invoice-item-by-product-id")
    public Uni<List<InvoiceItem>> findByProductId(@CacheKey Long productId) {
        log.info("Finding invoice items by product id: {}", productId);
        return invoiceItemRepository.findByProductId(productId)
                .onItem().transform(items -> {
                    log.debug("Found {} invoice items for product id: {}", items.size(), productId);
                    return items;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoice items by product id: {}", productId, throwable));
    }

    public Uni<Long> countByInvoice(Invoice invoice) {
        log.info("Counting invoice items by invoice: {}", invoice.getId());
        return invoiceItemRepository.countByInvoice(invoice)
                .onItem().transform(count -> {
                    log.debug("Found {} invoice items for invoice: {}", count, invoice.getId());
                    return count;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error counting invoice items by invoice: {}", invoice.getId(), throwable));
    }

    public Uni<List<InvoiceItem>> findByQuantityGreaterThan(BigInteger minQuantity) {
        log.info("Finding invoice items with quantity greater than: {}", minQuantity);
        return invoiceItemRepository.findByQuantityGreaterThan(minQuantity)
                .onItem().transform(items -> {
                    log.debug("Found {} invoice items with quantity > {}", items.size(), minQuantity);
                    return items;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoice items with quantity greater than: {}", minQuantity, throwable));
    }

    public Uni<List<InvoiceItem>> findByQuantityLessThan(BigInteger maxQuantity) {
        log.info("Finding invoice items with quantity less than: {}", maxQuantity);
        return invoiceItemRepository.findByQuantityLessThan(maxQuantity)
                .onItem().transform(items -> {
                    log.debug("Found {} invoice items with quantity < {}", items.size(), maxQuantity);
                    return items;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoice items with quantity less than: {}", maxQuantity, throwable));
    }

    public Uni<List<InvoiceItem>> findByQuantityBetween(BigInteger minQuantity, BigInteger maxQuantity) {
        log.info("Finding invoice items with quantity between: {} and {}", minQuantity, maxQuantity);
        return invoiceItemRepository.findByQuantityBetween(minQuantity, maxQuantity)
                .onItem().transform(items -> {
                    log.debug("Found {} invoice items with quantity between {} and {}",
                            items.size(), minQuantity, maxQuantity);
                    return items;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoice items with quantity between: {} and {}",
                                minQuantity, maxQuantity, throwable));
    }

    public Uni<List<InvoiceItem>> findByPriceGreaterThan(BigDecimal minPrice) {
        log.info("Finding invoice items with price greater than: {}", minPrice);
        return invoiceItemRepository.findByPriceGreaterThan(minPrice)
                .onItem().transform(items -> {
                    log.debug("Found {} invoice items with price > {}", items.size(), minPrice);
                    return items;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoice items with price greater than: {}", minPrice, throwable));
    }

    public Uni<List<InvoiceItem>> findByPriceLessThan(BigDecimal maxPrice) {
        log.info("Finding invoice items with price less than: {}", maxPrice);
        return invoiceItemRepository.findByPriceLessThan(maxPrice)
                .onItem().transform(items -> {
                    log.debug("Found {} invoice items with price < {}", items.size(), maxPrice);
                    return items;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoice items with price less than: {}", maxPrice, throwable));
    }

    public Uni<List<InvoiceItem>> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Finding invoice items with price between: {} and {}", minPrice, maxPrice);
        return invoiceItemRepository.findByPriceBetween(minPrice, maxPrice)
                .onItem().transform(items -> {
                    log.debug("Found {} invoice items with price between {} and {}",
                            items.size(), minPrice, maxPrice);
                    return items;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoice items with price between: {} and {}",
                                minPrice, maxPrice, throwable));
    }

    public Uni<List<InvoiceItem>> findByInvoicePaginated(Invoice invoice, int pageIndex, int pageSize) {
        log.info("Finding invoice items by invoice paginated, invoice: {}, pageIndex: {}, pageSize: {}",
                invoice.getId(), pageIndex, pageSize);
        return invoiceItemRepository.findByInvoicePaginated(invoice, pageIndex, pageSize)
                .onItem().transform(items -> {
                    log.debug("Found {} invoice items for invoice paginated query", items.size());
                    return items;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoice items by invoice paginated, invoice: {}, pageIndex: {}, pageSize: {}",
                                invoice.getId(), pageIndex, pageSize, throwable));
    }

    public Uni<List<InvoiceItem>> findByInvoiceIdPaginated(Long invoiceId, int pageIndex, int pageSize) {
        log.info("Finding invoice items by invoice id paginated, invoiceId: {}, pageIndex: {}, pageSize: {}",
                invoiceId, pageIndex, pageSize);
        return invoiceItemRepository.findByInvoiceIdPaginated(invoiceId, pageIndex, pageSize)
                .onItem().transform(items -> {
                    log.debug("Found {} invoice items for invoice id paginated query", items.size());
                    return items;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding invoice items by invoice id paginated, invoiceId: {}, pageIndex: {}, pageSize: {}",
                                invoiceId, pageIndex, pageSize, throwable));
    }

    @CacheResult(cacheName = "invoice-item-exists-by-invoice-product")
    public Uni<Boolean> existsByInvoiceAndProduct(@CacheKey Invoice invoice, @CacheKey Product product) {
        log.info("Checking if invoice item exists by invoice: {} and product: {}", invoice.getId(), product.getId());
        return invoiceItemRepository.existsByInvoiceAndProduct(invoice, product)
                .onItem().transform(exists -> {
                    log.debug("Invoice item exists by invoice {} and product {}: {}",
                            invoice.getId(), product.getId(), exists);
                    return exists;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error checking if invoice item exists by invoice: {} and product: {}",
                                invoice.getId(), product.getId(), throwable));
    }

    @CacheResult(cacheName = "invoice-item-exists-by-invoice-product-id")
    public Uni<Boolean> existsByInvoiceIdAndProductId(@CacheKey Long invoiceId, @CacheKey Long productId) {
        log.info("Checking if invoice item exists by invoiceId: {} and productId: {}", invoiceId, productId);
        return invoiceItemRepository.existsByInvoiceIdAndProductId(invoiceId, productId)
                .onItem().transform(exists -> {
                    log.debug("Invoice item exists by invoiceId {} and productId {}: {}",
                            invoiceId, productId, exists);
                    return exists;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error checking if invoice item exists by invoiceId: {} and productId: {}",
                                invoiceId, productId, throwable));
    }

    public Uni<Long> deleteByInvoice(Invoice invoice) {
        log.info("Deleting invoice items by invoice: {}", invoice.getId());
        return invoiceItemRepository.deleteByInvoice(invoice)
                .onItem().transform(count -> {
                    log.info("Deleted {} invoice items for invoice: {}", count, invoice.getId());
                    invalidateRelatedCaches();
                    return count;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error deleting invoice items by invoice: {}", invoice.getId(), throwable));
    }

    public Uni<Long> deleteByInvoiceId(Long invoiceId) {
        log.info("Deleting invoice items by invoice id: {}", invoiceId);
        return invoiceItemRepository.deleteByInvoiceId(invoiceId)
                .onItem().transform(count -> {
                    log.info("Deleted {} invoice items for invoice id: {}", count, invoiceId);
                    invalidateRelatedCaches();
                    return count;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error deleting invoice items by invoice id: {}", invoiceId, throwable));
    }

    public Uni<Long> deleteByProduct(Product product) {
        log.info("Deleting invoice items by product: {}", product.getId());
        return invoiceItemRepository.deleteByProduct(product)
                .onItem().transform(count -> {
                    log.info("Deleted {} invoice items for product: {}", count, product.getId());
                    invalidateRelatedCaches();
                    return count;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error deleting invoice items by product: {}", product.getId(), throwable));
    }

    public Uni<Integer> updateQuantity(Long id, BigInteger newQuantity) {
        log.info("Updating quantity for invoice item id: {}, new quantity: {}", id, newQuantity);
        return invoiceItemRepository.updateQuantity(id, newQuantity)
                .onItem().transform(updatedCount -> {
                    log.info("Updated quantity for {} invoice items with id: {}", updatedCount, id);
                    invalidateRelatedCaches();
                    return updatedCount;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error updating quantity for invoice item id: {}, new quantity: {}",
                                id, newQuantity, throwable));
    }

    public Uni<Integer> updatePrice(Long id, BigDecimal newPrice) {
        log.info("Updating price for invoice item id: {}, new price: {}", id, newPrice);
        return invoiceItemRepository.updatePrice(id, newPrice)
                .onItem().transform(updatedCount -> {
                    log.info("Updated price for {} invoice items with id: {}", updatedCount, id);
                    invalidateRelatedCaches();
                    return updatedCount;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error updating price for invoice item id: {}, new price: {}",
                                id, newPrice, throwable));
    }

    public Uni<Integer> updateQuantityAndPrice(Long id, BigInteger newQuantity, BigDecimal newPrice) {
        log.info("Updating quantity and price for invoice item id: {}, quantity: {}, price: {}",
                id, newQuantity, newPrice);
        return invoiceItemRepository.updateQuantityAndPrice(id, newQuantity, newPrice)
                .onItem().transform(updatedCount -> {
                    log.info("Updated quantity and price for {} invoice items with id: {}", updatedCount, id);
                    invalidateRelatedCaches();
                    return updatedCount;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error updating quantity and price for invoice item id: {}, quantity: {}, price: {}",
                                id, newQuantity, newPrice, throwable));
    }

    public Uni<Integer> updatePricesForProduct(Product product, BigDecimal newPrice) {
        log.info("Updating prices for product: {} to new price: {}", product.getId(), newPrice);
        return invoiceItemRepository.updatePricesForProduct(product, newPrice)
                .onItem().transform(updatedCount -> {
                    log.info("Updated prices for {} invoice items with product: {}", updatedCount, product.getId());
                    invalidateRelatedCaches();
                    return updatedCount;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error updating prices for product: {} to new price: {}",
                                product.getId(), newPrice, throwable));
    }

    @CacheResult(cacheName = "invoice-item-top-selling")
    public Uni<List<InvoiceItem>> findTopSellingItems(@CacheKey int limit) {
        log.info("Finding top {} selling items", limit);
        return invoiceItemRepository.findTopSellingItems(limit)
                .onItem().transform(items -> {
                    log.debug("Found {} top selling items", items.size());
                    return items;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding top {} selling items", limit, throwable));
    }

    @CacheResult(cacheName = "invoice-item-high-value")
    public Uni<List<InvoiceItem>> findHighValueItems(@CacheKey BigDecimal minValue) {
        log.info("Finding high value items with minimum value: {}", minValue);
        return invoiceItemRepository.findHighValueItems(minValue)
                .onItem().transform(items -> {
                    log.debug("Found {} high value items", items.size());
                    return items;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding high value items with minimum value: {}", minValue, throwable));
    }

    @CacheResult(cacheName = "invoice-item-total-revenue")
    public Uni<InvoiceItem> getTotalRevenue() {
        log.info("Calculating total revenue");
        return invoiceItemRepository.getTotalRevenue()
                .onItem().transform(revenue -> {
                    log.debug("Total revenue: {}", revenue);
                    return revenue;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error calculating total revenue", throwable));
    }

    public Uni<Long> getPageCountByInvoice(Invoice invoice, int pageSize) {
        log.info("Calculating page count by invoice: {}, page size: {}", invoice.getId(), pageSize);
        return invoiceItemRepository.getPageCountByInvoice(invoice, pageSize)
                .onItem().transform(pageCount -> {
                    log.debug("Page count by invoice: {} for page size: {}", pageCount, pageSize);
                    return pageCount;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error calculating page count by invoice: {}, page size: {}",
                                invoice.getId(), pageSize, throwable));
    }

    @Override
    public Uni<InvoiceItem> save(InvoiceItem invoiceItem) {
        log.info("Saving invoice item for invoice: {}, product: {}",
                invoiceItem.getInvoice().getId(), invoiceItem.getProduct().getId());
        return super.save(invoiceItem)
                .onItem().invoke(savedItem ->
                        log.info("Successfully saved invoice item with id: {}", savedItem.getId()));
    }

    @Override
    public Uni<InvoiceItem> update(InvoiceItem invoiceItem) {
        log.info("Updating invoice item with id: {}", invoiceItem.getId());
        return super.update(invoiceItem)
                .onItem().invoke(updatedItem ->
                        log.info("Successfully updated invoice item with id: {}", updatedItem.getId()));
    }

    @Scheduled(cron = "* 0 0/5 * * *")
    @CacheInvalidateAll(cacheName = "invoice-item-by-id")
    @CacheInvalidateAll(cacheName = "invoice-item-by-name")
    @CacheInvalidateAll(cacheName = "invoice-item-all-sorted")
    @CacheInvalidateAll(cacheName = "invoice-item-exists-by-name")
    @CacheInvalidateAll(cacheName = "invoice-item-exists-by-id")
    @CacheInvalidateAll(cacheName = "invoice-item-count-all")
    @CacheInvalidateAll(cacheName = "invoice-item-by-invoice")
    @CacheInvalidateAll(cacheName = "invoice-item-by-invoice-id")
    @CacheInvalidateAll(cacheName = "invoice-item-by-product")
    @CacheInvalidateAll(cacheName = "invoice-item-by-product-id")
    @CacheInvalidateAll(cacheName = "invoice-item-exists-by-invoice-product")
    @CacheInvalidateAll(cacheName = "invoice-item-exists-by-invoice-product-id")
    @CacheInvalidateAll(cacheName = "invoice-item-total-revenue")
    @CacheInvalidateAll(cacheName = "invoice-item-top-selling")
    @CacheInvalidateAll(cacheName = "invoice-item-high-value")
    public void scheduledCacheInvalidation() {
        log.debug("Scheduled cache invalidation for InvoiceItemService");
    }

    @Override
    protected void invalidateRelatedCaches() {
        scheduledCacheInvalidation();
    }
}