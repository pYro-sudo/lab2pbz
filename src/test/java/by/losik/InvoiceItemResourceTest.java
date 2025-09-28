package by.losik;

import by.losik.entity.Invoice;
import by.losik.entity.InvoiceItem;
import by.losik.entity.Product;
import by.losik.service.InvoiceItemService;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@QuarkusTest
class InvoiceItemResourceTest {

    @InjectMock
    InvoiceItemService invoiceItemService;

    private InvoiceItem createTestInvoiceItem(Long id, Long invoiceId, Long productId, BigInteger quantity, BigDecimal price) {
        InvoiceItem item = new InvoiceItem();
        item.setId(id);

        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);
        item.setInvoice(invoice);

        Product product = new Product();
        product.setId(productId);
        item.setProduct(product);

        item.setQuantity(quantity);
        item.setPrice(price);
        return item;
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetAllInvoiceItems_Success() {
        List<InvoiceItem> items = Arrays.asList(
                createTestInvoiceItem(1L, 1L, 1L, BigInteger.valueOf(10), new BigDecimal("100.50")),
                createTestInvoiceItem(2L, 1L, 2L, BigInteger.valueOf(5), new BigDecimal("50.25"))
        );

        when(invoiceItemService.findPaginatedSorted(anyInt(), anyInt(), any(Sort.class)))
                .thenReturn(Uni.createFrom().item(items));

        given()
                .when()
                .get("/api/invoice-items?page=0&size=20&sort=id&direction=asc")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].id", is(1))
                .body("[1].id", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetAllInvoiceItems_InvalidPage() {
        given()
                .when()
                .get("/api/invoice-items?page=-1&size=20")
                .then()
                .statusCode(400)
                .body(containsString("Page index cannot be negative"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetAllInvoiceItems_InvalidSize() {
        given()
                .when()
                .get("/api/invoice-items?page=0&size=0")
                .then()
                .statusCode(400)
                .body(containsString("Page size must be between 1 and 100"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetInvoiceItemById_Success() {
        InvoiceItem item = createTestInvoiceItem(1L, 1L, 1L, BigInteger.valueOf(10), new BigDecimal("100.50"));

        when(invoiceItemService.findById(1L))
                .thenReturn(Uni.createFrom().item(item));

        given()
                .when()
                .get("/api/invoice-items/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("quantity", is(10))
                .body("price", is(100.50F));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetInvoiceItemById_NotFound() {
        when(invoiceItemService.findById(999L))
                .thenReturn(Uni.createFrom().item((InvoiceItem) null));

        given()
                .when()
                .get("/api/invoice-items/999")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetInvoiceItemById_InvalidId() {
        given()
                .when()
                .get("/api/invoice-items/0")
                .then()
                .statusCode(400)
                .body(containsString("Invalid invoice item ID"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateInvoiceItem_Success() {
        InvoiceItem savedItem = createTestInvoiceItem(1L, 1L, 1L, BigInteger.valueOf(10), new BigDecimal("100.50"));

        when(invoiceItemService.save(any(InvoiceItem.class)))
                .thenReturn(Uni.createFrom().item(savedItem));

        given()
                .contentType(ContentType.JSON)
                .body("{\"invoice\": {\"id\": 1}, \"product\": {\"id\": 1}, \"quantity\": 10, \"price\": 100.50}")
                .when()
                .post("/api/invoice-items")
                .then()
                .statusCode(201)
                .body("id", is(1))
                .body("quantity", is(10))
                .body("price", is(100.50F));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateInvoiceItem_MissingInvoice() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"product\": {\"id\": 1}, \"quantity\": 10, \"price\": 100.50}")
                .when()
                .post("/api/invoice-items")
                .then()
                .statusCode(400)
                .body(containsString("Invoice is required"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateInvoiceItem_InvalidQuantity() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"invoice\": {\"id\": 1}, \"product\": {\"id\": 1}, \"quantity\": 0, \"price\": 100.50}")
                .when()
                .post("/api/invoice-items")
                .then()
                .statusCode(400)
                .body(containsString("Quantity must be greater than 0"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateInvoiceItem_Success() {
        InvoiceItem updatedItem = createTestInvoiceItem(1L, 1L, 1L, BigInteger.valueOf(15), new BigDecimal("150.75"));

        when(invoiceItemService.update(any(InvoiceItem.class)))
                .thenReturn(Uni.createFrom().item(updatedItem));

        given()
                .contentType(ContentType.JSON)
                .body("{\"invoice\": {\"id\": 1}, \"product\": {\"id\": 1}, \"quantity\": 15, \"price\": 150.75}")
                .when()
                .put("/api/invoice-items/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("quantity", is(15))
                .body("price", is(150.75F));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateInvoiceItem_InvalidId() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"invoice\": {\"id\": 1}, \"product\": {\"id\": 1}, \"quantity\": 10, \"price\": 100.50}")
                .when()
                .put("/api/invoice-items/0")
                .then()
                .statusCode(400)
                .body(containsString("Invalid invoice item ID"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testDeleteInvoiceItem_Success() {
        when(invoiceItemService.deleteById(1L))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .delete("/api/invoice-items/1")
                .then()
                .statusCode(204);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testDeleteInvoiceItem_NotFound() {
        when(invoiceItemService.deleteById(999L))
                .thenReturn(Uni.createFrom().item(false));

        given()
                .when()
                .delete("/api/invoice-items/999")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetItemsByInvoiceId_Success() {
        List<InvoiceItem> items = Arrays.asList(
                createTestInvoiceItem(1L, 1L, 1L, BigInteger.valueOf(10), new BigDecimal("100.50")),
                createTestInvoiceItem(2L, 1L, 2L, BigInteger.valueOf(5), new BigDecimal("50.25"))
        );

        when(invoiceItemService.findByInvoiceId(1L))
                .thenReturn(Uni.createFrom().item(items));

        given()
                .when()
                .get("/api/invoice-items/invoice/1")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].id", is(1))
                .body("[1].id", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetItemsByInvoiceId_InvalidId() {
        given()
                .when()
                .get("/api/invoice-items/invoice/0")
                .then()
                .statusCode(400)
                .body(containsString("Invalid invoice ID"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetItemsByProductId_Success() {
        List<InvoiceItem> items = Arrays.asList(
                createTestInvoiceItem(1L, 1L, 1L, BigInteger.valueOf(10), new BigDecimal("100.50")),
                createTestInvoiceItem(3L, 2L, 1L, BigInteger.valueOf(3), new BigDecimal("75.00"))
        );

        when(invoiceItemService.findByProductId(1L))
                .thenReturn(Uni.createFrom().item(items));

        given()
                .when()
                .get("/api/invoice-items/product/1")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].id", is(1))
                .body("[1].id", is(3));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetItemsByQuantityGreaterThan_Success() {
        List<InvoiceItem> items = Arrays.asList(
                createTestInvoiceItem(1L, 1L, 1L, BigInteger.valueOf(15), new BigDecimal("100.50")),
                createTestInvoiceItem(2L, 1L, 2L, BigInteger.valueOf(20), new BigDecimal("50.25"))
        );

        when(invoiceItemService.findByQuantityGreaterThan(any(BigInteger.class)))
                .thenReturn(Uni.createFrom().item(items));

        given()
                .when()
                .get("/api/invoice-items/quantity/greater-than/10")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetItemsByQuantityBetween_Success() {
        List<InvoiceItem> items = Arrays.asList(
                createTestInvoiceItem(1L, 1L, 1L, BigInteger.valueOf(5), new BigDecimal("100.50")),
                createTestInvoiceItem(2L, 1L, 2L, BigInteger.valueOf(10), new BigDecimal("50.25"))
        );

        when(invoiceItemService.findByQuantityBetween(any(BigInteger.class), any(BigInteger.class)))
                .thenReturn(Uni.createFrom().item(items));

        given()
                .when()
                .get("/api/invoice-items/quantity/between?min=1&max=15")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetItemsByQuantityBetween_InvalidRange() {
        given()
                .when()
                .get("/api/invoice-items/quantity/between?min=10&max=5")
                .then()
                .statusCode(400)
                .body(containsString("Maximum quantity must be greater than minimum quantity"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetTopSellingItems_Success() {
        List<InvoiceItem> items = Arrays.asList(
                createTestInvoiceItem(1L, 1L, 1L, BigInteger.valueOf(100), new BigDecimal("100.50")),
                createTestInvoiceItem(2L, 1L, 2L, BigInteger.valueOf(75), new BigDecimal("50.25"))
        );

        when(invoiceItemService.findTopSellingItems(anyInt()))
                .thenReturn(Uni.createFrom().item(items));

        given()
                .when()
                .get("/api/invoice-items/top-selling?limit=10")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetTopSellingItems_InvalidLimit() {
        given()
                .when()
                .get("/api/invoice-items/top-selling?limit=0")
                .then()
                .statusCode(400)
                .body(containsString("Limit must be between 1 and 100"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetTotalRevenue_Success() {
        InvoiceItem item = new InvoiceItem();
        item.setPrice(new BigDecimal("1500.75"));

        when(invoiceItemService.getTotalRevenue())
                .thenReturn(Uni.createFrom().item(item));

        given()
                .when()
                .get("/api/invoice-items/total-revenue")
                .then()
                .statusCode(200)
                .body(is("1500.75"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testExistsByInvoiceAndProduct_Success() {
        when(invoiceItemService.existsByInvoiceIdAndProductId(1L, 1L))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .get("/api/invoice-items/exists?invoiceId=1&productId=1")
                .then()
                .statusCode(200)
                .body(is("true"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testExistsByInvoiceAndProduct_InvalidInvoiceId() {
        given()
                .when()
                .get("/api/invoice-items/exists?invoiceId=0&productId=1")
                .then()
                .statusCode(400)
                .body(containsString("Invalid invoice ID"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateQuantity_Success() {
        when(invoiceItemService.updateQuantity(1L, BigInteger.valueOf(20)))
                .thenReturn(Uni.createFrom().item(1));

        given()
                .when()
                .put("/api/invoice-items/1/quantity?quantity=20")
                .then()
                .statusCode(200)
                .body(containsString("Quantity updated successfully"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateQuantity_InvalidQuantity() {
        given()
                .when()
                .put("/api/invoice-items/1/quantity?quantity=0")
                .then()
                .statusCode(400)
                .body(containsString("Quantity must be greater than 0"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateQuantity_NotFound() {
        when(invoiceItemService.updateQuantity(999L, BigInteger.valueOf(20)))
                .thenReturn(Uni.createFrom().item(0));

        given()
                .when()
                .put("/api/invoice-items/999/quantity?quantity=20")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testDeleteItemsByInvoiceId_Success() {
        when(invoiceItemService.deleteByInvoiceId(1L))
                .thenReturn(Uni.createFrom().item(3L));

        given()
                .when()
                .delete("/api/invoice-items/invoice/1")
                .then()
                .statusCode(200)
                .body(containsString("Deleted 3 invoice items"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetTotalCount_Success() {
        when(invoiceItemService.countAll())
                .thenReturn(Uni.createFrom().item(50L));

        given()
                .when()
                .get("/api/invoice-items/total/count")
                .then()
                .statusCode(200)
                .body(is("50"));
    }

    @Test
    void testUnauthenticatedAccess() {
        given()
                .when()
                .get("/api/invoice-items")
                .then()
                .statusCode(401); // Unauthorized
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testServiceErrorHandling() {
        when(invoiceItemService.findById(1L))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Database error")));

        given()
                .when()
                .get("/api/invoice-items/1")
                .then()
                .statusCode(500)
                .body(containsString("Error retrieving invoice item"));
    }
}