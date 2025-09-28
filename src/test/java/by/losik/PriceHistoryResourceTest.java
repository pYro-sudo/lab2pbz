package by.losik;

import by.losik.entity.PriceHistory;
import by.losik.entity.Product;
import by.losik.service.PriceHistoryService;
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
class PriceHistoryResourceTest {

    @InjectMock
    PriceHistoryService priceHistoryService;

    private PriceHistory createTestPriceHistory(Long id, Long productId, BigDecimal price, Date changeDate) {
        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setId(id);

        Product product = new Product();
        product.setId(productId);
        priceHistory.setProduct(product);

        priceHistory.setPrice(price);
        priceHistory.setChangeDate(changeDate);
        return priceHistory;
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetAllPriceHistories_Success() {
        List<PriceHistory> priceHistories = Arrays.asList(
                createTestPriceHistory(1L, 1L, new BigDecimal("100.00"), Date.valueOf("2024-01-01")),
                createTestPriceHistory(2L, 2L, new BigDecimal("200.00"), Date.valueOf("2024-01-02"))
        );

        when(priceHistoryService.findAllSorted(any(Sort.class)))
                .thenReturn(Uni.createFrom().item(priceHistories));

        given()
                .when()
                .get("/api/price-history?sort=changeDate&direction=desc")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].id", is(1))
                .body("[1].id", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetPriceHistoryById_Success() {
        PriceHistory priceHistory = createTestPriceHistory(1L, 1L, new BigDecimal("100.00"), Date.valueOf("2024-01-01"));

        when(priceHistoryService.findById(1L))
                .thenReturn(Uni.createFrom().item(priceHistory));

        given()
                .when()
                .get("/api/price-history/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("price", is(100.0F));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetPriceHistoryById_NotFound() {
        when(priceHistoryService.findById(999L))
                .thenReturn(Uni.createFrom().item((PriceHistory) null));

        given()
                .when()
                .get("/api/price-history/999")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetPriceHistoryById_InvalidId() {
        given()
                .when()
                .get("/api/price-history/0")
                .then()
                .statusCode(400)
                .body(containsString("Invalid price history ID"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetPriceHistoriesByProductId_Success() {
        List<PriceHistory> priceHistories = Arrays.asList(
                createTestPriceHistory(1L, 1L, new BigDecimal("100.00"), Date.valueOf("2024-01-01")),
                createTestPriceHistory(2L, 1L, new BigDecimal("150.00"), Date.valueOf("2024-01-02"))
        );

        when(priceHistoryService.findByProductId(1L))
                .thenReturn(Uni.createFrom().item(priceHistories));

        given()
                .when()
                .get("/api/price-history/product/1")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].product.id", is(1))
                .body("[1].product.id", is(1));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetPriceHistoriesByProductId_InvalidProductId() {
        given()
                .when()
                .get("/api/price-history/product/0")
                .then()
                .statusCode(400)
                .body(containsString("Invalid product ID"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetLatestPriceHistoryByProductId_Success() {
        PriceHistory priceHistory = createTestPriceHistory(1L, 1L, new BigDecimal("100.00"), Date.valueOf("2024-01-01"));

        when(priceHistoryService.findLatestByProductId(1L))
                .thenReturn(Uni.createFrom().item(priceHistory));

        given()
                .when()
                .get("/api/price-history/product/1/latest")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("product.id", is(1));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetLatestPriceHistoryByProductId_NotFound() {
        when(priceHistoryService.findLatestByProductId(999L))
                .thenReturn(Uni.createFrom().item((PriceHistory) null));

        given()
                .when()
                .get("/api/price-history/product/999/latest")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetPriceHistoriesByDate_Success() {
        List<PriceHistory> priceHistories = Arrays.asList(
                createTestPriceHistory(1L, 1L, new BigDecimal("100.00"), Date.valueOf("2024-01-01")),
                createTestPriceHistory(2L, 2L, new BigDecimal("200.00"), Date.valueOf("2024-01-01"))
        );

        when(priceHistoryService.findByChangeDate(any(Date.class)))
                .thenReturn(Uni.createFrom().item(priceHistories));

        given()
                .when()
                .get("/api/price-history/date/2024-01-01")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetPriceHistoriesByDate_MissingDate() {
        given()
                .when()
                .get("/api/price-history/date/")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetPriceHistoriesByDateRange_Success() {
        List<PriceHistory> priceHistories = Arrays.asList(
                createTestPriceHistory(1L, 1L, new BigDecimal("100.00"), Date.valueOf("2024-01-01")),
                createTestPriceHistory(2L, 2L, new BigDecimal("200.00"), Date.valueOf("2024-01-02"))
        );

        when(priceHistoryService.findByChangeDateRange(any(Date.class), any(Date.class)))
                .thenReturn(Uni.createFrom().item(priceHistories));

        given()
                .when()
                .get("/api/price-history/date-range?startDate=2024-01-01&endDate=2024-01-31")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetPriceHistoriesByDateRange_MissingDates() {
        given()
                .when()
                .get("/api/price-history/date-range?startDate=2024-01-01")
                .then()
                .statusCode(400)
                .body(containsString("Start date and end date are required"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetPriceHistoriesByDateRange_InvalidRange() {
        given()
                .when()
                .get("/api/price-history/date-range?startDate=2024-01-31&endDate=2024-01-01")
                .then()
                .statusCode(400)
                .body(containsString("End date must be after start date"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetPriceHistoriesByPriceRange_Success() {
        List<PriceHistory> priceHistories = Arrays.asList(
                createTestPriceHistory(1L, 1L, new BigDecimal("150.00"), Date.valueOf("2024-01-01")),
                createTestPriceHistory(2L, 2L, new BigDecimal("250.00"), Date.valueOf("2024-01-02"))
        );

        when(priceHistoryService.findByPriceBetween(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(Uni.createFrom().item(priceHistories));

        given()
                .when()
                .get("/api/price-history/price-range?minPrice=100&maxPrice=300")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetPriceHistoriesByPriceRange_InvalidPrices() {
        given()
                .when()
                .get("/api/price-history/price-range?minPrice=-100&maxPrice=300")
                .then()
                .statusCode(400)
                .body(containsString("Minimum price must be non-negative"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetCurrentPriceByProductId_Success() {
        when(priceHistoryService.getCurrentPrice(any(Product.class)))
                .thenReturn(Uni.createFrom().item(new BigDecimal("150.00")));

        given()
                .when()
                .get("/api/price-history/product/1/current-price")
                .then()
                .statusCode(200)
                .body(is("150.0"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreatePriceHistory_Success() {
        PriceHistory savedPriceHistory = createTestPriceHistory(1L, 1L, new BigDecimal("100.00"), Date.valueOf("2024-01-01"));

        when(priceHistoryService.save(any(PriceHistory.class)))
                .thenReturn(Uni.createFrom().item(savedPriceHistory));

        given()
                .contentType(ContentType.JSON)
                .body("{\"product\":{\"id\":1},\"price\":100.00,\"changeDate\":\"2024-01-01\"}")
                .when()
                .post("/api/price-history")
                .then()
                .statusCode(201)
                .body("id", is(1))
                .body("price", is(100.0F));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreatePriceHistory_MissingProduct() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"price\":100.00,\"changeDate\":\"2024-01-01\"}")
                .when()
                .post("/api/price-history")
                .then()
                .statusCode(400)
                .body(containsString("Product is required"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreatePriceHistory_InvalidPrice() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"product\":{\"id\":1},\"price\":0,\"changeDate\":\"2024-01-01\"}")
                .when()
                .post("/api/price-history")
                .then()
                .statusCode(400)
                .body(containsString("Price must be greater than 0"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdatePriceHistory_Success() {
        PriceHistory updatedPriceHistory = createTestPriceHistory(1L, 1L, new BigDecimal("150.00"), Date.valueOf("2024-01-01"));

        when(priceHistoryService.update(any(PriceHistory.class)))
                .thenReturn(Uni.createFrom().item(updatedPriceHistory));

        given()
                .contentType(ContentType.JSON)
                .body("{\"id\":1,\"product\":{\"id\":1},\"price\":150.00,\"changeDate\":\"2024-01-01\"}")
                .when()
                .put("/api/price-history/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("price", is(150.0F));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdatePriceHistory_IdMismatch() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"id\":2,\"product\":{\"id\":1},\"price\":150.00,\"changeDate\":\"2024-01-01\"}")
                .when()
                .put("/api/price-history/1")
                .then()
                .statusCode(400)
                .body(containsString("ID in path does not match ID in request body"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdatePrice_Success() {
        when(priceHistoryService.updatePrice(1L, new BigDecimal("200.00")))
                .thenReturn(Uni.createFrom().item(1));

        given()
                .contentType(ContentType.JSON)
                .body("200.00")
                .when()
                .patch("/api/price-history/1/price")
                .then()
                .statusCode(200)
                .body(containsString("Price updated successfully"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdatePrice_NotFound() {
        when(priceHistoryService.updatePrice(999L, new BigDecimal("200.00")))
                .thenReturn(Uni.createFrom().item(0));

        given()
                .contentType(ContentType.JSON)
                .body("200.00")
                .when()
                .patch("/api/price-history/999/price")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testDeletePriceHistory_Success() {
        when(priceHistoryService.deleteById(1L))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .delete("/api/price-history/1")
                .then()
                .statusCode(204);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testDeletePriceHistory_NotFound() {
        when(priceHistoryService.deleteById(999L))
                .thenReturn(Uni.createFrom().item(false));

        given()
                .when()
                .delete("/api/price-history/999")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetPriceTrend_Success() {
        List<PriceHistory> priceTrend = Arrays.asList(
                createTestPriceHistory(1L, 1L, new BigDecimal("100.00"), Date.valueOf("2024-01-01")),
                createTestPriceHistory(2L, 1L, new BigDecimal("150.00"), Date.valueOf("2024-01-02"))
        );

        when(priceHistoryService.getPriceTrend(any(Product.class), anyInt()))
                .thenReturn(Uni.createFrom().item(priceTrend));

        given()
                .when()
                .get("/api/price-history/product/1/trend?limit=10")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetPriceTrend_InvalidLimit() {
        given()
                .when()
                .get("/api/price-history/product/1/trend?limit=0")
                .then()
                .statusCode(400)
                .body(containsString("Limit must be between 1 and 100"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetRecentPriceChanges_Success() {
        List<PriceHistory> recentChanges = Arrays.asList(
                createTestPriceHistory(1L, 1L, new BigDecimal("100.00"), Date.valueOf("2024-01-01")),
                createTestPriceHistory(2L, 2L, new BigDecimal("200.00"), Date.valueOf("2024-01-02"))
        );

        when(priceHistoryService.findRecentPriceChanges(anyInt()))
                .thenReturn(Uni.createFrom().item(recentChanges));

        given()
                .when()
                .get("/api/price-history/recent?days=7")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetRecentPriceChanges_InvalidDays() {
        given()
                .when()
                .get("/api/price-history/recent?days=400")
                .then()
                .statusCode(400)
                .body(containsString("Days must be between 1 and 365"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetPriceStatsByProductId_Success() {
        PriceHistory maxPrice = createTestPriceHistory(1L, 1L, new BigDecimal("300.00"), Date.valueOf("2024-01-01"));
        PriceHistory minPrice = createTestPriceHistory(2L, 1L, new BigDecimal("100.00"), Date.valueOf("2024-01-02"));

        when(priceHistoryService.getMaxPriceByProduct(any(Product.class)))
                .thenReturn(Uni.createFrom().item(maxPrice));
        when(priceHistoryService.getMinPriceByProduct(any(Product.class)))
                .thenReturn(Uni.createFrom().item(minPrice));
        when(priceHistoryService.countPriceChangesByProduct(any(Product.class)))
                .thenReturn(Uni.createFrom().item(5L));

        given()
                .when()
                .get("/api/price-history/stats/product/1")
                .then()
                .statusCode(200)
                .body("maxPrice", is(300.0F))
                .body("minPrice", is(100.0F))
                .body("changeCount", is(5));
    }

    @Test
    void testUnauthenticatedAccess() {
        given()
                .when()
                .get("/api/price-history")
                .then()
                .statusCode(401); // Unauthorized
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testServiceErrorHandling() {
        when(priceHistoryService.findById(1L))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Database error")));

        given()
                .when()
                .get("/api/price-history/1")
                .then()
                .statusCode(500)
                .body(containsString("Error retrieving price history"));
    }
}