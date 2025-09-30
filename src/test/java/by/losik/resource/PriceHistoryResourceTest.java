package by.losik.resource;

import by.losik.entity.PriceHistory;
import by.losik.entity.Product;
import by.losik.service.PriceHistoryService;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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

    @ConfigProperty(name = "app.auth.username")
    String VALID_USERNAME;

    @ConfigProperty(name = "app.auth.password")
    String VALID_PASSWORD;

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
    void testGetAllPriceHistories_Success() {
        List<PriceHistory> priceHistories = Arrays.asList(
                createTestPriceHistory(1L, 1L, new BigDecimal("100.00"), Date.valueOf("2024-01-01")),
                createTestPriceHistory(2L, 2L, new BigDecimal("200.00"), Date.valueOf("2024-01-02"))
        );

        when(priceHistoryService.findAllSorted(any(Sort.class)))
                .thenReturn(Uni.createFrom().item(priceHistories));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/price-history?sort=changeDate&direction=desc")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].id", is(1))
                .body("[1].id", is(2));
    }

    @Test
    void testGetPriceHistoryById_Success() {
        PriceHistory priceHistory = createTestPriceHistory(1L, 1L, new BigDecimal("100.00"), Date.valueOf("2024-01-01"));

        when(priceHistoryService.findById(1L))
                .thenReturn(Uni.createFrom().item(priceHistory));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/price-history/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("price", is(100.0F));
    }

    @Test
    void testGetPriceHistoryById_NotFound() {
        when(priceHistoryService.findById(999L))
                .thenReturn(Uni.createFrom().item((PriceHistory) null));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/price-history/999")
                .then()
                .statusCode(404);
    }

    @Test
    void testGetPriceHistoryById_InvalidId() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/price-history/0")
                .then()
                .statusCode(400)
                .body(containsString("Invalid price history ID"));
    }

    @Test
    void testGetPriceHistoriesByProductId_Success() {
        List<PriceHistory> priceHistories = Arrays.asList(
                createTestPriceHistory(1L, 1L, new BigDecimal("100.00"), Date.valueOf("2024-01-01")),
                createTestPriceHistory(2L, 1L, new BigDecimal("150.00"), Date.valueOf("2024-01-02"))
        );

        when(priceHistoryService.findByProductId(1L))
                .thenReturn(Uni.createFrom().item(priceHistories));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/price-history/product/1")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].product.id", is(1))
                .body("[1].product.id", is(1));
    }

    @Test
    void testGetPriceHistoriesByProductId_InvalidProductId() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/price-history/product/0")
                .then()
                .statusCode(400)
                .body(containsString("Invalid product ID"));
    }

    @Test
    void testGetLatestPriceHistoryByProductId_Success() {
        PriceHistory priceHistory = createTestPriceHistory(1L, 1L, new BigDecimal("100.00"), Date.valueOf("2024-01-01"));

        when(priceHistoryService.findLatestByProductId(1L))
                .thenReturn(Uni.createFrom().item(priceHistory));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/price-history/product/1/latest")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("product.id", is(1));
    }

    @Test
    void testGetLatestPriceHistoryByProductId_NotFound() {
        when(priceHistoryService.findLatestByProductId(999L))
                .thenReturn(Uni.createFrom().item((PriceHistory) null));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/price-history/product/999/latest")
                .then()
                .statusCode(404);
    }

    @Test
    void testGetPriceHistoriesByDate_Success() {
        List<PriceHistory> priceHistories = Arrays.asList(
                createTestPriceHistory(1L, 1L, new BigDecimal("100.00"), Date.valueOf("2024-01-01")),
                createTestPriceHistory(2L, 2L, new BigDecimal("200.00"), Date.valueOf("2024-01-01"))
        );

        when(priceHistoryService.findByChangeDate(any(Date.class)))
                .thenReturn(Uni.createFrom().item(priceHistories));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/price-history/date/2024-01-01")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    void testGetPriceHistoriesByDate_MissingDate() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/price-history/date/")
                .then()
                .statusCode(404);
    }

    @Test
    void testGetPriceHistoriesByDateRange_Success() {
        List<PriceHistory> priceHistories = Arrays.asList(
                createTestPriceHistory(1L, 1L, new BigDecimal("100.00"), Date.valueOf("2024-01-01")),
                createTestPriceHistory(2L, 2L, new BigDecimal("200.00"), Date.valueOf("2024-01-02"))
        );

        when(priceHistoryService.findByChangeDateRange(any(Date.class), any(Date.class)))
                .thenReturn(Uni.createFrom().item(priceHistories));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/price-history/date-range?startDate=2024-01-01&endDate=2024-01-31")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    void testGetPriceHistoriesByDateRange_MissingDates() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/price-history/date-range?startDate=2024-01-01")
                .then()
                .statusCode(400)
                .body(containsString("Start date and end date are required"));
    }

    @Test
    void testGetPriceHistoriesByDateRange_InvalidRange() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/price-history/date-range?startDate=2024-01-31&endDate=2024-01-01")
                .then()
                .statusCode(400)
                .body(containsString("End date must be after start date"));
    }

    @Test
    void testGetPriceHistoriesByPriceRange_Success() {
        List<PriceHistory> priceHistories = Arrays.asList(
                createTestPriceHistory(1L, 1L, new BigDecimal("150.00"), Date.valueOf("2024-01-01")),
                createTestPriceHistory(2L, 2L, new BigDecimal("250.00"), Date.valueOf("2024-01-02"))
        );

        when(priceHistoryService.findByPriceBetween(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(Uni.createFrom().item(priceHistories));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/price-history/price-range?minPrice=100&maxPrice=300")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    void testGetPriceHistoriesByPriceRange_InvalidPrices() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/price-history/price-range?minPrice=-100&maxPrice=300")
                .then()
                .statusCode(400)
                .body(containsString("Minimum price must be non-negative"));
    }

    @Test
    void testGetCurrentPriceByProductId_Success() {
        when(priceHistoryService.getCurrentPrice(any(Product.class)))
                .thenReturn(Uni.createFrom().item(new BigDecimal("150.00")));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/price-history/product/1/current-price")
                .then()
                .statusCode(200)
                .body(is("150.00"));
    }

    @Test
    void testCreatePriceHistory_Success() {
        PriceHistory savedPriceHistory = createTestPriceHistory(1L, 1L, new BigDecimal("100.00"), Date.valueOf("2024-01-01"));

        when(priceHistoryService.save(any(PriceHistory.class)))
                .thenReturn(Uni.createFrom().item(savedPriceHistory));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
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
    void testCreatePriceHistory_MissingProduct() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .contentType(ContentType.JSON)
                .body("{\"price\":100.00,\"changeDate\":\"2024-01-01\"}")
                .when()
                .post("/api/price-history")
                .then()
                .statusCode(400)
                .body(containsString("Product is required"));
    }

    @Test
    void testCreatePriceHistory_InvalidPrice() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .contentType(ContentType.JSON)
                .body("{\"product\":{\"id\":1},\"price\":0,\"changeDate\":\"2024-01-01\"}")
                .when()
                .post("/api/price-history")
                .then()
                .statusCode(400)
                .body(containsString("Price must be greater than 0"));
    }

    @Test
    void testUpdatePriceHistory_Success() {
        PriceHistory updatedPriceHistory = createTestPriceHistory(1L, 1L, new BigDecimal("150.00"), Date.valueOf("2024-01-01"));

        when(priceHistoryService.update(any(PriceHistory.class)))
                .thenReturn(Uni.createFrom().item(updatedPriceHistory));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
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
    void testUpdatePriceHistory_IdMismatch() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .contentType(ContentType.JSON)
                .body("{\"id\":2,\"product\":{\"id\":1},\"price\":150.00,\"changeDate\":\"2024-01-01\"}")
                .when()
                .put("/api/price-history/1")
                .then()
                .statusCode(400)
                .body(containsString("ID in path does not match ID in request body"));
    }

    @Test
    void testUpdatePrice_Success() {
        when(priceHistoryService.updatePrice(1L, new BigDecimal("200.00")))
                .thenReturn(Uni.createFrom().item(1));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .contentType(ContentType.JSON)
                .body("200.00")
                .when()
                .patch("/api/price-history/1/price")
                .then()
                .statusCode(200)
                .body(containsString("Price updated successfully"));
    }

    @Test
    void testUpdatePrice_NotFound() {
        when(priceHistoryService.updatePrice(999L, new BigDecimal("200.00")))
                .thenReturn(Uni.createFrom().item(0));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .contentType(ContentType.JSON)
                .body("200.00")
                .when()
                .patch("/api/price-history/999/price")
                .then()
                .statusCode(404);
    }

    @Test
    void testDeletePriceHistory_Success() {
        when(priceHistoryService.deleteById(1L))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .delete("/api/price-history/1")
                .then()
                .statusCode(204);
    }

    @Test
    void testDeletePriceHistory_NotFound() {
        when(priceHistoryService.deleteById(999L))
                .thenReturn(Uni.createFrom().item(false));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .delete("/api/price-history/999")
                .then()
                .statusCode(404);
    }

    @Test
    void testGetPriceTrend_Success() {
        List<PriceHistory> priceTrend = Arrays.asList(
                createTestPriceHistory(1L, 1L, new BigDecimal("100.00"), Date.valueOf("2024-01-01")),
                createTestPriceHistory(2L, 1L, new BigDecimal("150.00"), Date.valueOf("2024-01-02"))
        );

        when(priceHistoryService.getPriceTrend(any(Product.class), anyInt()))
                .thenReturn(Uni.createFrom().item(priceTrend));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/price-history/product/1/trend?limit=10")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    void testGetPriceTrend_InvalidLimit() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/price-history/product/1/trend?limit=0")
                .then()
                .statusCode(400)
                .body(containsString("Limit must be between 1 and 100"));
    }

    @Test
    void testGetRecentPriceChanges_Success() {
        List<PriceHistory> recentChanges = Arrays.asList(
                createTestPriceHistory(1L, 1L, new BigDecimal("100.00"), Date.valueOf("2024-01-01")),
                createTestPriceHistory(2L, 2L, new BigDecimal("200.00"), Date.valueOf("2024-01-02"))
        );

        when(priceHistoryService.findRecentPriceChanges(anyInt()))
                .thenReturn(Uni.createFrom().item(recentChanges));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/price-history/recent?days=7")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    void testGetRecentPriceChanges_InvalidDays() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/price-history/recent?days=400")
                .then()
                .statusCode(400)
                .body(containsString("Days must be between 1 and 365"));
    }

    @Test
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
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
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
    void testServiceErrorHandling() {
        when(priceHistoryService.findById(1L))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Database error")));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/price-history/1")
                .then()
                .statusCode(500)
                .body(containsString("Error retrieving price history"));
    }
}