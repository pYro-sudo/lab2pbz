package by.losik.service;

import by.losik.entity.Region;
import by.losik.repository.RegionRepository;
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
public class RegionService extends BaseService<Region, RegionRepository> {

    @Inject
    RegionRepository regionRepository;

    @Override
    protected String getEntityName() {
        return "Region";
    }

    @Override
    protected String getCachePrefix() {
        return "region";
    }

    @Inject
    public void setRepository(RegionRepository repository) {
        this.repository = repository;
    }

    @CacheResult(cacheName = "region-by-country")
    public Uni<List<Region>> findByCountry(@CacheKey String country) {
        log.info("Finding regions by country: {}", country);
        return regionRepository.findByCountry(country)
                .onItem().transform(regions -> {
                    log.debug("Found {} regions for country: {}", regions.size(), country);
                    return regions;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding regions by country: {}", country, throwable));
    }

    @CacheResult(cacheName = "region-search")
    public Uni<List<Region>> searchRegions(@CacheKey String searchTerm) {
        log.info("Searching regions with term: {}", searchTerm);
        return regionRepository.searchRegions(searchTerm)
                .onItem().transform(regions -> {
                    log.debug("Found {} regions matching search term: {}", regions.size(), searchTerm);
                    return regions;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error searching regions with term: {}", searchTerm, throwable));
    }

    @CacheResult(cacheName = "region-distinct-countries")
    public Uni<List<String>> findDistinctCountries() {
        log.info("Finding distinct countries");
        return regionRepository.findDistinctCountries()
                .onItem().transform(countries -> {
                    log.debug("Found {} distinct countries", countries.size());
                    return countries;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding distinct countries", throwable));
    }

    @CacheResult(cacheName = "region-exists-by-name-country")
    public Uni<Boolean> existsByNameAndCountry(@CacheKey String name, @CacheKey String country) {
        log.info("Checking if region exists by name: {} and country: {}", name, country);
        return regionRepository.existsByNameAndCountry(name, country)
                .onItem().transform(exists -> {
                    log.debug("Region exists by name {} and country {}: {}", name, country, exists);
                    return exists;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error checking if region exists by name: {} and country: {}",
                                name, country, throwable));
    }

    public Uni<List<Region>> findByCountryContaining(String countryPart) {
        log.info("Finding regions by country containing: {}", countryPart);
        return regionRepository.findByCountryContaining(countryPart)
                .onItem().transform(regions -> {
                    log.debug("Found {} regions with country containing: {}", regions.size(), countryPart);
                    return regions;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding regions by country containing: {}", countryPart, throwable));
    }

    public Uni<List<Region>> findByNameContaining(String namePart) {
        log.info("Finding regions by name containing: {}", namePart);
        return regionRepository.findByNameContaining(namePart)
                .onItem().transform(regions -> {
                    log.debug("Found {} regions with name containing: {}", regions.size(), namePart);
                    return regions;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding regions by name containing: {}", namePart, throwable));
    }

    public Uni<List<Region>> findByCountryAndName(String country, String namePart) {
        log.info("Finding regions by country: {} and name containing: {}", country, namePart);
        return regionRepository.findByCountryAndName(country, namePart)
                .onItem().transform(regions -> {
                    log.debug("Found {} regions for country and name query", regions.size());
                    return regions;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding regions by country: {} and name containing: {}",
                                country, namePart, throwable));
    }

    public Uni<List<Region>> searchRegionsPaginated(String searchTerm, int pageIndex, int pageSize) {
        log.info("Searching regions paginated, term: {}, pageIndex: {}, pageSize: {}",
                searchTerm, pageIndex, pageSize);
        return regionRepository.searchRegionsPaginated(searchTerm, pageIndex, pageSize)
                .onItem().transform(regions -> {
                    log.debug("Found {} regions for search paginated query", regions.size());
                    return regions;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error searching regions paginated, term: {}, pageIndex: {}, pageSize: {}",
                                searchTerm, pageIndex, pageSize, throwable));
    }

    public Uni<Long> countByCountry(String country) {
        log.info("Counting regions by country: {}", country);
        return regionRepository.countByCountry(country)
                .onItem().transform(count -> {
                    log.debug("Found {} regions for country: {}", count, country);
                    return count;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error counting regions by country: {}", country, throwable));
    }

    @Override
    public Uni<Region> save(Region region) {
        log.info("Saving region: {}, country: {}", region.getName(), region.getCountry());
        return super.save(region)
                .onItem().invoke(savedRegion ->
                        log.info("Successfully saved region: {} with id: {}", savedRegion.getName(), savedRegion.getId()));
    }

    public Uni<Integer> updateRegionName(Long id, String newName) {
        log.info("Updating region name for id: {}, new name: {}", id, newName);
        return regionRepository.updateRegionName(id, newName)
                .onItem().invoke(updatedCount -> {
                    if (updatedCount > 0) {
                        invalidateRelatedCaches();
                    }
                })
                .onFailure().invoke(throwable ->
                        log.error("Error updating region name for id: {}, new name: {}", id, newName, throwable));
    }

    public Uni<Integer> updateRegionCountry(Long id, String newCountry) {
        log.info("Updating region country for id: {}, new country: {}", id, newCountry);
        return regionRepository.updateRegionCountry(id, newCountry)
                .onItem().invoke(updatedCount -> {
                    if (updatedCount > 0) {
                        invalidateRelatedCaches();
                    }
                })
                .onFailure().invoke(throwable ->
                        log.error("Error updating region country for id in Regions"));
    }

    public Uni<Long> deleteByCountry(String country) {
        log.info("Deleting regions by country: {}", country);
        return regionRepository.deleteByCountry(country)
                .onItem().invoke(count -> {
                    if (count > 0) {
                        invalidateRelatedCaches();
                    }
                })
                .onFailure().invoke(throwable ->
                        log.error("Error deleting regions by country: {}", country, throwable));
    }

    public Uni<Integer> updateCountryForRegions(String oldCountry, String newCountry) {
        log.info("Updating country for regions from: {} to: {}", oldCountry, newCountry);
        return regionRepository.updateCountryForRegions(oldCountry, newCountry)
                .onItem().invoke(updatedCount -> {
                    if (updatedCount > 0) {
                        invalidateRelatedCaches();
                    }
                })
                .onFailure().invoke(throwable ->
                        log.error("Error updating country for regions from: {} to: {}",
                                oldCountry, newCountry, throwable));
    }

    public Uni<List<Region>> findRegionsWithoutSettlements() {
        log.info("Finding regions without settlements");
        return regionRepository.findRegionsWithNoSettlements()
                .onItem().transform(regions -> {
                    log.debug("Found {} regions without settlements", regions.size());
                    return regions;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding regions without settlements", throwable));
    }

    public Uni<List<Object[]>> getSettlementCountByRegion() {
        log.info("Getting settlement count by region");
        return regionRepository.getSettlementCountByRegion()
                .onItem().transform(results -> {
                    log.debug("Found settlement count for {} regions", results.size());
                    return results;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error getting settlement count by region", throwable));
    }

    @Scheduled(cron = "0 0 * * * ?")
    @CacheInvalidateAll(cacheName = "region-by-id")
    @CacheInvalidateAll(cacheName = "region-by-name")
    @CacheInvalidateAll(cacheName = "region-all-sorted")
    @CacheInvalidateAll(cacheName = "region-exists-by-id")
    @CacheInvalidateAll(cacheName = "region-exists-by-name")
    @CacheInvalidateAll(cacheName = "region-count-all")
    @CacheInvalidateAll(cacheName = "region-by-country")
    @CacheInvalidateAll(cacheName = "region-search")
    @CacheInvalidateAll(cacheName = "region-distinct-countries")
    @CacheInvalidateAll(cacheName = "region-exists-by-name-country")
    public void scheduledCacheInvalidation() {
        log.debug("Scheduled cache invalidation for RegionService");
    }

    @Override
    protected void invalidateRelatedCaches() {
        scheduledCacheInvalidation();
    }
}