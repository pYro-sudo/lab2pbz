package by.losik.repository;

import by.losik.entity.Product;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProductRepository extends BaseRepository<Product> {

    @WithTransaction
    public Uni<List<Product>> findByManufacturer(String manufacturer) {
        return find("manufacturer", manufacturer).list();
    }

    @WithTransaction
    public Uni<List<Product>> findByManufacturerContaining(String manufacturerPart) {
        return find("manufacturer like ?1", "%" + manufacturerPart + "%").list();
    }

    @WithTransaction
    public Uni<List<Product>> findByCategoryId(Long categoryId) {
        return find("category.id", categoryId).list();
    }

    @WithTransaction
    public Uni<List<Product>> findByCodeContaining(String codePart) {
        return find("code like ?1", "%" + codePart + "%").list();
    }

    @WithTransaction
    public Uni<Optional<Product>> findByCode(String code) {
        return find("code", code).firstResult().map(Optional::ofNullable);
    }

    @WithTransaction
    public Uni<List<Product>> findByNameContaining(String namePart) {
        return find("name like ?1", "%" + namePart + "%").list();
    }

    @WithTransaction
    public Uni<List<Product>> findByNameContainingIgnoreCase(String namePart) {
        return find("LOWER(name) like LOWER(?1)", "%" + namePart + "%").list();
    }

    @WithTransaction
    public Uni<List<Product>> searchProducts(String searchTerm) {
        String pattern = "%" + searchTerm + "%";
        return find("name like ?1 or code like ?1 or manufacturer like ?1", pattern).list();
    }

    @WithTransaction
    public Uni<List<Product>> searchProductsPaginated(String searchTerm, int pageIndex, int pageSize) {
        String pattern = "%" + searchTerm + "%";
        return find("name like ?1 or code like ?1 or manufacturer like ?1", pattern)
                .page(Page.of(pageIndex, pageSize))
                .list();
    }

    @WithTransaction
    public Uni<Long> countByCategory(Long categoryId) {
        return count("category.id", categoryId);
    }

    @WithTransaction
    public Uni<Long> countByManufacturer(String manufacturer) {
        return count("manufacturer", manufacturer);
    }

    @WithTransaction
    public Uni<Boolean> existsByCode(String code) {
        return count("code", code).map(count -> count > 0);
    }

    @WithTransaction
    public Uni<Boolean> existsByNameAndManufacturer(String name, String manufacturer) {
        return count("name = ?1 and manufacturer = ?2", name, manufacturer).map(count -> count > 0);
    }

    @WithTransaction
    public Uni<Integer> updateProductCode(Long id, String newCode) {
        return update("code = ?1 where id = ?2", newCode, id);
    }

    @WithTransaction
    public Uni<Integer> updateProductName(Long id, String newName) {
        return update("name = ?1 where id = ?2", newName, id);
    }

    @WithTransaction
    public Uni<Long> deleteByCategory(Long categoryId) {
        return delete("category.id", categoryId);
    }

    @WithTransaction
    public Uni<Long> deleteByManufacturer(String manufacturer) {
        return delete("manufacturer", manufacturer);
    }
}