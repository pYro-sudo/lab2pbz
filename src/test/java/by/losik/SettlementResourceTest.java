package by.losik;

import by.losik.entity.Region;
import by.losik.entity.Settlement;
import by.losik.service.SettlementService;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@QuarkusTest
class SettlementResourceTest {

    @InjectMock
    SettlementService settlementService;

    private Settlement createTestSettlement(Long id, String name, Long regionId) {
        Settlement settlement = new Settlement();
        settlement.setId(id);
        settlement.setName(name);

        Region region = new Region();
        region.setId(regionId);
        settlement.setRegion(region);

        return settlement;
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetAllSettlements_Success() {
        List<Settlement> settlements = Arrays.asList(
                createTestSettlement(1L, "Minsk", 1L),
                createTestSettlement(2L, "Gomel", 2L)
        );

        when(settlementService.findPaginatedSorted(anyInt(), anyInt(), any(Sort.class)))
                .thenReturn(Uni.createFrom().item(settlements));

        given()
                .when()
                .get("/api/settlements?page=0&size=20&sort=name&direction=asc")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("Minsk"))
                .body("[1].name", is("Gomel"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetSettlementById_Success() {
        Settlement settlement = createTestSettlement(1L, "Minsk", 1L);

        when(settlementService.findById(1L))
                .thenReturn(Uni.createFrom().item(settlement));

        given()
                .when()
                .get("/api/settlements/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("name", is("Minsk"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetSettlementById_NotFound() {
        when(settlementService.findById(999L))
                .thenReturn(Uni.createFrom().item((Settlement) null));

        given()
                .when()
                .get("/api/settlements/999")
                .then()
                .statusCode(404)
                .body(containsString("Settlement not found"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetSettlementsByRegion_Success() {
        List<Settlement> settlements = Arrays.asList(
                createTestSettlement(1L, "Minsk", 1L),
                createTestSettlement(2L, "Borisov", 1L)
        );

        when(settlementService.findByRegionId(1L))
                .thenReturn(Uni.createFrom().item(settlements));

        given()
                .when()
                .get("/api/settlements/region/1")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("Minsk"))
                .body("[1].name", is("Borisov"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testSearchSettlements_Success() {
        List<Settlement> settlements = Arrays.asList(
                createTestSettlement(1L, "Minsk", 1L),
                createTestSettlement(2L, "Minsk Region", 1L)
        );

        when(settlementService.searchSettlementsPaginated(anyString(), anyInt(), anyInt()))
                .thenReturn(Uni.createFrom().item(settlements));

        given()
                .when()
                .get("/api/settlements/search?q=Minsk&page=0&size=20")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testSearchSettlements_EmptyTerm() {
        given()
                .when()
                .get("/api/settlements/search?q=")
                .then()
                .statusCode(400)
                .body(containsString("Search term cannot be empty"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetSettlementsByNameContaining_Success() {
        List<Settlement> settlements = Arrays.asList(
                createTestSettlement(1L, "Gomel", 2L),
                createTestSettlement(2L, "Gomel Region", 2L)
        );

        when(settlementService.findByNameContaining("Gomel"))
                .thenReturn(Uni.createFrom().item(settlements));

        given()
                .when()
                .get("/api/settlements/name-contains/Gomel")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetSettlementsByRegionAndName_Success() {
        List<Settlement> settlements = Collections.singletonList(
                createTestSettlement(1L, "Minsk", 1L)
        );

        when(settlementService.findByRegionIdAndName(1L, "Minsk"))
                .thenReturn(Uni.createFrom().item(settlements));

        given()
                .when()
                .get("/api/settlements/region/1/name-contains/Minsk")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].name", is("Minsk"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetSettlementsWithoutInvoices_Success() {
        List<Settlement> settlements = Arrays.asList(
                createTestSettlement(1L, "Village1", 3L),
                createTestSettlement(2L, "Village2", 3L)
        );

        when(settlementService.findSettlementsWithoutInvoices())
                .thenReturn(Uni.createFrom().item(settlements));

        given()
                .when()
                .get("/api/settlements/without-invoices")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetSettlementsWithInvoices_Success() {
        List<Settlement> settlements = Arrays.asList(
                createTestSettlement(1L, "Minsk", 1L),
                createTestSettlement(2L, "Gomel", 2L)
        );

        when(settlementService.findSettlementsWithInvoices())
                .thenReturn(Uni.createFrom().item(settlements));

        given()
                .when()
                .get("/api/settlements/with-invoices")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetMostActiveSettlements_Success() {
        List<Settlement> settlements = Arrays.asList(
                createTestSettlement(1L, "Minsk", 1L),
                createTestSettlement(2L, "Gomel", 2L)
        );

        when(settlementService.findMostActiveSettlements(5))
                .thenReturn(Uni.createFrom().item(settlements));

        given()
                .when()
                .get("/api/settlements/most-active?limit=5")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateSettlement_Success() {
        Settlement savedSettlement = createTestSettlement(1L, "New Settlement", 1L);

        when(settlementService.existsByNameAndRegionId("New Settlement", 1L))
                .thenReturn(Uni.createFrom().item(false));
        when(settlementService.save(any(Settlement.class)))
                .thenReturn(Uni.createFrom().item(savedSettlement));

        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"New Settlement\", \"region\": {\"id\": 1}}")
                .when()
                .post("/api/settlements")
                .then()
                .statusCode(201)
                .body("id", is(1))
                .body("name", is("New Settlement"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateSettlement_MissingName() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"region\": {\"id\": 1}}")
                .when()
                .post("/api/settlements")
                .then()
                .statusCode(400)
                .body(containsString("Settlement name is required"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateSettlement_MissingRegion() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"New Settlement\"}")
                .when()
                .post("/api/settlements")
                .then()
                .statusCode(400)
                .body(containsString("Region ID is required"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateSettlement_AlreadyExists() {
        when(settlementService.existsByNameAndRegionId("Existing Settlement", 1L))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"Existing Settlement\", \"region\": {\"id\": 1}}")
                .when()
                .post("/api/settlements")
                .then()
                .statusCode(409)
                .body(containsString("already exists"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateSettlement_Success() {
        Settlement updatedSettlement = createTestSettlement(1L, "Updated Settlement", 1L);

        when(settlementService.update(any(Settlement.class)))
                .thenReturn(Uni.createFrom().item(updatedSettlement));

        given()
                .contentType(ContentType.JSON)
                .body("{\"id\": 1, \"name\": \"Updated Settlement\", \"region\": {\"id\": 1}}")
                .when()
                .put("/api/settlements/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("name", is("Updated Settlement"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateSettlement_IdMismatch() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"id\": 2, \"name\": \"Updated Settlement\", \"region\": {\"id\": 1}}")
                .when()
                .put("/api/settlements/1")
                .then()
                .statusCode(400)
                .body(containsString("ID in path does not match ID in request body"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateSettlementName_Success() {
        when(settlementService.updateSettlementName(1L, "New Name"))
                .thenReturn(Uni.createFrom().item(1));

        given()
                .contentType(ContentType.TEXT)
                .body("New Name")
                .when()
                .patch("/api/settlements/1/name")
                .then()
                .statusCode(200)
                .body(containsString("Settlement name updated successfully"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateSettlementName_EmptyName() {
        given()
                .contentType(ContentType.TEXT)
                .body("")
                .when()
                .patch("/api/settlements/1/name")
                .then()
                .statusCode(400)
                .body(containsString("Settlement name cannot be empty"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateSettlementName_NotFound() {
        when(settlementService.updateSettlementName(999L, "New Name"))
                .thenReturn(Uni.createFrom().item(0));

        given()
                .contentType(ContentType.TEXT)
                .body("New Name")
                .when()
                .patch("/api/settlements/999/name")
                .then()
                .statusCode(404)
                .body(containsString("Settlement not found"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateSettlementRegion_Success() {
        when(settlementService.updateSettlementRegion(1L, 2L))
                .thenReturn(Uni.createFrom().item(1));

        given()
                .contentType(ContentType.TEXT)
                .body("2")
                .when()
                .patch("/api/settlements/1/region")
                .then()
                .statusCode(200)
                .body(containsString("Settlement region updated successfully"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateSettlementRegion_NullRegionId() {
        given()
                .contentType(ContentType.TEXT)
                .body("")
                .when()
                .patch("/api/settlements/1/region")
                .then()
                .statusCode(400)
                .body(containsString("Region ID cannot be null"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testTransferSettlementsToRegion_Success() {
        when(settlementService.transferSettlementsToRegion(1L, 2L))
                .thenReturn(Uni.createFrom().item(5));

        given()
                .when()
                .put("/api/settlements/transfer-region?fromRegionId=1&toRegionId=2")
                .then()
                .statusCode(200)
                .body(containsString("Transferred 5 settlements"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testTransferSettlementsToRegion_MissingParameters() {
        given()
                .when()
                .put("/api/settlements/transfer-region?fromRegionId=1")
                .then()
                .statusCode(400)
                .body(containsString("Both fromRegionId and toRegionId parameters are required"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testTransferSettlementsToRegion_SameRegion() {
        given()
                .when()
                .put("/api/settlements/transfer-region?fromRegionId=1&toRegionId=1")
                .then()
                .statusCode(400)
                .body(containsString("Source and target region IDs cannot be the same"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testDeleteSettlement_Success() {
        when(settlementService.deleteById(1L))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .delete("/api/settlements/1")
                .then()
                .statusCode(204);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testDeleteSettlement_NotFound() {
        when(settlementService.deleteById(999L))
                .thenReturn(Uni.createFrom().item(false));

        given()
                .when()
                .delete("/api/settlements/999")
                .then()
                .statusCode(404)
                .body(containsString("Settlement not found"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testDeleteSettlementsByRegion_Success() {
        when(settlementService.deleteByRegionId(1L))
                .thenReturn(Uni.createFrom().item(3L));

        given()
                .when()
                .delete("/api/settlements/region/1")
                .then()
                .statusCode(200)
                .body(containsString("Deleted 3 settlements"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetSettlementCountByRegion_Success() {
        when(settlementService.countByRegionId(1L))
                .thenReturn(Uni.createFrom().item(10L));

        given()
                .when()
                .get("/api/settlements/stats/region/1")
                .then()
                .statusCode(200)
                .body(is("10"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetSettlementInvoiceStats_Success() {
        List<Object[]> stats = Arrays.asList(
                new Object[]{1L, "Minsk", 5L},
                new Object[]{2L, "Gomel", 3L}
        );

        when(settlementService.getSettlementInvoiceStats())
                .thenReturn(Uni.createFrom().item(stats));

        given()
                .when()
                .get("/api/settlements/stats/invoice-stats")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCheckSettlementExistsByNameAndRegion_Success() {
        when(settlementService.existsByNameAndRegionId("Minsk", 1L))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .get("/api/settlements/exists/name-region?name=Minsk&regionId=1")
                .then()
                .statusCode(200)
                .body(is("true"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCheckSettlementExistsByNameAndRegion_MissingParameters() {
        given()
                .when()
                .get("/api/settlements/exists/name-region?name=Minsk")
                .then()
                .statusCode(400)
                .body(containsString("Both name and regionId parameters are required"));
    }

    @Test
    void testUnauthenticatedAccess() {
        given()
                .when()
                .get("/api/settlements")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testServiceErrorHandling() {
        when(settlementService.findById(1L))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Database error")));

        given()
                .when()
                .get("/api/settlements/1")
                .then()
                .statusCode(500)
                .body(containsString("Error retrieving settlement"));
    }
}
