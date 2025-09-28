package by.losik;

import by.losik.entity.Customer;
import by.losik.service.CustomerService;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@QuarkusTest
class CustomerResourceTest {

    @InjectMock
    CustomerService customerService;

    private Customer createTestCustomer(Long id, String name, Boolean isLegalEntity) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        customer.setIsLegalEntity(isLegalEntity);
        return customer;
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetAllCustomers_Success() {
        List<Customer> customers = Arrays.asList(
                createTestCustomer(1L, "Customer One", false),
                createTestCustomer(2L, "Customer Two", true)
        );

        when(customerService.findPaginatedSorted(anyInt(), anyInt(), ArgumentMatchers.any(Sort.class)))
                .thenReturn(Uni.createFrom().item(customers));

        given()
                .when()
                .get("/api/customers?page=0&size=20&sort=name&direction=asc")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("Customer One"))
                .body("[1].name", is("Customer Two"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetAllCustomers_InvalidPage() {
        given()
                .when()
                .get("/api/customers?page=-1&size=20")
                .then()
                .statusCode(400)
                .body(containsString("Page index cannot be negative"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetAllCustomers_InvalidSize() {
        given()
                .when()
                .get("/api/customers?page=0&size=0")
                .then()
                .statusCode(400)
                .body(containsString("Page size must be between 1 and 100"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetCustomerById_Success() {
        Customer customer = createTestCustomer(1L, "Test Customer", false);

        when(customerService.findById(1L))
                .thenReturn(Uni.createFrom().item(customer));

        given()
                .when()
                .get("/api/customers/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("name", is("Test Customer"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetCustomerById_NotFound() {
        when(customerService.findById(999L))
                .thenReturn(Uni.createFrom().item((Customer) null));

        given()
                .when()
                .get("/api/customers/999")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetCustomerById_InvalidId() {
        given()
                .when()
                .get("/api/customers/0")
                .then()
                .statusCode(400)
                .body(containsString("Invalid customer ID"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateCustomer_Success() {
        Customer savedCustomer = createTestCustomer(1L, "New Customer", false);

        when(customerService.save(ArgumentMatchers.any(Customer.class)))
                .thenReturn(Uni.createFrom().item(savedCustomer));

        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"New Customer\", \"isLegalEntity\": false}")
                .when()
                .post("/api/customers")
                .then()
                .statusCode(201)
                .body("id", is(1))
                .body("name", is("New Customer"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateCustomer_MissingName() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"\"}")
                .when()
                .post("/api/customers")
                .then()
                .statusCode(400)
                .body(containsString("Customer name is required"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateCustomer_NameTooLong() {
        String longName = "A".repeat(201);

        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"" + longName + "\"}")
                .when()
                .post("/api/customers")
                .then()
                .statusCode(400)
                .body(containsString("Customer name must not exceed 200 characters"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateCustomer_Success() {
        Customer updatedCustomer = createTestCustomer(1L, "Updated Customer", false);

        when(customerService.update(ArgumentMatchers.any(Customer.class)))
                .thenReturn(Uni.createFrom().item(updatedCustomer));

        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"Updated Customer\", \"isLegalEntity\": false}")
                .when()
                .put("/api/customers/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("name", is("Updated Customer"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testDeleteCustomer_Success() {
        when(customerService.deleteById(1L))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .delete("/api/customers/1")
                .then()
                .statusCode(204);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetCustomersByLegalStatus_Success() {
        List<Customer> customers = Arrays.asList(
                createTestCustomer(1L, "Legal Corp", true),
                createTestCustomer(2L, "Another Corp", true)
        );

        when(customerService.findByLegalEntityPaginated(anyBoolean(), anyInt(), anyInt()))
                .thenReturn(Uni.createFrom().item(customers));

        given()
                .when()
                .get("/api/customers/legal-status/true?page=0&size=20")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("Legal Corp"))
                .body("[1].name", is("Another Corp"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetCustomersByLegalStatus_InvalidStatus() {
        given()
                .when()
                .get("/api/customers/legal-status/")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetCustomersByBankName_Success() {
        List<Customer> customers = Arrays.asList(
                createTestCustomer(1L, "Bank Customer", true),
                createTestCustomer(2L, "Another Bank Customer", true)
        );

        when(customerService.findByBankName("Alpha Bank"))
                .thenReturn(Uni.createFrom().item(customers));

        given()
                .when()
                .get("/api/customers/bank/Alpha%20Bank")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetCustomersByBankName_MissingName() {
        given()
                .when()
                .get("/api/customers/bank/")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetCustomersByDocument_Success() {
        List<Customer> customers = List.of(
                createTestCustomer(1L, "Document Customer", false)
        );

        when(customerService.findByDocument("AB", "123456"))
                .thenReturn(Uni.createFrom().item(customers));

        given()
                .when()
                .get("/api/customers/document?series=AB&number=123456")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].name", is("Document Customer"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetCustomersByDocument_MissingSeries() {
        given()
                .when()
                .get("/api/customers/document?number=123456")
                .then()
                .statusCode(400)
                .body(containsString("Document series is required"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetCustomersByDocument_MissingNumber() {
        given()
                .when()
                .get("/api/customers/document?series=AB")
                .then()
                .statusCode(400)
                .body(containsString("Document number is required"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetLegalEntitiesWithBankAccounts_Success() {
        List<Customer> customers = Arrays.asList(
                createTestCustomer(1L, "Legal Corp with Bank", true),
                createTestCustomer(2L, "Another Legal Corp", true)
        );

        when(customerService.findLegalEntitiesWithBankAccounts())
                .thenReturn(Uni.createFrom().item(customers));

        given()
                .when()
                .get("/api/customers/legal-entities/with-bank")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetIndividualsWithDocuments_Success() {
        List<Customer> customers = Arrays.asList(
                createTestCustomer(1L, "Individual with Docs", false),
                createTestCustomer(2L, "Another Individual", false)
        );

        when(customerService.findIndividualsWithDocuments())
                .thenReturn(Uni.createFrom().item(customers));

        given()
                .when()
                .get("/api/customers/individuals/with-docs")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCountByLegalStatus_Success() {
        when(customerService.countByLegalEntityStatus(true))
                .thenReturn(Uni.createFrom().item(15L));

        given()
                .when()
                .get("/api/customers/count/legal-status/true")
                .then()
                .statusCode(200)
                .body(is("15"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testExistsByDocument_Success() {
        when(customerService.existsByDocument("AB", "123456"))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .get("/api/customers/exists/document?series=AB&number=123456")
                .then()
                .statusCode(200)
                .body(is("true"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testExistsByName_Success() {
        when(customerService.existsByName("Test Customer"))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .get("/api/customers/exists/name/Test%20Customer")
                .then()
                .statusCode(200)
                .body(is("true"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateAddress_Success() {
        when(customerService.updateCustomerAddress(1L, "New Address 123"))
                .thenReturn(Uni.createFrom().item(1));

        given()
                .contentType(ContentType.TEXT)
                .body("New Address 123")
                .when()
                .put("/api/customers/1/address")
                .then()
                .statusCode(200)
                .body(containsString("Address updated successfully"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateAddress_NotFound() {
        when(customerService.updateCustomerAddress(999L, "New Address"))
                .thenReturn(Uni.createFrom().item(0));

        given()
                .contentType(ContentType.TEXT)
                .body("New Address")
                .when()
                .put("/api/customers/999/address")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateBankDetails_Success() {
        when(customerService.updateBankDetails(1L, "Alpha Bank", "ACC123456"))
                .thenReturn(Uni.createFrom().item(1));

        given()
                .when()
                .put("/api/customers/1/bank-details?bankName=Alpha%20Bank&bankAccount=ACC123456")
                .then()
                .statusCode(200)
                .body(containsString("Bank details updated successfully"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateBankDetails_MissingBankName() {
        given()
                .when()
                .put("/api/customers/1/bank-details?bankAccount=ACC123456")
                .then()
                .statusCode(400)
                .body(containsString("Bank name is required"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testSearchCustomers_Success() {
        List<Customer> customers = Arrays.asList(
                createTestCustomer(1L, "Search Result One", false),
                createTestCustomer(2L, "Search Result Two", true)
        );

        when(customerService.findByNamePaginated(anyString(), anyInt(), anyInt()))
                .thenReturn(Uni.createFrom().item(customers));

        given()
                .when()
                .get("/api/customers/search?q=search&page=0&size=20")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testSearchCustomers_MissingTerm() {
        given()
                .when()
                .get("/api/customers/search?q=")
                .then()
                .statusCode(400)
                .body(containsString("Search term is required"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetTotalCount_Success() {
        when(customerService.countAll())
                .thenReturn(Uni.createFrom().item(100L));

        given()
                .when()
                .get("/api/customers/total/count")
                .then()
                .statusCode(200)
                .body(is("100"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetSearchPageCount_Success() {
        when(customerService.getPageCountByName(anyString(), anyInt()))
                .thenReturn(Uni.createFrom().item(5L));

        given()
                .when()
                .get("/api/customers/search/page-count?q=test&size=20")
                .then()
                .statusCode(200)
                .body(is("5"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testMarkLegalEntitiesWithBank_Success() {
        when(customerService.markAllLegalEntitiesWithBank("Alpha Bank"))
                .thenReturn(Uni.createFrom().item(15));

        given()
                .when()
                .post("/api/customers/bulk/mark-legal-entities?bankName=Alpha%20Bank")
                .then()
                .statusCode(200)
                .body(containsString("Successfully marked 15 legal entities"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testMarkLegalEntitiesWithBank_MissingBankName() {
        given()
                .when()
                .post("/api/customers/bulk/mark-legal-entities")
                .then()
                .statusCode(400)
                .body(containsString("Bank name is required"));
    }

    @Test
    void testUnauthenticatedAccess() {
        given()
                .when()
                .get("/api/customers")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testServiceErrorHandling() {
        when(customerService.findById(1L))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Database error")));

        given()
                .when()
                .get("/api/customers/1")
                .then()
                .statusCode(500)
                .body(containsString("Error retrieving customer"));
    }
}