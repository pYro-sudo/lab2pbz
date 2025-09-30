package by.losik.repository;

import by.losik.entity.Customer;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class CustomerRepository extends BaseRepository<Customer> {

    @WithTransaction
    public Uni<List<Customer>> findByLegalEntityStatus(Boolean isLegalEntity) {
        return find("isLegalEntity", isLegalEntity).list();
    }

    @WithTransaction
    public Uni<List<Customer>> findByDocument(String documentSeries, String documentNumber) {
        return find("documentSeries = ?1 and documentNumber = ?2", documentSeries, documentNumber).list();
    }

    @WithTransaction
    public Uni<List<Customer>> findLegalEntitiesWithBankAccounts() {
        return find("isLegalEntity = true and bankName is not null and bankAccount is not null and bankName != '' and bankAccount != ''").list();
    }

    @WithTransaction
    public Uni<List<Customer>> findIndividualsWithDocuments() {
        return find("isLegalEntity = false and documentSeries is not null and documentNumber is not null and documentSeries != '' and documentNumber != ''").list();
    }

    @WithTransaction
    public Uni<List<Customer>> findByLegalEntityPaginated(Boolean isLegalEntity, int pageIndex, int pageSize) {
        return findByFieldPaginated("isLegalEntity", isLegalEntity, pageIndex, pageSize);
    }

    @WithTransaction
    public Uni<Long> countByLegalEntityStatus(Boolean isLegalEntity) {
        return count("isLegalEntity", isLegalEntity);
    }

    @WithTransaction
    public Uni<Boolean> existsByDocument(String documentSeries, String documentNumber) {
        return count("documentSeries = ?1 and documentNumber = ?2", documentSeries, documentNumber)
                .map(count -> count > 0);
    }

    @WithTransaction
    public Uni<Integer> updateCustomerAddress(Long id, String newAddress) {
        return update("address = ?1 where id = ?2", newAddress, id);
    }

    @WithTransaction
    public Uni<Integer> updateBankDetails(Long id, String bankName, String bankAccount) {
        return update("bankName = ?1, bankAccount = ?2 where id = ?3", bankName, bankAccount, id);
    }

    @WithTransaction
    public Uni<Integer> markAllLegalEntitiesWithBank(String bankName) {
        return update("set bankName = ?1 where isLegalEntity = true", bankName);
    }

    @WithTransaction
    public Uni<List<Customer>> findByBankName(String bankName) {
        return find("bankName", bankName).list();
    }

    @WithTransaction
    public Uni<Long> getPageCountByName(String namePattern, int pageSize) {
        return count("name like ?1", "%" + namePattern + "%")
                .map(filteredCount -> (filteredCount + pageSize - 1) / pageSize);
    }

    @WithTransaction
    public Uni<List<Customer>> findCustomerStatsByLegalEntity() {
        return find("select isLegalEntity, count(*) from Customer group by isLegalEntity").list();
    }
}