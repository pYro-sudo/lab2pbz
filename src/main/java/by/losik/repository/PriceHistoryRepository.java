package by.losik.repository;

import by.losik.entity.PriceHistory;
import by.losik.entity.Product;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@ApplicationScoped
public class PriceHistoryRepository extends BaseRepository<PriceHistory> {

    @WithTransaction
    public Uni<List<PriceHistory>> findByProductId(Long productId) {
        return find("product.id", productId).list();
    }

    @WithTransaction
    public Uni<List<PriceHistory>> findByProduct(Product product) {
        return find("productId", product).list();
    }

    @WithTransaction
    public Uni<List<PriceHistory>> findByChangeDate(Date date) {
        return find("invoiceDate", date).list();
    }

    @WithTransaction
    public Uni<List<PriceHistory>> findByChangeDateRange(Date startDate, Date endDate) {
        return find("invoiceDate between ?1 and ?2", startDate, endDate).list();
    }

    @WithTransaction
    public Uni<List<PriceHistory>> findByChangeDateBefore(Date date) {
        return find("invoiceDate < ?1", date).list();
    }

    @WithTransaction
    public Uni<List<PriceHistory>> findByChangeDateAfter(Date date) {
        return find("invoiceDate > ?1", date).list();
    }

    @WithTransaction
    public Uni<List<PriceHistory>> findByPriceGreaterThan(BigDecimal minPrice) {
        return find("price > ?1", minPrice).list();
    }

    @WithTransaction
    public Uni<List<PriceHistory>> findByPriceLessThan(BigDecimal maxPrice) {
        return find("price < ?1", maxPrice).list();
    }

    @WithTransaction
    public Uni<List<PriceHistory>> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return find("price between ?1 and ?2", minPrice, maxPrice).list();
    }

    @WithTransaction
    public Uni<PriceHistory> findLatestByProduct(Product product) {
        return find("productId = ?1 order by invoiceDate desc", product).firstResult();
    }

    @WithTransaction
    public Uni<PriceHistory> findLatestByProductId(Long productId) {
        return find("productId.id = ?1 order by invoiceDate desc", productId).firstResult();
    }

    @WithTransaction
    public Uni<PriceHistory> findOldestByProduct(Product product) {
        return find("productId = ?1 order by invoiceDate asc", product).firstResult();
    }

    @WithTransaction
    public Uni<PriceHistory> findPriceAtDate(Product product, Date date) {
        return find("productId = ?1 and invoiceDate = ?2", product, date).firstResult();
    }

    @WithTransaction
    public Uni<PriceHistory> findPriceAtDateByProductId(Long productId, Date date) {
        return find("productId.id = ?1 and invoiceDate = ?2", productId, date).firstResult();
    }

    @WithTransaction
    public Uni<List<PriceHistory>> findByProductPaginated(Product product, int pageIndex, int pageSize) {
        Page page = Page.of(pageIndex, pageSize);
        return find("productId", product).page(page).list();
    }

    @WithTransaction
    public Uni<List<PriceHistory>> findByDateRangePaginated(Date startDate, Date endDate, int pageIndex, int pageSize) {
        Page page = Page.of(pageIndex, pageSize);
        return find("invoiceDate between ?1 and ?2", startDate, endDate).page(page).list();
    }

    @WithTransaction
    public Uni<BigDecimal> getCurrentPrice(Product product) {
        return findLatestByProduct(product)
                .onItem().transform(history -> history != null ? history.getPrice() : BigDecimal.ZERO);
    }

    @WithTransaction
    public Uni<PriceHistory> getPriceChange(Product product) {
        return find("select (max(price) - min(price)) from PriceHistory where productId = ?1", product)
                .singleResult();
    }

    @WithTransaction
    public Uni<PriceHistory> getMaxPriceByProduct(Product product) {
        return find("select max(price) from PriceHistory where productId = ?1", product)
                .singleResult();
    }

    @WithTransaction
    public Uni<PriceHistory> getMinPriceByProduct(Product product) {
        return find("select min(price) from PriceHistory where productId = ?1", product)
                .singleResult();
    }

    @WithTransaction
    public Uni<List<PriceHistory>> getPriceTrend(Product product, int limit) {
        return find("productId = ?1 order by invoiceDate desc", product).range(0, limit - 1).list();
    }

    @WithTransaction
    public Uni<Long> countPriceChangesByProduct(Product product) {
        return count("productId", product);
    }

    @WithTransaction
    public Uni<Long> countPriceChangesByDateRange(Date startDate, Date endDate) {
        return count("invoiceDate between ?1 and ?2", startDate, endDate);
    }

    @WithTransaction
    public Uni<Boolean> existsByProductAndDate(Product product, Date date) {
        return count("productId = ?1 and invoiceDate = ?2", product, date).map(count -> count > 0);
    }

    @WithTransaction
    public Uni<Boolean> hasPriceHistory(Product product) {
        return count("productId", product).map(count -> count > 0);
    }

    @WithTransaction
    public Uni<Long> deleteByProduct(Product product) {
        return delete("productId", product);
    }

    @WithTransaction
    public Uni<Long> deleteByDateRange(Date startDate, Date endDate) {
        return delete("invoiceDate between ?1 and ?2", startDate, endDate);
    }

    @WithTransaction
    public Uni<Integer> updatePrice(Long id, BigDecimal newPrice) {
        return update("price = ?1 where id = ?2", newPrice, id);
    }

    @WithTransaction
    public Uni<Integer> updateChangeDate(Long id, Date newDate) {
        return update("invoiceDate = ?1 where id = ?2", newDate, id);
    }

    @WithTransaction
    public Uni<Integer> updatePricesForProduct(Product product, BigDecimal newPrice) {
        return update("price = ?1 where productId = ?2", newPrice, product);
    }

    @WithTransaction
    public Uni<List<PriceHistory>> findRecentPriceChanges(int days) {
        Date recentDate = new Date(System.currentTimeMillis() - (long) days * 24 * 60 * 60 * 1000);
        return find("invoiceDate >= ?1", recentDate).list();
    }

    @WithTransaction
    public Uni<Long> getPageCountByProduct(Product product, int pageSize) {
        return count("productId", product)
                .map(count -> (count + pageSize - 1) / pageSize);
    }
}