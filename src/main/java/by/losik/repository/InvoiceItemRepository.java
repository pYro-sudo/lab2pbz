package by.losik.repository;

import by.losik.entity.Invoice;
import by.losik.entity.InvoiceItem;
import by.losik.entity.Product;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@ApplicationScoped
public class InvoiceItemRepository extends BaseRepository<InvoiceItem> {

    @WithTransaction
    public Uni<List<InvoiceItem>> findByInvoiceId(Long invoiceId) {
        return find("invoiceId.id", invoiceId).list();
    }

    @WithTransaction
    public Uni<List<InvoiceItem>> findByProductId(Long productId) {
        return find("productId.id", productId).list();
    }

    @WithTransaction
    public Uni<List<InvoiceItem>> findByInvoiceIdPaginated(Long invoiceId, int pageIndex, int pageSize) {
        return find("invoice.id", invoiceId)
                .page(Page.of(pageIndex, pageSize))
                .list();
    }

    @WithTransaction
    public Uni<List<InvoiceItem>> findByQuantityGreaterThan(BigInteger quantity) {
        return find("quantity > ?1", quantity).list();
    }

    @WithTransaction
    public Uni<List<InvoiceItem>> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return find("price between ?1 and ?2", minPrice, maxPrice).list();
    }

    @WithTransaction
    public Uni<List<InvoiceItem>> findByInvoice(Invoice invoice) {
        return find("invoice", invoice).list();
    }

    @WithTransaction
    public Uni<List<InvoiceItem>> findByProduct(Product product) {
        return find("product", product).list();
    }

    @WithTransaction
    public Uni<Long> countByInvoice(Invoice invoice) {
        return count("invoice", invoice);
    }

    @WithTransaction
    public Uni<List<InvoiceItem>> findByQuantityLessThan(BigInteger maxQuantity) {
        return find("quantity < ?1", maxQuantity).list();
    }

    @WithTransaction
    public Uni<List<InvoiceItem>> findByQuantityBetween(BigInteger minQuantity, BigInteger maxQuantity) {
        return find("quantity between ?1 and ?2", minQuantity, maxQuantity).list();
    }

    @WithTransaction
    public Uni<List<InvoiceItem>> findByPriceGreaterThan(BigDecimal minPrice) {
        return find("price > ?1", minPrice).list();
    }

    @WithTransaction
    public Uni<List<InvoiceItem>> findByPriceLessThan(BigDecimal maxPrice) {
        return find("price < ?1", maxPrice).list();
    }

    @WithTransaction
    public Uni<List<InvoiceItem>> findByInvoicePaginated(Invoice invoice, int pageIndex, int pageSize) {
        Page page = Page.of(pageIndex, pageSize);
        return find("invoice", invoice).page(page).list();
    }

    @WithTransaction
    public Uni<Boolean> existsByInvoiceAndProduct(Invoice invoice, Product product) {
        return count("invoice = ?1 and product = ?2", invoice, product).map(count -> count > 0);
    }

    @WithTransaction
    public Uni<Boolean> existsByInvoiceIdAndProductId(Long invoiceId, Long productId) {
        return count("invoice.id = ?1 and product.id = ?2", invoiceId, productId).map(count -> count > 0);
    }

    @WithTransaction
    public Uni<Long> deleteByInvoice(Invoice invoice) {
        return delete("invoice", invoice);
    }

    @WithTransaction
    public Uni<Long> deleteByInvoiceId(Long invoiceId) {
        return delete("invoice.id", invoiceId);
    }

    @WithTransaction
    public Uni<Long> deleteByProduct(Product product) {
        return delete("product", product);
    }

    @WithTransaction
    public Uni<Integer> updateQuantity(Long id, BigInteger newQuantity) {
        return update("quantity = ?1 where id = ?2", newQuantity, id);
    }

    @WithTransaction
    public Uni<Integer> updatePrice(Long id, BigDecimal newPrice) {
        return update("price = ?1 where id = ?2", newPrice, id);
    }

    @WithTransaction
    public Uni<Integer> updateQuantityAndPrice(Long id, BigInteger newQuantity, BigDecimal newPrice) {
        return update("quantity = ?1, price = ?2 where id = ?3", newQuantity, newPrice, id);
    }

    @WithTransaction
    public Uni<Integer> updatePricesForProduct(Product product, BigDecimal newPrice) {
        return update("price = ?1 where product = ?2", newPrice, product);
    }

    @WithTransaction
    public Uni<List<InvoiceItem>> findTopSellingItems(int limit) {
        return find("order by quantity desc").range(0, limit - 1).list();
    }

    @WithTransaction
    public Uni<List<InvoiceItem>> findHighValueItems(BigDecimal minValue) {
        return find("price * quantity > ?1", minValue).list();
    }

    @WithTransaction
    public Uni<InvoiceItem> getTotalRevenue() {
        return find("select sum(price * quantity) from InvoiceItem").singleResult();
    }

    @WithTransaction
    public Uni<Long> getPageCountByInvoice(Invoice invoice, int pageSize) {
        return count("invoice", invoice)
                .map(count -> (count + pageSize - 1) / pageSize);
    }
}