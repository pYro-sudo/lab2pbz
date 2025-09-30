package by.losik.service;

import by.losik.repository.BaseRepository;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class BaseService<T, R extends BaseRepository<T>> {

    @Inject
    protected R repository;

    protected abstract String getEntityName();

    protected abstract String getCachePrefix();

    protected String cacheByName() { return getCachePrefix() + "-by-name"; }
    protected String cacheById() { return getCachePrefix() + "-by-id"; }
    protected String cacheAllSorted() { return getCachePrefix() + "-all-sorted"; }
    protected String cacheExistsByName() { return getCachePrefix() + "-exists-by-name"; }
    protected String cacheExistsById() { return getCachePrefix() + "-exists-by-id"; }
    protected String cacheCountAll() { return getCachePrefix() + "-count-all"; }

    @CacheResult(cacheName = "by-id")
    public Uni<T> findById(@CacheKey Long id) {
        log.info("Finding {} by id: {}", getEntityName(), id);
        return repository.findById(id)
                .onItem().transform(entity -> {
                    if (entity == null) {
                        log.warn("{} with id: {} not found", getEntityName(), id);
                    }
                    return entity;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding {} by id: {}", getEntityName(), id, throwable));
    }

    @CacheResult(cacheName = "by-name")
    public Uni<List<T>> findByName(@CacheKey String name) {
        log.info("Finding {} by name: {}", getEntityName(), name);
        return repository.findByField("name", name)
                .onItem().transform(entities -> {
                    log.debug("Found {} {} with name: {}", entities.size(), getEntityName(), name);
                    return entities;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding {} by name: {}", getEntityName(), name, throwable));
    }

    @CacheResult(cacheName = "all-sorted")
    public Uni<List<T>> findAllSorted(Sort sort) {
        log.info("Finding all {} sorted by: {}", getEntityName(), sort);
        return repository.findAllSorted(sort)
                .onItem().transform(entities -> {
                    log.debug("Found {} total {}", entities.size(), getEntityName());
                    return entities;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding all {} sorted by: {}", getEntityName(), sort, throwable));
    }

    public Uni<List<T>> findAllSortedByName() {
        return findAllSorted(Sort.by("name"));
    }

    public Uni<List<T>> findByNameContaining(String namePart) {
        log.info("Finding {} containing: {}", getEntityName(), namePart);
        return repository.findByNameContaining(namePart)
                .onFailure().invoke(throwable ->
                        log.error("Error finding {} containing: {}", getEntityName(), namePart, throwable));
    }

    public Uni<List<T>> findByNameContainingIgnoreCase(String namePart) {
        log.info("Finding {} containing (ignore case): {}", getEntityName(), namePart);
        return repository.findByNameContainingIgnoreCase(namePart)
                .onFailure().invoke(throwable ->
                        log.error("Error finding {} containing (ignore case): {}", getEntityName(), namePart, throwable));
    }

    public Uni<List<T>> findPaginated(int pageIndex, int pageSize) {
        log.info("Finding paginated {}, pageIndex: {}, pageSize: {}", getEntityName(), pageIndex, pageSize);
        return repository.findPaginated(pageIndex, pageSize)
                .onFailure().invoke(throwable ->
                        log.error("Error finding paginated {}, pageIndex: {}, pageSize: {}",
                                getEntityName(), pageIndex, pageSize, throwable));
    }

    public Uni<List<T>> findPaginatedSorted(int pageIndex, int pageSize, Sort sort) {
        log.info("Finding paginated sorted {}, pageIndex: {}, pageSize: {}, sort: {}",
                getEntityName(), pageIndex, pageSize, sort);
        return repository.findPaginatedSorted(pageIndex, pageSize, sort)
                .onFailure().invoke(throwable ->
                        log.error("Error finding paginated sorted {}, pageIndex: {}, pageSize: {}, sort: {}",
                                getEntityName(), pageIndex, pageSize, sort, throwable));
    }

    public Uni<List<T>> findByNamePaginated(String namePattern, int pageIndex, int pageSize) {
        log.info("Finding {} by name pattern paginated, pattern: {}, pageIndex: {}, pageSize: {}",
                getEntityName(), namePattern, pageIndex, pageSize);
        return repository.findByNamePaginated(namePattern, pageIndex, pageSize)
                .onFailure().invoke(throwable ->
                        log.error("Error finding {} by name pattern paginated, pattern: {}, pageIndex: {}, pageSize: {}",
                                getEntityName(), namePattern, pageIndex, pageSize, throwable));
    }

    @CacheResult(cacheName = "count-all")
    public Uni<Long> countAll() {
        log.info("Counting all {}", getEntityName());
        return repository.countAll()
                .onItem().transform(count -> {
                    log.debug("Total {} count: {}", getEntityName(), count);
                    return count;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error counting all {}", getEntityName(), throwable));
    }

    public Uni<Long> countByNamePattern(String namePattern) {
        log.info("Counting {} by name pattern: {}", getEntityName(), namePattern);
        return repository.countByNamePattern(namePattern)
                .onFailure().invoke(throwable ->
                        log.error("Error counting {} by name pattern: {}", getEntityName(), namePattern, throwable));
    }

    @CacheInvalidate(cacheName = "by-name")
    public Uni<Long> deleteByName(@CacheKey String name) {
        log.info("Deleting {} by name: {}", getEntityName(), name);
        return repository.deleteByName(name)
                .onItem().invoke(count -> {
                    if (count > 0) {
                        invalidateRelatedCaches();
                    }
                })
                .onFailure().invoke(throwable ->
                        log.error("Error deleting {} by name: {}", getEntityName(), name, throwable));
    }

    @CacheInvalidate(cacheName = "by-id")
    public Uni<Integer> updateName(@CacheKey Long id, String newName) {
        log.info("Updating {} name, id: {}, newName: {}", getEntityName(), id, newName);
        return repository.updateName(id, newName)
                .onItem().invoke(count -> {
                    if (count > 0) {
                        invalidateRelatedCaches();
                    }
                })
                .onFailure().invoke(throwable ->
                        log.error("Error updating {} name, id: {}, newName: {}", getEntityName(), id, newName, throwable));
    }

    @CacheResult(cacheName = "exists-by-name")
    public Uni<Boolean> existsByName(@CacheKey String name) {
        log.info("Checking if {} exists by name: {}", getEntityName(), name);
        return repository.existsByName(name)
                .onFailure().invoke(throwable ->
                        log.error("Error checking if {} exists by name: {}", getEntityName(), name, throwable));
    }

    @CacheResult(cacheName = "exists-by-id")
    public Uni<Boolean> existsById(@CacheKey Long id) {
        log.info("Checking if {} exists by id: {}", getEntityName(), id);
        return repository.existsById(id)
                .onFailure().invoke(throwable ->
                        log.error("Error checking if {} exists by id: {}", getEntityName(), id, throwable));
    }

    public Uni<List<T>> findTopN(int limit) {
        log.info("Finding top {} {}", limit, getEntityName());
        return repository.findTopN(limit)
                .onFailure().invoke(throwable ->
                        log.error("Error finding top {} {}", limit, getEntityName(), throwable));
    }

    public Uni<List<T>> findTopNSorted(int limit, Sort sort) {
        log.info("Finding top {} {} sorted by {}", limit, getEntityName(), sort);
        return repository.findTopNSorted(limit, sort)
                .onFailure().invoke(throwable ->
                        log.error("Error finding top {} {} sorted by {}", limit, getEntityName(), sort, throwable));
    }

    @CacheInvalidate(cacheName = "by-id")
    public Uni<T> save(@CacheKey T entity) {
        log.info("Saving {}", getEntityName());
        return repository.save(entity)
                .onItem().invoke(saved -> invalidateRelatedCaches())
                .onFailure().invoke(throwable ->
                        log.error("Error saving {}", getEntityName(), throwable));
    }

    @CacheInvalidate(cacheName = "by-id")
    public Uni<T> update(@CacheKey T entity) {
        log.info("Updating {}", getEntityName());
        return repository.update(entity)
                .onItem().invoke(updated -> invalidateRelatedCaches())
                .onFailure().invoke(throwable ->
                        log.error("Error updating {}", getEntityName(), throwable));
    }

    public Uni<Void> saveAll(List<T> entities) {
        log.info("Saving {} {}", entities.size(), getEntityName());
        return repository.saveAll(entities)
                .onItem().invoke(() -> invalidateRelatedCaches())
                .onFailure().invoke(throwable ->
                        log.error("Error saving {} {}", entities.size(), getEntityName(), throwable));
    }

    public Uni<Boolean> delete(T entity) {
        log.info("Deleting {}", getEntityName());
        return repository.delete(entity)
                .onItem().invoke(deleted -> {
                    invalidateRelatedCaches();
                }).replaceWith(true)
                .onFailure().invoke(throwable ->
                        log.error("Error deleting {}", getEntityName(), throwable));
    }

    public Uni<Boolean> deleteById(Long id) {
        log.info("Deleting {} by id: {}", getEntityName(), id);
        return repository.deleteById(id)
                .onItem().invoke(deleted -> {
                    if (deleted) {
                        invalidateRelatedCaches();
                    }
                })
                .onFailure().invoke(throwable ->
                        log.error("Error deleting {} by id: {}", getEntityName(), id, throwable));
    }

    protected void invalidateRelatedCaches() {
        log.debug("Invalidating related caches for {}", getEntityName());
        // This method can be overridden by subclasses for specific cache invalidation
    }
}