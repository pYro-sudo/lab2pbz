package by.losik.repository;

import by.losik.entity.Settlement;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class SettlementRepository extends BaseRepository<Settlement> {

    @WithTransaction
    public Uni<List<Settlement>> findByRegionId(Long regionId) {
        return find("region.id", regionId).list();
    }

    @WithTransaction
    public Uni<List<Settlement>> findByNameContaining(String namePart) {
        return find("name like ?1", "%" + namePart + "%").list();
    }

    @WithTransaction
    public Uni<List<Settlement>> findByNameContainingIgnoreCase(String namePart) {
        return find("LOWER(name) like LOWER(?1)", "%" + namePart + "%").list();
    }

    @WithTransaction
    public Uni<List<Settlement>> findByRegionIdAndName(Long regionId, String name) {
        return find("region.id = ?1 and name = ?2", regionId, name).list();
    }

    @WithTransaction
    public Uni<List<Settlement>> searchSettlements(String searchTerm) {
        String pattern = "%" + searchTerm + "%";
        return find("name like ?1 or region.name like ?1 or region.country like ?1", pattern).list();
    }

    @WithTransaction
    public Uni<List<Settlement>> searchSettlementsPaginated(String searchTerm, int pageIndex, int pageSize) {
        String pattern = "%" + searchTerm + "%";
        return find("name like ?1 or region.name like ?1 or region.country like ?1", pattern)
                .page(Page.of(pageIndex, pageSize))
                .list();
    }

    @WithTransaction
    public Uni<List<Settlement>> findSettlementsWithoutInvoices() {
        return getSession().flatMap(session ->
                session.createQuery("SELECT s FROM Settlement s WHERE s.id NOT IN (SELECT i.settlement.id FROM Invoice i)", Settlement.class)
                        .getResultList()
        );
    }

    @WithTransaction
    public Uni<List<Settlement>> findSettlementsWithInvoices() {
        return getSession().flatMap(session ->
                session.createQuery("SELECT DISTINCT s FROM Settlement s JOIN Invoice i ON i.settlement.id = s.id", Settlement.class)
                        .getResultList()
        );
    }

    @WithTransaction
    public Uni<List<Object[]>> getSettlementInvoiceStats() {
        return getSession().flatMap(session ->
                session.createQuery("SELECT s.name, r.name, COUNT(i), COALESCE(SUM(i.totalAmount), 0) " +
                                "FROM Settlement s " +
                                "JOIN s.region r " +
                                "LEFT JOIN Invoice i ON i.settlement.id = s.id " +
                                "GROUP BY s.id, s.name, r.name " +
                                "ORDER BY COUNT(i) DESC", Object[].class)
                        .getResultList()
        );
    }

    @WithTransaction
    public Uni<Long> countByRegionId(Long regionId) {
        return count("region.id", regionId);
    }

    @WithTransaction
    public Uni<Long> countByNamePattern(String namePattern) {
        return count("name like ?1", "%" + namePattern + "%");
    }

    @WithTransaction
    public Uni<Boolean> existsByName(String name) {
        return count("name", name).map(count -> count > 0);
    }

    @WithTransaction
    public Uni<Boolean> existsByNameAndRegionId(String name, Long regionId) {
        return count("name = ?1 and region.id = ?2", name, regionId).map(count -> count > 0);
    }

    @WithTransaction
    public Uni<Integer> updateSettlementName(Long id, String newName) {
        return update("name = ?1 where id = ?2", newName, id);
    }

    @WithTransaction
    public Uni<Integer> updateSettlementRegion(Long id, Long newRegionId) {
        return update("region.id = ?1 where id = ?2", newRegionId, id);
    }

    @WithTransaction
    public Uni<Integer> bulkUpdateRegionForSettlements(Long oldRegionId, Long newRegionId) {
        return update("set region.id = ?1 where region.id = ?2", newRegionId, oldRegionId);
    }

    @WithTransaction
    public Uni<Long> deleteByRegionId(Long regionId) {
        return delete("region.id", regionId);
    }

    @WithTransaction
    public Uni<Long> deleteByName(String name) {
        return delete("name", name);
    }

    @WithTransaction
    public Uni<List<Settlement>> findMostActiveSettlements(int limit) {
        return getSession().flatMap(session ->
                session.createQuery("SELECT s FROM Settlement s " +
                                "LEFT JOIN Invoice i ON i.settlement.id = s.id " +
                                "GROUP BY s.id, s.name " +
                                "ORDER BY COUNT(i) DESC", Settlement.class)
                        .setMaxResults(limit)
                        .getResultList()
        );
    }
}