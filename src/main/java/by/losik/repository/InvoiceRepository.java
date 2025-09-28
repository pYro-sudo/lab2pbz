package by.losik.repository;

import by.losik.entity.Customer;
import by.losik.entity.Invoice;
import by.losik.entity.Settlement;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@ApplicationScoped
public class InvoiceRepository extends BaseRepository<Invoice> {

    @WithTransaction
    public Uni<List<Invoice>> findByCustomerId(Long customerId) {
        return find("customer.id", customerId).list();
    }

    @WithTransaction
    public Uni<List<Invoice>> findBySettlementId(Long settlementId) {
        return find("settlement.id", settlementId).list();
    }

    @WithTransaction
    public Uni<List<Invoice>> findByEnterprise(String enterprise) {
        return find("enterprise", enterprise).list();
    }

    @WithTransaction
    public Uni<List<Invoice>> findByEnterpriseContaining(String enterprisePart) {
        return find("enterprise like ?1", "%" + enterprisePart + "%").list();
    }

    @WithTransaction
    public Uni<List<Invoice>> findByCustomer(Customer customer) {
        return find("customer", customer).list();
    }

    @WithTransaction
    public Uni<List<Invoice>> findBySettlement(Settlement settlement) {
        return find("settlement", settlement).list();
    }

    @WithTransaction
    public Uni<List<Invoice>> findByDate(Date date) {
        return find("invoiceDate", date).list();
    }

    @WithTransaction
    public Uni<List<Invoice>> findByDateRange(Date startDate, Date endDate) {
        return find("invoiceDate between ?1 and ?2", startDate, endDate).list();
    }

    @WithTransaction
    public Uni<List<Invoice>> findByDateBefore(Date date) {
        return find("invoiceDate < ?1", date).list();
    }

    @WithTransaction
    public Uni<List<Invoice>> findByDateAfter(Date date) {
        return find("invoiceDate > ?1", date).list();
    }

    @WithTransaction
    public Uni<List<Invoice>> findByAmountGreaterThan(BigDecimal minAmount) {
        return find("totalAmount > ?1", minAmount).list();
    }

    @WithTransaction
    public Uni<List<Invoice>> findByAmountLessThan(BigDecimal maxAmount) {
        return find("totalAmount < ?1", maxAmount).list();
    }

    @WithTransaction
    public Uni<List<Invoice>> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount) {
        return find("totalAmount between ?1 and ?2", minAmount, maxAmount).list();
    }

    @WithTransaction
    public Uni<List<Invoice>> findByCustomerPaginated(Customer customer, int pageIndex, int pageSize) {
        Page page = Page.of(pageIndex, pageSize);
        return find("customer", customer).page(page).list();
    }

    @WithTransaction
    public Uni<List<Invoice>> findByDateRangePaginated(Date startDate, Date endDate, int pageIndex, int pageSize) {
        Page page = Page.of(pageIndex, pageSize);
        return find("invoiceDate between ?1 and ?2", startDate, endDate).page(page).list();
    }

    @WithTransaction
    public Uni<Invoice> getTotalRevenueByCustomer(Customer customer) {
        return find("select sum(totalAmount) from Invoice where customer = ?1", customer)
                .singleResult();
    }

    @WithTransaction
    public Uni<Long> countByCustomer(Customer customer) {
        return count("customer", customer);
    }

    @WithTransaction
    public Uni<Long> countByDateRange(Date startDate, Date endDate) {
        return count("invoiceDate between ?1 and ?2", startDate, endDate);
    }

    @WithTransaction
    public Uni<Invoice> getAverageInvoiceAmount() {
        return find("select avg(totalAmount) from Invoice")
                .singleResult();
    }

    @WithTransaction
    public Uni<Invoice> getMaxInvoiceAmount() {
        return find("select max(totalAmount) from Invoice")
                .singleResult();
    }

    @WithTransaction
    public Uni<Invoice> getMinInvoiceAmount() {
        return find("select min(totalAmount) from Invoice")
                .singleResult();
    }

    @WithTransaction
    public Uni<Boolean> existsByCustomerAndDate(Customer customer, Date date) {
        return count("customer = ?1 and invoiceDate = ?2", customer, date).map(count -> count > 0);
    }

    @WithTransaction
    public Uni<Boolean> existsByEnterpriseAndDate(String enterprise, Date date) {
        return count("enterprise = ?1 and invoiceDate = ?2", enterprise, date).map(count -> count > 0);
    }

    @WithTransaction
    public Uni<Long> deleteByCustomer(Customer customer) {
        return delete("customer", customer);
    }

    @WithTransaction
    public Uni<Long> deleteByDateRange(Date startDate, Date endDate) {
        return delete("invoiceDate between ?1 and ?2", startDate, endDate);
    }

    @WithTransaction
    public Uni<Integer> updateTotalAmount(Long id, BigDecimal newAmount) {
        return update("totalAmount = ?1 where id = ?2", newAmount, id);
    }

    @WithTransaction
    public Uni<Integer> updateEnterprise(Long id, String newEnterprise) {
        return update("enterprise = ?1 where id = ?2", newEnterprise, id);
    }

    @WithTransaction
    public Uni<Integer> updateAmountsForCustomer(Customer customer, BigDecimal newAmount) {
        return update("totalAmount = ?1 where customer = ?2", newAmount, customer);
    }

    @WithTransaction
    public Uni<List<Invoice>> findTopInvoicesByAmount(int limit) {
        return find("order by totalAmount desc").range(0, limit - 1).list();
    }

    @WithTransaction
    public Uni<List<Invoice>> findRecentInvoices(int days) {
        Date recentDate = new Date(System.currentTimeMillis() - (long) days * 24 * 60 * 60 * 1000);
        return find("invoiceDate >= ?1", recentDate).list();
    }

    @WithTransaction
    public Uni<List<Invoice>> findInvoicesWithoutItems() {
        return find("id not in (select distinct invoice.id from InvoiceItem)").list();
    }

    @WithTransaction
    public Uni<Long> getPageCountByCustomer(Customer customer, int pageSize) {
        return count("customer", customer)
                .map(count -> (count + pageSize - 1) / pageSize);
    }

    @WithTransaction
    public Uni<Long> getPageCountByDateRange(Date startDate, Date endDate, int pageSize) {
        return count("invoiceDate between ?1 and ?2", startDate, endDate)
                .map(count -> (count + pageSize - 1) / pageSize);
    }
}