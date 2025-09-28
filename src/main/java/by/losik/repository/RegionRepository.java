package by.losik.repository;

import by.losik.entity.Region;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class RegionRepository extends BaseRepository<Region> {

    @WithTransaction
    public Uni<List<Region>> findByCountry(String country) {
        return find("country", country).list();
    }

    @WithTransaction
    public Uni<List<Region>> findByCountryContaining(String countryPart) {
        return find("country like ?1", "%" + countryPart + "%").list();
    }

    @WithTransaction
    public Uni<List<Region>> findByName(String name) {
        return find("name", name).list();
    }

    @WithTransaction
    public Uni<List<Region>> findByNameContaining(String namePart) {
        return find("name like ?1", "%" + namePart + "%").list();
    }

    @WithTransaction
    public Uni<List<Region>> findByNameContainingIgnoreCase(String namePart) {
        return find("LOWER(name) like LOWER(?1)", "%" + namePart + "%").list();
    }

    @WithTransaction
    public Uni<List<Region>> searchRegions(String searchTerm) {
        String pattern = "%" + searchTerm + "%";
        return find("name like ?1 or country like ?1", pattern).list();
    }

    @WithTransaction
    public Uni<List<Region>> searchRegionsPaginated(String searchTerm, int pageIndex, int pageSize) {
        String pattern = "%" + searchTerm + "%";
        return find("name like ?1 or country like ?1", pattern)
                .page(Page.of(pageIndex, pageSize))
                .list();
    }

    @WithTransaction
    public Uni<Long> countByCountry(String country) {
        return count("country", country);
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
    public Uni<Integer> updateRegionName(Long id, String newName) {
        return update("name = ?1 where id = ?2", newName, id);
    }

    @WithTransaction
    public Uni<Long> deleteByCountry(String country) {
        return delete("country", country);
    }

    @WithTransaction
    public Uni<Long> deleteByName(String name) {
        return delete("name", name);
    }

    @WithTransaction
    public Uni<List<Region>> findByCountryAndName(String country, String namePart) {
        return find("country = ?1 and name like ?2", country, "%" + namePart + "%").list();
    }

    @WithTransaction
    public Uni<List<String>> findDistinctCountries() {
        return getSession().flatMap(session ->
                session.createQuery("SELECT DISTINCT r.country FROM Region r ORDER BY r.country", String.class)
                        .getResultList()
        );
    }

    @WithTransaction
    public Uni<Boolean> existsByNameAndCountry(String name, String country) {
        return count("name = ?1 and country = ?2", name, country).map(count -> count > 0);
    }

    @WithTransaction
    public Uni<Integer> updateRegionCountry(Long id, String newCountry) {
        return update("country = ?1 where id = ?2", newCountry, id);
    }

    @WithTransaction
    public Uni<Integer> updateCountryForRegions(String oldCountry, String newCountry) {
        return update("set country = ?1 where country = ?2", newCountry, oldCountry);
    }

    @WithTransaction
    public Uni<List<Region>> findRegionsWithNoSettlements() {
        return getSession().flatMap(session ->
                session.createQuery("SELECT r FROM Region r WHERE r.id NOT IN (SELECT s.region.id FROM Settlement s)", Region.class)
                        .getResultList()
        );
    }

    @WithTransaction
    public Uni<List<Object[]>> getSettlementCountByRegion() {
        return getSession().flatMap(session ->
                session.createQuery("SELECT r.name, COUNT(s) FROM Region r LEFT JOIN Settlement s ON s.region.id = r.id GROUP BY r.id, r.name ORDER BY COUNT(s) DESC", Object[].class)
                        .getResultList()
        );
    }
}