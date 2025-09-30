package by.losik.repository;

import by.losik.entity.Category;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class CategoryRepository extends BaseRepository<Category> {

    @WithTransaction
    public Uni<List<Category>> findCategoriesStartingWithLetter(String letter, int pageIndex, int pageSize) {
        String pattern = letter + "%";
        return find("LOWER(name) LIKE LOWER(?1)", pattern)
                .page(Page.of(pageIndex, pageSize))
                .list();
    }

    @WithTransaction
    public Uni<Long> countCategoriesStartingWithLetter(String letter) {
        String pattern = letter + "%";
        return count("LOWER(name) LIKE LOWER(?1)", pattern);
    }

    @WithTransaction
    public Uni<List<Category>> findPaginatedSorted(int pageIndex, int pageSize, Sort sort) {
        Page page = Page.of(pageIndex, pageSize);
        return findAll(sort).page(page).list();
    }
}