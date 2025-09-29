package by.losik.service;

import by.losik.entity.Product;
import by.losik.repository.ProductRepository;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.quarkus.cache.CacheKey;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Slf4j
public class ProductService extends BaseService<Product, ProductRepository> {

    @Inject
    ProductRepository productRepository;

    @Override
    protected String getEntityName() {
        return "Product";
    }

    @Override
    protected String getCachePrefix() {
        return "product";
    }

    @Inject
    public void setRepository(ProductRepository repository) {
        this.repository = repository;
    }

    @CacheResult(cacheName = "product-by-code")
    public Uni<Optional<Product>> findByCode(@CacheKey String code) {
        log.info("Finding product by code: {}", code);
        return productRepository.findByCode(code)
                .onItem().transform(product -> product)
                .onFailure().invoke(throwable ->
                        log.error("Error finding product by code: {}", code, throwable));
    }

    @CacheResult(cacheName = "product-by-category")
    public Uni<List<Product>> findByCategoryId(@CacheKey Long categoryId) {
        log.info("Finding products by category id: {}", categoryId);
        return productRepository.findByCategoryId(categoryId)
                .onItem().transform(products -> {
                    log.debug("Found {} products for category id: {}", products.size(), categoryId);
                    return products;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding products by category id: {}", categoryId, throwable));
    }

    @CacheResult(cacheName = "product-by-manufacturer")
    public Uni<List<Product>> findByManufacturer(@CacheKey String manufacturer) {
        log.info("Finding products by manufacturer: {}", manufacturer);
        return productRepository.findByManufacturer(manufacturer)
                .onItem().transform(products -> {
                    log.debug("Found {} products by manufacturer: {}", products.size(), manufacturer);
                    return products;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding products by manufacturer: {}", manufacturer, throwable));
    }

    @CacheResult(cacheName = "product-search")
    public Uni<List<Product>> searchProducts(@CacheKey String searchTerm) {
        log.info("Searching products with term: {}", searchTerm);
        return productRepository.searchProducts(searchTerm)
                .onItem().transform(products -> {
                    log.debug("Found {} products matching search term: {}", products.size(), searchTerm);
                    return products;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error searching products with term: {}", searchTerm, throwable));
    }

    @CacheResult(cacheName = "product-exists-by-code")
    public Uni<Boolean> existsByCode(@CacheKey String code) {
        log.info("Checking if product exists by code: {}", code);
        return productRepository.existsByCode(code)
                .onItem().transform(exists -> {
                    log.debug("Product exists by code {}: {}", code, exists);
                    return exists;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error checking if product exists by code: {}", code, throwable));
    }

    @CacheResult(cacheName = "product-exists-by-name-manufacturer")
    public Uni<Boolean> existsByNameAndManufacturer(@CacheKey String name, @CacheKey String manufacturer) {
        log.info("Checking if product exists by name: {} and manufacturer: {}", name, manufacturer);
        return productRepository.existsByNameAndManufacturer(name, manufacturer)
                .onItem().transform(exists -> {
                    log.debug("Product exists by name {} and manufacturer {}: {}", name, manufacturer, exists);
                    return exists;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error checking if product exists by name: {} and manufacturer: {}",
                                name, manufacturer, throwable));
    }

    public Uni<List<Product>> findByCodeContaining(String codePart) {
        log.info("Finding products by code containing: {}", codePart);
        return productRepository.findByCodeContaining(codePart)
                .onItem().transform(products -> {
                    log.debug("Found {} products with code containing: {}", products.size(), codePart);
                    return products;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding products by code containing: {}", codePart, throwable));
    }

    public Uni<List<Product>> findByNameContaining(String namePart) {
        log.info("Finding products by name containing: {}", namePart);
        return productRepository.findByNameContaining(namePart)
                .onItem().transform(products -> {
                    log.debug("Found {} products with name containing: {}", products.size(), namePart);
                    return products;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding products by name containing: {}", namePart, throwable));
    }

    public Uni<List<Product>> findByManufacturerContaining(String manufacturerPart) {
        log.info("Finding products by manufacturer containing: {}", manufacturerPart);
        return productRepository.findByManufacturerContaining(manufacturerPart)
                .onItem().transform(products -> {
                    log.debug("Found {} products with manufacturer containing: {}", products.size(), manufacturerPart);
                    return products;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error finding products by manufacturer containing: {}", manufacturerPart, throwable));
    }

    public Uni<List<Product>> searchProductsPaginated(String searchTerm, int pageIndex, int pageSize) {
        log.info("Searching products paginated, term: {}, pageIndex: {}, pageSize: {}",
                searchTerm, pageIndex, pageSize);
        return productRepository.searchProductsPaginated(searchTerm, pageIndex, pageSize)
                .onItem().transform(products -> {
                    log.debug("Found {} products for search paginated query", products.size());
                    return products;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error searching products paginated, term: {}, pageIndex: {}, pageSize: {}",
                                searchTerm, pageIndex, pageSize, throwable));
    }

    public Uni<Long> countByCategory(Long categoryId) {
        log.info("Counting products by category: {}", categoryId);
        return productRepository.countByCategory(categoryId)
                .onItem().transform(count -> {
                    log.debug("Found {} products for category: {}", count, categoryId);
                    return count;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error counting products by category: {}", categoryId, throwable));
    }

    public Uni<Long> countByManufacturer(String manufacturer) {
        log.info("Counting products by manufacturer: {}", manufacturer);
        return productRepository.countByManufacturer(manufacturer)
                .onItem().transform(count -> {
                    log.debug("Found {} products by manufacturer: {}", count, manufacturer);
                    return count;
                })
                .onFailure().invoke(throwable ->
                        log.error("Error counting products by manufacturer: {}", manufacturer, throwable));
    }

    @Override
    public Uni<Product> save(Product product) {
        log.info("Saving product: {}, code: {}", product.getName(), product.getCode());
        return super.save(product)
                .onItem().invoke(savedProduct ->
                        log.info("Successfully saved product: {} with id: {}", savedProduct.getName(), savedProduct.getId()));
    }

    public Uni<Integer> updateProductCode(Long id, String newCode) {
        log.info("Updating product code for id: {}, new code: {}", id, newCode);
        return productRepository.updateProductCode(id, newCode)
                .onItem().invoke(updatedCount -> {
                    if (updatedCount > 0) {
                        invalidateRelatedCaches();
                    }
                })
                .onFailure().invoke(throwable ->
                        log.error("Error updating product code for id: {}, new code: {}", id, newCode, throwable));
    }

    public Uni<Integer> updateProductName(Long id, String newName) {
        log.info("Updating product name for id: {}, new name: {}", id, newName);
        return productRepository.updateProductName(id, newName)
                .onItem().invoke(updatedCount -> {
                    if (updatedCount > 0) {
                        invalidateRelatedCaches();
                    }
                })
                .onFailure().invoke(throwable ->
                        log.error("Error updating product name for id: {}, new name: {}", id, newName, throwable));
    }

    public Uni<Long> deleteByCategory(Long categoryId) {
        log.info("Deleting products by category: {}", categoryId);
        return productRepository.deleteByCategory(categoryId)
                .onItem().invoke(count -> {
                    if (count > 0) {
                        invalidateRelatedCaches();
                    }
                })
                .onFailure().invoke(throwable ->
                        log.error("Error deleting products by category: {}", categoryId, throwable));
    }

    public Uni<Long> deleteByManufacturer(String manufacturer) {
        log.info("Deleting products by manufacturer: {}", manufacturer);
        return productRepository.deleteByManufacturer(manufacturer)
                .onItem().invoke(count -> {
                    if (count > 0) {
                        invalidateRelatedCaches();
                    }
                })
                .onFailure().invoke(throwable ->
                        log.error("Error deleting products by manufacturer: {}", manufacturer, throwable));
    }

    @Scheduled(cron = "0 0 * * * ?")
    @CacheInvalidateAll(cacheName = "product-by-id")
    @CacheInvalidateAll(cacheName = "product-by-name")
    @CacheInvalidateAll(cacheName = "product-all-sorted")
    @CacheInvalidateAll(cacheName = "product-exists-by-id")
    @CacheInvalidateAll(cacheName = "product-exists-by-name")
    @CacheInvalidateAll(cacheName = "product-count-all")
    @CacheInvalidateAll(cacheName = "product-by-code")
    @CacheInvalidateAll(cacheName = "product-by-category")
    @CacheInvalidateAll(cacheName = "product-by-manufacturer")
    @CacheInvalidateAll(cacheName = "product-search")
    @CacheInvalidateAll(cacheName = "product-exists-by-code")
    @CacheInvalidateAll(cacheName = "product-exists-by-name-manufacturer")
    public void scheduledCacheInvalidation() {
        log.debug("Scheduled cache invalidation for ProductService");
    }

    @Override
    protected void invalidateRelatedCaches() {
        scheduledCacheInvalidation();
    }
}