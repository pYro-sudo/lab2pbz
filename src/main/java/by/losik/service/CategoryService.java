package by.losik.service;

import by.losik.entity.Category;
import by.losik.repository.CategoryRepository;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.quarkus.cache.CacheKey;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Resource
@ApplicationScoped
@Slf4j
public class CategoryService extends BaseService<Category, CategoryRepository> {

    @Inject
    CategoryRepository categoryRepository;

    @Override
    protected String getEntityName() {
        return "Category";
    }

    @Override
    protected String getCachePrefix() {
        return "category";
    }

    @Inject
    public void setRepository(CategoryRepository repository) {
        this.repository = repository;
    }

    @CacheResult(cacheName = "category-starting-with-letter")
    public Uni<List<Category>> findCategoriesStartingWithLetter(@CacheKey String letter, int pageIndex, int pageSize) {
        log.info("Finding categories starting with letter: {}, pageIndex: {}, pageSize: {}", letter, pageIndex, pageSize);
        return categoryRepository.findCategoriesStartingWithLetter(letter, pageIndex, pageSize)
                .onItem().transform(categories -> {
                    log.debug("Found {} categories starting with letter: {}", categories.size(), letter);
                    return categories;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding categories starting with letter: {}", letter, throwable));
    }

    @CacheResult(cacheName = "category-count-starting-with-letter")
    public Uni<Long> countCategoriesStartingWithLetter(@CacheKey String letter) {
        log.info("Counting categories starting with letter: {}", letter);
        return categoryRepository.countCategoriesStartingWithLetter(letter)
                .onItem().transform(count -> {
                    log.debug("Found {} categories starting with letter: {}", count, letter);
                    return count;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error counting categories starting with letter: {}", letter, throwable));
    }

    @Override
    public Uni<Category> save(Category category) {
        log.info("Saving category: {}", category.getName());
        return super.save(category)
                .onItem().invoke(savedCategory ->
                        log.info("Successfully saved category: {} with id: {}", savedCategory.getName(), savedCategory.getId()));
    }

    @Scheduled(cron = "0 0 * * * ?")
    @CacheInvalidateAll(cacheName = "category-by-id")
    @CacheInvalidateAll(cacheName = "category-by-name")
    @CacheInvalidateAll(cacheName = "category-all-sorted")
    @CacheInvalidateAll(cacheName = "category-exists-by-id")
    @CacheInvalidateAll(cacheName = "category-exists-by-name")
    @CacheInvalidateAll(cacheName = "category-count-all")
    @CacheInvalidateAll(cacheName = "category-starting-with-letter")
    @CacheInvalidateAll(cacheName = "category-count-starting-with-letter")
    public void scheduledCacheInvalidation() {
        log.debug("Scheduled cache invalidation for CategoryService");
    }

    @Override
    protected void invalidateRelatedCaches() {
        scheduledCacheInvalidation();
    }
}