package by.losik.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;

import java.util.List;

public abstract class BaseRepository<T> implements PanacheRepository<T> {

    @WithTransaction
    public Uni<List<T>> findAllSorted(Sort sort) {
        return listAll(sort);
    }

    @WithTransaction
    public Uni<T> findById(Long id) {
        return find("id", id).firstResult();
    }

    @WithTransaction
    public Uni<List<T>> findByField(String fieldName, Object value) {
        return find(fieldName, value).list();
    }

    @WithTransaction
    public Uni<T> findOneByField(String fieldName, Object value) {
        return find(fieldName, value).firstResult();
    }

    @WithTransaction
    public Uni<List<T>> findByFieldContaining(String fieldName, String valuePart) {
        return find(fieldName + " like ?1", "%" + valuePart + "%").list();
    }

    @WithTransaction
    public Uni<List<T>> findByFieldContainingIgnoreCase(String fieldName, String valuePart) {
        return find("LOWER(" + fieldName + ") like LOWER(?1)", "%" + valuePart + "%").list();
    }

    @WithTransaction
    public Uni<List<T>> findPaginated(int pageIndex, int pageSize) {
        Page page = Page.of(pageIndex, pageSize);
        return findAll().page(page).list();
    }

    @WithTransaction
    public Uni<List<T>> findPaginatedSorted(int pageIndex, int pageSize, Sort sort) {
        Page page = Page.of(pageIndex, pageSize);
        return findAll(sort).page(page).list();
    }

    @WithTransaction
    public Uni<List<T>> findByFieldPaginated(String fieldName, Object value, int pageIndex, int pageSize) {
        Page page = Page.of(pageIndex, pageSize);
        return find(fieldName, value).page(page).list();
    }

    @WithTransaction
    public Uni<List<T>> findByFieldPatternPaginated(String fieldName, String pattern, int pageIndex, int pageSize) {
        Page page = Page.of(pageIndex, pageSize);
        return find(fieldName + " like ?1", "%" + pattern + "%").page(page).list();
    }

    @WithTransaction
    public Uni<Long> countAll() {
        return count();
    }

    @WithTransaction
    public Uni<Long> countByField(String fieldName, Object value) {
        return count(fieldName, value);
    }

    @WithTransaction
    public Uni<Long> countByFieldPattern(String fieldName, String pattern) {
        return count(fieldName + " like ?1", "%" + pattern + "%");
    }

    @WithTransaction
    public Uni<Boolean> existsByField(String fieldName, Object value) {
        return count(fieldName, value).map(count -> count > 0);
    }

    @WithTransaction
    public Uni<T> save(T entity) {
        return persist(entity);
    }

    @WithTransaction
    public Uni<T> update(T entity) {
        return getSession().flatMap(session -> session.merge(entity));
    }

    @WithTransaction
    public Uni<Long> deleteByField(String fieldName, Object value) {
        return delete(fieldName, value);
    }

    @WithTransaction
    public Uni<Integer> updateField(Long id, String fieldName, Object newValue) {
        return update(fieldName + " = ?1 where id = ?2", newValue, id);
    }

    @WithTransaction
    public Uni<Integer> updateFields(Long id, String setClause, Object... params) {
        String query = setClause + " where id = ?" + (params.length + 1);
        Object[] allParams = new Object[params.length + 1];
        System.arraycopy(params, 0, allParams, 0, params.length);
        allParams[params.length] = id;
        return update(query, allParams);
    }

    @WithTransaction
    public Uni<Void> saveAll(List<T> entities) {
        return persist(entities);
    }

    @WithTransaction
    public Uni<Long> getPageCount(int pageSize) {
        return count().map(totalCount -> (totalCount + pageSize - 1) / pageSize);
    }

    @WithTransaction
    public Uni<Long> getPageCountByFieldPattern(String fieldName, String pattern, int pageSize) {
        return count(fieldName + " like ?1", "%" + pattern + "%")
                .map(filteredCount -> (filteredCount + pageSize - 1) / pageSize);
    }

    @WithTransaction
    public Uni<List<T>> findTopN(int limit) {
        return findAll().range(0, limit - 1).list();
    }

    @WithTransaction
    public Uni<List<T>> findTopNSorted(int limit, Sort sort) {
        return findAll(sort).range(0, limit - 1).list();
    }

    // Entity-specific methods that can be overridden
    @WithTransaction
    public Uni<List<T>> findByName(String name) {
        return findByField("name", name);
    }

    @WithTransaction
    public Uni<List<T>> findByNameContaining(String namePart) {
        return findByFieldContaining("name", namePart);
    }

    @WithTransaction
    public Uni<List<T>> findByNameContainingIgnoreCase(String namePart) {
        return findByFieldContainingIgnoreCase("name", namePart);
    }

    @WithTransaction
    public Uni<List<T>> findByNamePaginated(String namePattern, int pageIndex, int pageSize) {
        return findByFieldPatternPaginated("name", namePattern, pageIndex, pageSize);
    }

    @WithTransaction
    public Uni<Long> countByNamePattern(String namePattern) {
        return countByFieldPattern("name", namePattern);
    }

    @WithTransaction
    public Uni<Long> deleteByName(String name) {
        return deleteByField("name", name);
    }

    @WithTransaction
    public Uni<Integer> updateName(Long id, String newName) {
        return updateField(id, "name", newName);
    }

    @WithTransaction
    public Uni<Boolean> existsByName(String name) {
        return existsByField("name", name);
    }

    @WithTransaction
    public Uni<Boolean> existsById(Long id) {
        return existsByField("id", id);
    }
}