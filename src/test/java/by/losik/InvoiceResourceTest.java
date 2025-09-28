package by.losik;

import by.losik.entity.Customer;
import by.losik.entity.Invoice;
import by.losik.entity.Settlement;
import by.losik.service.InvoiceService;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@QuarkusTest
class InvoiceResourceTest {

    @InjectMock
    InvoiceService invoiceService;

    private Invoice createTestInvoice(Long id, String enterprise, BigDecimal amount) {
        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setEnterprise(enterprise);
        invoice.setTotalAmount(amount);
        invoice.setInvoiceDate(Date.valueOf("2024-01-01"));

        Customer customer = new Customer();
        customer.setId(1L);
        invoice.setCustomer(customer);

        Settlement settlement = new Settlement();
        settlement.setId(1L);
        invoice.setSettlement(settlement);

        return invoice;
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetAllInvoices_Success() {
        List<Invoice> invoices = Arrays.asList(
                createTestInvoice(1L, "Enterprise A", new BigDecimal("100.00")),
                createTestInvoice(2L, "Enterprise B", new BigDecimal("200.00"))
        );

        when(invoiceService.findPaginatedSorted(anyInt(), anyInt(), any(Sort.class)))
                .thenReturn(Uni.createFrom().item(invoices));

        given()
                .when()
                .get("/api/invoices?page=0&size=20&sort=invoiceDate&direction=asc")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].enterprise", is("Enterprise A"))
                .body("[1].enterprise", is("Enterprise B"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetAllInvoices_InvalidPage() {
        given()
                .when()
                .get("/api/invoices?page=-1&size=20")
                .then()
                .statusCode(400)
                .body(containsString("Page index cannot be negative"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetAllInvoices_InvalidSize() {
        given()
                .when()
                .get("/api/invoices?page=0&size=0")
                .then()
                .statusCode(400)
                .body(containsString("Page size must be between 1 and 100"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetInvoiceById_Success() {
        Invoice invoice = createTestInvoice(1L, "Enterprise A", new BigDecimal("100.00"));

        when(invoiceService.findById(1L))
                .thenReturn(Uni.createFrom().item(invoice));

        given()
                .when()
                .get("/api/invoices/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("enterprise", is("Enterprise A"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetInvoiceById_NotFound() {
        when(invoiceService.findById(999L))
                .thenReturn(Uni.createFrom().item((Invoice) null));

        given()
                .when()
                .get("/api/invoices/999")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetInvoiceById_InvalidId() {
        given()
                .when()
                .get("/api/invoices/0")
                .then()
                .statusCode(400)
                .body(containsString("Invalid invoice ID"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateInvoice_Success() {
        Invoice savedInvoice = createTestInvoice(1L, "New Enterprise", new BigDecimal("150.00"));

        when(invoiceService.save(any(Invoice.class)))
                .thenReturn(Uni.createFrom().item(savedInvoice));

        given()
                .contentType(ContentType.JSON)
                .body("{\"enterprise\": \"New Enterprise\", \"totalAmount\": 150.00, \"invoiceDate\": \"2024-01-01\", \"customer\": {\"id\": 1}, \"settlement\": {\"id\": 1}}")
                .when()
                .post("/api/invoices")
                .then()
                .statusCode(201)
                .body("id", is(1))
                .body("enterprise", is("New Enterprise"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateInvoice_MissingCustomer() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"enterprise\": \"New Enterprise\", \"totalAmount\": 150.00, \"invoiceDate\": \"2024-01-01\", \"settlement\": {\"id\": 1}}")
                .when()
                .post("/api/invoices")
                .then()
                .statusCode(400)
                .body(containsString("Customer is required"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateInvoice_InvalidAmount() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"enterprise\": \"New Enterprise\", \"totalAmount\": 0, \"invoiceDate\": \"2024-01-01\", \"customer\": {\"id\": 1}, \"settlement\": {\"id\": 1}}")
                .when()
                .post("/api/invoices")
                .then()
                .statusCode(400)
                .body(containsString("Total amount must be greater than 0"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateInvoice_Success() {
        Invoice updatedInvoice = createTestInvoice(1L, "Updated Enterprise", new BigDecimal("200.00"));

        when(invoiceService.update(any(Invoice.class)))
                .thenReturn(Uni.createFrom().item(updatedInvoice));

        given()
                .contentType(ContentType.JSON)
                .body("{\"enterprise\": \"Updated Enterprise\", \"totalAmount\": 200.00, \"invoiceDate\": \"2024-01-01\", \"customer\": {\"id\": 1}, \"settlement\": {\"id\": 1}}")
                .when()
                .put("/api/invoices/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("enterprise", is("Updated Enterprise"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateInvoice_InvalidId() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"enterprise\": \"Updated Enterprise\", \"totalAmount\": 200.00, \"invoiceDate\": \"2024-01-01\", \"customer\": {\"id\": 1}, \"settlement\": {\"id\": 1}}")
                .when()
                .put("/api/invoices/0")
                .then()
                .statusCode(400)
                .body(containsString("Invalid invoice ID"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testDeleteInvoice_Success() {
        when(invoiceService.deleteById(1L))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .delete("/api/invoices/1")
                .then()
                .statusCode(204);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testDeleteInvoice_NotFound() {
        when(invoiceService.deleteById(999L))
                .thenReturn(Uni.createFrom().item(false));

        given()
                .when()
                .delete("/api/invoices/999")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetInvoicesByCustomerId_Success() {
        List<Invoice> invoices = Arrays.asList(
                createTestInvoice(1L, "Enterprise A", new BigDecimal("100.00")),
                createTestInvoice(2L, "Enterprise B", new BigDecimal("200.00"))
        );

        when(invoiceService.findByCustomerId(1L))
                .thenReturn(Uni.createFrom().item(invoices));

        given()
                .when()
                .get("/api/invoices/customer/1")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].enterprise", is("Enterprise A"))
                .body("[1].enterprise", is("Enterprise B"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetInvoicesByCustomerId_InvalidId() {
        given()
                .when()
                .get("/api/invoices/customer/0")
                .then()
                .statusCode(400)
                .body(containsString("Invalid customer ID"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetInvoicesByDate_Success() {
        List<Invoice> invoices = List.of(
                createTestInvoice(1L, "Enterprise A", new BigDecimal("100.00"))
        );

        when(invoiceService.findByDate(any(Date.class)))
                .thenReturn(Uni.createFrom().item(invoices));

        given()
                .when()
                .get("/api/invoices/date/2024-01-01")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].enterprise", is("Enterprise A"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetInvoicesByDate_MissingDate() {
        given()
                .when()
                .get("/api/invoices/date/")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetInvoicesByDateRange_Success() {
        List<Invoice> invoices = Arrays.asList(
                createTestInvoice(1L, "Enterprise A", new BigDecimal("100.00")),
                createTestInvoice(2L, "Enterprise B", new BigDecimal("200.00"))
        );

        when(invoiceService.findByDateRange(any(Date.class), any(Date.class)))
                .thenReturn(Uni.createFrom().item(invoices));

        given()
                .when()
                .get("/api/invoices/date-range?start=2024-01-01&end=2024-01-31")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetInvoicesByDateRange_MissingParams() {
        given()
                .when()
                .get("/api/invoices/date-range")
                .then()
                .statusCode(400)
                .body(containsString("Start date and end date are required"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetInvoicesByEnterprise_Success() {
        List<Invoice> invoices = List.of(
                createTestInvoice(1L, "Test Enterprise", new BigDecimal("100.00"))
        );

        when(invoiceService.findByEnterprise("Test Enterprise"))
                .thenReturn(Uni.createFrom().item(invoices));

        given()
                .when()
                .get("/api/invoices/enterprise/Test Enterprise")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].enterprise", is("Test Enterprise"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetInvoicesByEnterprise_MissingName() {
        given()
                .when()
                .get("/api/invoices/enterprise/")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetInvoicesByAmountGreaterThan_Success() {
        List<Invoice> invoices = List.of(
                createTestInvoice(1L, "Enterprise A", new BigDecimal("500.00"))
        );

        when(invoiceService.findByAmountGreaterThan(any(BigDecimal.class)))
                .thenReturn(Uni.createFrom().item(invoices));

        given()
                .when()
                .get("/api/invoices/amount/greater-than/100")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].totalAmount", is(500));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetInvoicesByAmountGreaterThan_InvalidAmount() {
        given()
                .when()
                .get("/api/invoices/amount/greater-than/-1")
                .then()
                .statusCode(400)
                .body(containsString("Minimum amount must be non-negative"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetTopInvoicesByAmount_Success() {
        List<Invoice> invoices = Arrays.asList(
                createTestInvoice(1L, "Enterprise A", new BigDecimal("1000.00")),
                createTestInvoice(2L, "Enterprise B", new BigDecimal("900.00"))
        );

        when(invoiceService.findTopInvoicesByAmount(5))
                .thenReturn(Uni.createFrom().item(invoices));

        given()
                .when()
                .get("/api/invoices/top-invoices?limit=5")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetTopInvoicesByAmount_InvalidLimit() {
        given()
                .when()
                .get("/api/invoices/top-invoices?limit=0")
                .then()
                .statusCode(400)
                .body(containsString("Limit must be between 1 and 100"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetRecentInvoices_Success() {
        List<Invoice> invoices = List.of(
                createTestInvoice(1L, "Enterprise A", new BigDecimal("100.00"))
        );

        when(invoiceService.findRecentInvoices(30))
                .thenReturn(Uni.createFrom().item(invoices));

        given()
                .when()
                .get("/api/invoices/recent?days=30")
                .then()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetRecentInvoices_InvalidDays() {
        given()
                .when()
                .get("/api/invoices/recent?days=400")
                .then()
                .statusCode(400)
                .body(containsString("Days must be between 1 and 365"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetAverageInvoiceAmount_Success() {
        Invoice invoice = new Invoice();
        invoice.setTotalAmount(new BigDecimal("250.50"));

        when(invoiceService.getAverageInvoiceAmount())
                .thenReturn(Uni.createFrom().item(invoice));

        given()
                .when()
                .get("/api/invoices/stats/average-amount")
                .then()
                .statusCode(200)
                .body(is("250.5"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCountByCustomerId_Success() {
        when(invoiceService.countByCustomer(any(Customer.class)))
                .thenReturn(Uni.createFrom().item(5L));

        given()
                .when()
                .get("/api/invoices/customer/1/count")
                .then()
                .statusCode(200)
                .body(is("5"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateTotalAmount_Success() {
        when(invoiceService.updateTotalAmount(1L, new BigDecimal("300.00")))
                .thenReturn(Uni.createFrom().item(1));

        given()
                .when()
                .put("/api/invoices/1/total-amount?amount=300.00")
                .then()
                .statusCode(200)
                .body(containsString("Total amount updated successfully"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateTotalAmount_InvalidAmount() {
        given()
                .when()
                .put("/api/invoices/1/total-amount?amount=0")
                .then()
                .statusCode(400)
                .body(containsString("Amount must be greater than 0"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testDeleteInvoicesByCustomerId_Success() {
        when(invoiceService.deleteByCustomer(any(Customer.class)))
                .thenReturn(Uni.createFrom().item(3L));

        given()
                .when()
                .delete("/api/invoices/customer/1")
                .then()
                .statusCode(200)
                .body(containsString("Deleted 3 invoices"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetTotalCount_Success() {
        when(invoiceService.countAll())
                .thenReturn(Uni.createFrom().item(100L));

        given()
                .when()
                .get("/api/invoices/total/count")
                .then()
                .statusCode(200)
                .body(is("100"));
    }

    @Test
    void testUnauthenticatedAccess() {
        given()
                .when()
                .get("/api/invoices")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testServiceErrorHandling() {
        when(invoiceService.findById(1L))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Database error")));

        given()
                .when()
                .get("/api/invoices/1")
                .then()
                .statusCode(500)
                .body(containsString("Error retrieving invoice"));
    }
}