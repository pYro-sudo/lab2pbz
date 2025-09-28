package by.losik.service;

import by.losik.entity.PriceHistory;
import by.losik.entity.Product;
import by.losik.repository.PriceHistoryRepository;
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
public class PriceHistoryService extends BaseService<PriceHistory, PriceHistoryRepository> {

    @Inject
    PriceHistoryRepository priceHistoryRepository;

    @Override
    protected String getEntityName() {
        return "PriceHistory";
    }

    @Override
    protected String getCachePrefix() {
        return "price-history";
    }

    @Inject
    public void setRepository(PriceHistoryRepository repository) {
        this.repository = repository;
    }

    @CacheResult(cacheName = "price-history-by-product")
    public Uni<List<PriceHistory>> findByProduct(@CacheKey Product product) {
        log.info("Finding price histories by product: {}", product.getId());
        return priceHistoryRepository.findByProduct(product)
                .onItem().transform(histories -> {
                    log.debug("Found {} price histories for product: {}", histories.size(), product.getId());
                    return histories;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding price histories by product: {}", product.getId(), throwable));
    }

    @CacheResult(cacheName = "price-history-by-product-id")
    public Uni<List<PriceHistory>> findByProductId(@CacheKey Long productId) {
        log.info("Finding price histories by product id: {}", productId);
        return priceHistoryRepository.findByProductId(productId)
                .onItem().transform(histories -> {
                    log.debug("Found {} price histories for product id: {}", histories.size(), productId);
                    return histories;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding price histories by product id: {}", productId, throwable));
    }

    @CacheResult(cacheName = "price-history-by-date")
    public Uni<List<PriceHistory>> findByChangeDate(@CacheKey Date date) {
        log.info("Finding price histories by change date: {}", date);
        return priceHistoryRepository.findByChangeDate(date)
                .onItem().transform(histories -> {
                    log.debug("Found {} price histories for change date: {}", histories.size(), date);
                    return histories;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding price histories by change date: {}", date, throwable));
    }

    @CacheResult(cacheName = "price-history-by-date-range")
    public Uni<List<PriceHistory>> findByChangeDateRange(@CacheKey Date startDate, @CacheKey Date endDate) {
        log.info("Finding price histories by change date range: {} to {}", startDate, endDate);
        return priceHistoryRepository.findByChangeDateRange(startDate, endDate)
                .onItem().transform(histories -> {
                    log.debug("Found {} price histories for change date range: {} to {}",
                            histories.size(), startDate, endDate);
                    return histories;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding price histories by change date range: {} to {}",
                                startDate, endDate, throwable));
    }

    public Uni<List<PriceHistory>> findByChangeDateBefore(Date date) {
        log.info("Finding price histories before change date: {}", date);
        return priceHistoryRepository.findByChangeDateBefore(date)
                .onItem().transform(histories -> {
                    log.debug("Found {} price histories before change date: {}", histories.size(), date);
                    return histories;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding price histories before change date: {}", date, throwable));
    }

    public Uni<List<PriceHistory>> findByChangeDateAfter(Date date) {
        log.info("Finding price histories after change date: {}", date);
        return priceHistoryRepository.findByChangeDateAfter(date)
                .onItem().transform(histories -> {
                    log.debug("Found {} price histories after change date: {}", histories.size(), date);
                    return histories;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding price histories after change date: {}", date, throwable));
    }

    public Uni<List<PriceHistory>> findByPriceGreaterThan(BigDecimal minPrice) {
        log.info("Finding price histories with price greater than: {}", minPrice);
        return priceHistoryRepository.findByPriceGreaterThan(minPrice)
                .onItem().transform(histories -> {
                    log.debug("Found {} price histories with price > {}", histories.size(), minPrice);
                    return histories;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding price histories with price greater than: {}", minPrice, throwable));
    }

    public Uni<List<PriceHistory>> findByPriceLessThan(BigDecimal maxPrice) {
        log.info("Finding price histories with price less than: {}", maxPrice);
        return priceHistoryRepository.findByPriceLessThan(maxPrice)
                .onItem().transform(histories -> {
                    log.debug("Found {} price histories with price < {}", histories.size(), maxPrice);
                    return histories;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding price histories with price less than: {}", maxPrice, throwable));
    }

    public Uni<List<PriceHistory>> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Finding price histories with price between: {} and {}", minPrice, maxPrice);
        return priceHistoryRepository.findByPriceBetween(minPrice, maxPrice)
                .onItem().transform(histories -> {
                    log.debug("Found {} price histories with price between {} and {}",
                            histories.size(), minPrice, maxPrice);
                    return histories;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding price histories with price between: {} and {}",
                                minPrice, maxPrice, throwable));
    }

    @CacheResult(cacheName = "price-history-latest-by-product")
    public Uni<PriceHistory> findLatestByProduct(@CacheKey Product product) {
        log.info("Finding latest price history by product: {}", product.getId());
        return priceHistoryRepository.findLatestByProduct(product)
                .onItem().transform(history -> {
                    if (history != null) {
                        log.debug("Found latest price history for product {}: price {}",
                                product.getId(), history.getPrice());
                    } else {
                        log.warn("No price history found for product: {}", product.getId());
                    }
                    return history;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding latest price history by product: {}", product.getId(), throwable));
    }

    @CacheResult(cacheName = "price-history-latest-by-product-id")
    public Uni<PriceHistory> findLatestByProductId(@CacheKey Long productId) {
        log.info("Finding latest price history by product id: {}", productId);
        return priceHistoryRepository.findLatestByProductId(productId)
                .onItem().transform(history -> {
                    if (history != null) {
                        log.debug("Found latest price history for product id {}: price {}",
                                productId, history.getPrice());
                    } else {
                        log.warn("No price history found for product id: {}", productId);
                    }
                    return history;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding latest price history by product id: {}", productId, throwable));
    }

    @CacheResult(cacheName = "price-history-oldest-by-product")
    public Uni<PriceHistory> findOldestByProduct(@CacheKey Product product) {
        log.info("Finding oldest price history by product: {}", product.getId());
        return priceHistoryRepository.findOldestByProduct(product)
                .onItem().transform(history -> {
                    if (history != null) {
                        log.debug("Found oldest price history for product {}: price {}",
                                product.getId(), history.getPrice());
                    } else {
                        log.warn("No price history found for product: {}", product.getId());
                    }
                    return history;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding oldest price history by product: {}", product.getId(), throwable));
    }

    @CacheResult(cacheName = "price-history-price-at-date")
    public Uni<PriceHistory> findPriceAtDate(@CacheKey Product product, @CacheKey Date date) {
        log.info("Finding price at date for product: {}, date: {}", product.getId(), date);
        return priceHistoryRepository.findPriceAtDate(product, date)
                .onItem().transform(history -> {
                    if (history != null) {
                        log.debug("Found price at date for product {}: price {}",
                                product.getId(), history.getPrice());
                    } else {
                        log.warn("No price history found for product {} at date: {}", product.getId(), date);
                    }
                    return history;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding price at date for product: {}, date: {}",
                                product.getId(), date, throwable));
    }

    @CacheResult(cacheName = "price-history-price-at-date-by-product-id")
    public Uni<PriceHistory> findPriceAtDateByProductId(@CacheKey Long productId, @CacheKey Date date) {
        log.info("Finding price at date for product id: {}, date: {}", productId, date);
        return priceHistoryRepository.findPriceAtDateByProductId(productId, date)
                .onItem().transform(history -> {
                    if (history != null) {
                        log.debug("Found price at date for product id {}: price {}", productId, history.getPrice());
                    } else {
                        log.warn("No price history found for product id {} at date: {}", productId, date);
                    }
                    return history;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding price at date for product id: {}, date: {}",
                                productId, date, throwable));
    }

    public Uni<List<PriceHistory>> findByProductPaginated(Product product, int pageIndex, int pageSize) {
        log.info("Finding price histories by product paginated, product: {}, pageIndex: {}, pageSize: {}",
                product.getId(), pageIndex, pageSize);
        return priceHistoryRepository.findByProductPaginated(product, pageIndex, pageSize)
                .onItem().transform(histories -> {
                    log.debug("Found {} price histories for product paginated query", histories.size());
                    return histories;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding price histories by product paginated, product: {}, pageIndex: {}, pageSize: {}",
                                product.getId(), pageIndex, pageSize, throwable));
    }

    public Uni<List<PriceHistory>> findByDateRangePaginated(Date startDate, Date endDate, int pageIndex, int pageSize) {
        log.info("Finding price histories by date range paginated, startDate: {}, endDate: {}, pageIndex: {}, pageSize: {}",
                startDate, endDate, pageIndex, pageSize);
        return priceHistoryRepository.findByDateRangePaginated(startDate, endDate, pageIndex, pageSize)
                .onItem().transform(histories -> {
                    log.debug("Found {} price histories for date range paginated query", histories.size());
                    return histories;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding price histories by date range paginated, startDate: {}, endDate: {}, pageIndex: {}, pageSize: {}",
                                startDate, endDate, pageIndex, pageSize, throwable));
    }

    @CacheResult(cacheName = "price-history-current-price")
    public Uni<BigDecimal> getCurrentPrice(@CacheKey Product product) {
        log.info("Getting current price for product: {}", product.getId());
        return priceHistoryRepository.getCurrentPrice(product)
                .onItem().transform(price -> {
                    log.debug("Current price for product {}: {}", product.getId(), price);
                    return price;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error getting current price for product: {}", product.getId(), throwable));
    }

    @CacheResult(cacheName = "price-history-price-change")
    public Uni<PriceHistory> getPriceChange(@CacheKey Product product) {
        log.info("Getting price change for product: {}", product.getId());
        return priceHistoryRepository.getPriceChange(product)
                .onItem().transform(change -> {
                    log.debug("Price change for product {}: {}", product.getId(), change);
                    return change;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error getting price change for product: {}", product.getId(), throwable));
    }

    @CacheResult(cacheName = "price-history-max-price")
    public Uni<PriceHistory> getMaxPriceByProduct(@CacheKey Product product) {
        log.info("Getting max price for product: {}", product.getId());
        return priceHistoryRepository.getMaxPriceByProduct(product)
                .onItem().transform(maxPrice -> {
                    log.debug("Max price for product {}: {}", product.getId(), maxPrice);
                    return maxPrice;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error getting max price for product: {}", product.getId(), throwable));
    }

    @CacheResult(cacheName = "price-history-min-price")
    public Uni<PriceHistory> getMinPriceByProduct(@CacheKey Product product) {
        log.info("Getting min price for product: {}", product.getId());
        return priceHistoryRepository.getMinPriceByProduct(product)
                .onItem().transform(minPrice -> {
                    log.debug("Min price for product {}: {}", product.getId(), minPrice);
                    return minPrice;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error getting min price for product: {}", product.getId(), throwable));
    }

    @CacheResult(cacheName = "price-history-price-trend")
    public Uni<List<PriceHistory>> getPriceTrend(@CacheKey Product product, @CacheKey int limit) {
        log.info("Getting price trend for product: {}, limit: {}", product.getId(), limit);
        return priceHistoryRepository.getPriceTrend(product, limit)
                .onItem().transform(trend -> {
                    log.debug("Found {} price trend entries for product {}", trend.size(), product.getId());
                    return trend;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error getting price trend for product: {}, limit: {}",
                                product.getId(), limit, throwable));
    }

    public Uni<Long> countPriceChangesByProduct(Product product) {
        log.info("Counting price changes by product: {}", product.getId());
        return priceHistoryRepository.countPriceChangesByProduct(product)
                .onItem().transform(count -> {
                    log.debug("Found {} price changes for product: {}", count, product.getId());
                    return count;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error counting price changes by product: {}", product.getId(), throwable));
    }

    public Uni<Long> countPriceChangesByDateRange(Date startDate, Date endDate) {
        log.info("Counting price changes by date range: {} to {}", startDate, endDate);
        return priceHistoryRepository.countPriceChangesByDateRange(startDate, endDate)
                .onItem().transform(count -> {
                    log.debug("Found {} price changes for date range: {} to {}", count, startDate, endDate);
                    return count;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error counting price changes by date range: {} to {}", startDate, endDate, throwable));
    }

    @CacheResult(cacheName = "price-history-exists-by-product-date")
    public Uni<Boolean> existsByProductAndDate(@CacheKey Product product, @CacheKey Date date) {
        log.info("Checking if price history exists by product: {} and date: {}", product.getId(), date);
        return priceHistoryRepository.existsByProductAndDate(product, date)
                .onItem().transform(exists -> {
                    log.debug("Price history exists by product {} and date {}: {}", product.getId(), date, exists);
                    return exists;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error checking if price history exists by product: {} and date: {}",
                                product.getId(), date, throwable));
    }

    @CacheResult(cacheName = "price-history-has-history")
    public Uni<Boolean> hasPriceHistory(@CacheKey Product product) {
        log.info("Checking if price history exists for product: {}", product.getId());
        return priceHistoryRepository.hasPriceHistory(product)
                .onItem().transform(hasHistory -> {
                    log.debug("Price history exists for product {}: {}", product.getId(), hasHistory);
                    return hasHistory;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error checking if price history exists for product: {}", product.getId(), throwable));
    }

    public Uni<Long> deleteByProduct(Product product) {
        log.info("Deleting price histories by product: {}", product.getId());
        return priceHistoryRepository.deleteByProduct(product)
                .onItem().transform(count -> {
                    log.info("Deleted {} price histories for product: {}", count, product.getId());
                    invalidateRelatedCaches();
                    return count;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error deleting price histories by product: {}", product.getId(), throwable));
    }

    public Uni<Long> deleteByDateRange(Date startDate, Date endDate) {
        log.info("Deleting price histories by date range: {} to {}", startDate, endDate);
        return priceHistoryRepository.deleteByDateRange(startDate, endDate)
                .onItem().transform(count -> {
                    log.info("Deleted {} price histories for date range: {} to {}", count, startDate, endDate);
                    invalidateRelatedCaches();
                    return count;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error deleting price histories by date range: {} to {}", startDate, endDate, throwable));
    }

    public Uni<Integer> updatePrice(Long id, BigDecimal newPrice) {
        log.info("Updating price for price history id: {}, new price: {}", id, newPrice);
        return priceHistoryRepository.updatePrice(id, newPrice)
                .onItem().transform(updatedCount -> {
                    log.info("Updated price for {} price histories with id: {}", updatedCount, id);
                    invalidateRelatedCaches();
                    return updatedCount;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error updating price for price history id: {}, new price: {}",
                                id, newPrice, throwable));
    }

    public Uni<Integer> updateChangeDate(Long id, Date newDate) {
        log.info("Updating change date for price history id: {}, new date: {}", id, newDate);
        return priceHistoryRepository.updateChangeDate(id, newDate)
                .onItem().transform(updatedCount -> {
                    log.info("Updated change date for {} price histories with id: {}", updatedCount, id);
                    invalidateRelatedCaches();
                    return updatedCount;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error updating change date for price history id: {}, new date: {}",
                                id, newDate, throwable));
    }

    public Uni<Integer> updatePricesForProduct(Product product, BigDecimal newPrice) {
        log.info("Updating prices for product: {} to new price: {}", product.getId(), newPrice);
        return priceHistoryRepository.updatePricesForProduct(product, newPrice)
                .onItem().transform(updatedCount -> {
                    log.info("Updated prices for {} price histories with product: {}", updatedCount, product.getId());
                    invalidateRelatedCaches();
                    return updatedCount;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error updating prices for product: {} to new price: {}",
                                product.getId(), newPrice, throwable));
    }

    @CacheResult(cacheName = "price-history-recent-changes")
    public Uni<List<PriceHistory>> findRecentPriceChanges(@CacheKey int days) {
        log.info("Finding recent price changes from last {} days", days);
        return priceHistoryRepository.findRecentPriceChanges(days)
                .onItem().transform(changes -> {
                    log.debug("Found {} recent price changes from last {} days", changes.size(), days);
                    return changes;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding recent price changes from last {} days", days, throwable));
    }

    public Uni<Long> getPageCountByProduct(Product product, int pageSize) {
        log.info("Calculating page count by product: {}, page size: {}", product.getId(), pageSize);
        return priceHistoryRepository.getPageCountByProduct(product, pageSize)
                .onItem().transform(pageCount -> {
                    log.debug("Page count by product: {} for page size: {}", pageCount, pageSize);
                    return pageCount;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error calculating page count by product: {}, page size: {}",
                                product.getId(), pageSize, throwable));
    }

    @Override
    public Uni<PriceHistory> save(PriceHistory priceHistory) {
        log.info("Saving price history for product: {}, date: {}, price: {}",
                priceHistory.getProductId().getId(), priceHistory.getInvoiceDate(), priceHistory.getPrice());
        return super.save(priceHistory)
                .onItem().invoke(savedHistory ->
                        log.info("Successfully saved price history with id: {}", savedHistory.getId()));
    }

    @Override
    public Uni<PriceHistory> update(PriceHistory priceHistory) {
        log.info("Updating price history with id: {}", priceHistory.getId());
        return super.update(priceHistory)
                .onItem().invoke(updatedHistory ->
                        log.info("Successfully updated price history with id: {}", updatedHistory.getId()));
    }

    @Scheduled(cron = "* 0 0/5 * * *")
    @CacheInvalidateAll(cacheName = "price-history-by-id")
    @CacheInvalidateAll(cacheName = "price-history-by-name")
    @CacheInvalidateAll(cacheName = "price-history-all-sorted")
    @CacheInvalidateAll(cacheName = "price-history-exists-by-name")
    @CacheInvalidateAll(cacheName = "price-history-exists-by-id")
    @CacheInvalidateAll(cacheName = "price-history-count-all")
    @CacheInvalidateAll(cacheName = "price-history-by-product")
    @CacheInvalidateAll(cacheName = "price-history-by-product-id")
    @CacheInvalidateAll(cacheName = "price-history-by-date")
    @CacheInvalidateAll(cacheName = "price-history-by-date-range")
    @CacheInvalidateAll(cacheName = "price-history-latest-by-product")
    @CacheInvalidateAll(cacheName = "price-history-latest-by-product-id")
    @CacheInvalidateAll(cacheName = "price-history-oldest-by-product")
    @CacheInvalidateAll(cacheName = "price-history-price-at-date")
    @CacheInvalidateAll(cacheName = "price-history-price-at-date-by-product-id")
    @CacheInvalidateAll(cacheName = "price-history-current-price")
    @CacheInvalidateAll(cacheName = "price-history-price-change")
    @CacheInvalidateAll(cacheName = "price-history-max-price")
    @CacheInvalidateAll(cacheName = "price-history-min-price")
    @CacheInvalidateAll(cacheName = "price-history-price-trend")
    @CacheInvalidateAll(cacheName = "price-history-has-increased")
    @CacheInvalidateAll(cacheName = "price-history-exists-by-product-date")
    @CacheInvalidateAll(cacheName = "price-history-has-history")
    @CacheInvalidateAll(cacheName = "price-history-recent-changes")
    @CacheInvalidateAll(cacheName = "price-history-significant-changes")
    public void scheduledCacheInvalidation() {
        log.debug("Scheduled cache invalidation for PriceHistoryService");
    }

    @Override
    protected void invalidateRelatedCaches() {
        scheduledCacheInvalidation();
    }
}