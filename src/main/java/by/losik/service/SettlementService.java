package by.losik.service;

import by.losik.entity.Settlement;
import by.losik.repository.SettlementRepository;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@ApplicationScoped
@Slf4j
public class SettlementService extends BaseService<Settlement, SettlementRepository> {

    @Inject
    SettlementRepository settlementRepository;

    @Override
    protected String getEntityName() {
        return "Settlement";
    }

    @Override
    protected String getCachePrefix() {
        return "settlement";
    }

    @Inject
    public void setRepository(SettlementRepository repository) {
        this.repository = repository;
    }

    @CacheResult(cacheName = "settlement-by-region")
    public Uni<List<Settlement>> findByRegionId(@CacheKey Long regionId) {
        log.info("Finding settlements by region id: {}", regionId);
        return settlementRepository.findByRegionId(regionId)
                .onItem().transform(settlements -> {
                    log.debug("Found {} settlements for region id: {}", settlements.size(), regionId);
                    return settlements;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding settlements by region id: {}", regionId, throwable));
    }

    @CacheResult(cacheName = "settlement-search")
    public Uni<List<Settlement>> searchSettlements(@CacheKey String searchTerm) {
        log.info("Searching settlements with term: {}", searchTerm);
        return settlementRepository.searchSettlements(searchTerm)
                .onItem().transform(settlements -> {
                    log.debug("Found {} settlements matching search term: {}", settlements.size(), searchTerm);
                    return settlements;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error searching settlements with term: {}", searchTerm, throwable));
    }

    @CacheResult(cacheName = "settlement-exists-by-name-region")
    public Uni<Boolean> existsByNameAndRegionId(@CacheKey String name, @CacheKey Long regionId) {
        log.info("Checking if settlement exists by name: {} and region id: {}", name, regionId);
        return settlementRepository.existsByNameAndRegionId(name, regionId)
                .onItem().transform(exists -> {
                    log.debug("Settlement exists by name {} and region id {}: {}", name, regionId, exists);
                    return exists;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error checking if settlement exists by name: {} and region id: {}",
                                name, regionId, throwable));
    }

    public Uni<List<Settlement>> findByRegionIdAndName(Long regionId, String namePart) {
        log.info("Finding settlements by region id: {} and name containing: {}", regionId, namePart);
        return settlementRepository.findByRegionIdAndName(regionId, namePart)
                .onItem().transform(settlements -> {
                    log.debug("Found {} settlements for region id and name query", settlements.size());
                    return settlements;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding settlements by region id: {} and name containing: {}",
                                regionId, namePart, throwable));
    }

    public Uni<List<Settlement>> findByNameContaining(String namePart) {
        log.info("Finding settlements by name containing: {}", namePart);
        return settlementRepository.findByNameContaining(namePart)
                .onItem().transform(settlements -> {
                    log.debug("Found {} settlements with name containing: {}", settlements.size(), namePart);
                    return settlements;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding settlements by name containing: {}", namePart, throwable));
    }

    public Uni<List<Settlement>> searchSettlementsPaginated(String searchTerm, int pageIndex, int pageSize) {
        log.info("Searching settlements paginated, term: {}, pageIndex: {}, pageSize: {}",
                searchTerm, pageIndex, pageSize);
        return settlementRepository.searchSettlementsPaginated(searchTerm, pageIndex, pageSize)
                .onItem().transform(settlements -> {
                    log.debug("Found {} settlements for search paginated query", settlements.size());
                    return settlements;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error searching settlements paginated, term: {}, pageIndex: {}, pageSize: {}",
                                searchTerm, pageIndex, pageSize, throwable));
    }

    public Uni<Long> countByRegionId(Long regionId) {
        log.info("Counting settlements by region id: {}", regionId);
        return settlementRepository.countByRegionId(regionId)
                .onItem().transform(count -> {
                    log.debug("Found {} settlements for region id: {}", count, regionId);
                    return count;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error counting settlements by region id: {}", regionId, throwable));
    }

    @Override
    public Uni<Settlement> save(Settlement settlement) {
        log.info("Saving settlement: {}, region: {}",
                settlement.getName(),
                settlement.getRegion() != null ? settlement.getRegion().getId() : "null");
        return super.save(settlement)
                .onItem().invoke(savedSettlement ->
                        log.info("Successfully saved settlement: {} with id: {}",
                                savedSettlement.getName(), savedSettlement.getId()));
    }

    public Uni<Integer> updateSettlementName(Long id, String newName) {
        log.info("Updating settlement name for id: {}, new name: {}", id, newName);
        return settlementRepository.updateSettlementName(id, newName)
                .onItem().invoke(updatedCount -> {
                    if (updatedCount > 0) {
                        invalidateRelatedCaches();
                    }
                })
                .onFailure().invoke(throwable ->
                        log.error("Error updating settlement name for id: {}, new name: {}", id, newName, throwable));
    }

    public Uni<Integer> updateSettlementRegion(Long id, Long newRegionId) {
        log.info("Updating settlement region for id: {}, new region id: {}", id, newRegionId);
        return settlementRepository.updateSettlementRegion(id, newRegionId)
                .onItem().invoke(updatedCount -> {
                    if (updatedCount > 0) {
                        invalidateRelatedCaches();
                    }
                })
                .onFailure().invoke(throwable ->
                        log.error("Error updating settlement region for id: {}, new region id: {}",
                                id, newRegionId, throwable));
    }

    public Uni<Long> deleteByRegionId(Long regionId) {
        log.info("Deleting settlements by region id: {}", regionId);
        return settlementRepository.deleteByRegionId(regionId)
                .onItem().invoke(count -> {
                    if (count > 0) {
                        invalidateRelatedCaches();
                    }
                })
                .onFailure().invoke(throwable ->
                        log.error("Error deleting settlements by region id: {}", regionId, throwable));
    }

    public Uni<Integer> transferSettlementsToRegion(Long fromRegionId, Long toRegionId) {
        log.info("Transferring settlements from region: {} to region: {}", fromRegionId, toRegionId);
        return settlementRepository.bulkUpdateRegionForSettlements(fromRegionId, toRegionId)
                .onItem().invoke(updatedCount -> {
                    if (updatedCount > 0) {
                        invalidateRelatedCaches();
                    }
                })
                .onFailure().invoke(throwable ->
                        log.error("Error transferring settlements from region: {} to region: {}",
                                fromRegionId, toRegionId, throwable));
    }

    public Uni<List<Settlement>> findSettlementsWithoutInvoices() {
        log.info("Finding settlements without invoices");
        return settlementRepository.findSettlementsWithoutInvoices()
                .onItem().transform(settlements -> {
                    log.debug("Found {} settlements without invoices", settlements.size());
                    return settlements;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding settlements without invoices", throwable));
    }

    public Uni<List<Settlement>> findSettlementsWithInvoices() {
        log.info("Finding settlements with invoices");
        return settlementRepository.findSettlementsWithInvoices()
                .onItem().transform(settlements -> {
                    log.debug("Found {} settlements with invoices", settlements.size());
                    return settlements;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding settlements with invoices", throwable));
    }

    public Uni<List<Settlement>> findMostActiveSettlements(int limit) {
        log.info("Finding most active settlements, limit: {}", limit);
        return settlementRepository.findMostActiveSettlements(limit)
                .onItem().transform(settlements -> {
                    log.debug("Found {} most active settlements", settlements.size());
                    return settlements;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding most active settlements, limit: {}", limit, throwable));
    }

    public Uni<List<Object[]>> getSettlementInvoiceStats() {
        log.info("Getting settlement invoice statistics");
        return settlementRepository.getSettlementInvoiceStats()
                .onItem().transform(results -> {
                    log.debug("Found invoice stats for {} settlements", results.size());
                    return results;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error getting settlement invoice statistics", throwable));
    }

    @Scheduled(cron = "* 0 0/5 * * *")
    @CacheInvalidateAll(cacheName = "settlement-by-id")
    @CacheInvalidateAll(cacheName = "settlement-by-name")
    @CacheInvalidateAll(cacheName = "settlement-all-sorted")
    @CacheInvalidateAll(cacheName = "settlement-exists-by-id")
    @CacheInvalidateAll(cacheName = "settlement-exists-by-name")
    @CacheInvalidateAll(cacheName = "settlement-count-all")
    @CacheInvalidateAll(cacheName = "settlement-by-region")
    @CacheInvalidateAll(cacheName = "settlement-search")
    @CacheInvalidateAll(cacheName = "settlement-exists-by-name-region")
    public void scheduledCacheInvalidation() {
        log.debug("Scheduled cache invalidation for SettlementService");
    }

    @Override
    protected void invalidateRelatedCaches() {
        scheduledCacheInvalidation();
    }
}