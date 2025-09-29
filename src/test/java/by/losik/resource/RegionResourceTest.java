package by.losik.resource;

import by.losik.entity.Region;
import by.losik.service.RegionService;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@QuarkusTest
class RegionResourceTest {

    @ConfigProperty(name = "app.auth.username")
    String VALID_USERNAME;
    @ConfigProperty(name = "app.auth.password")
    String VALID_PASSWORD;

    @InjectMock
    RegionService regionService;

    private Region createTestRegion(Long id, String name, String country) {
        Region region = new Region();
        region.setId(id);
        region.setName(name);
        region.setCountry(country);
        return region;
    }

    @Test
    void testGetAllRegions_Success() {
        List<Region> regions = Arrays.asList(
                createTestRegion(1L, "California", "USA"),
                createTestRegion(2L, "Texas", "USA")
        );

        when(regionService.findPaginatedSorted(anyInt(), anyInt(), any(Sort.class)))
                .thenReturn(Uni.createFrom().item(regions));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/regions?page=0&size=20&sort=name&direction=asc")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("California"))
                .body("[1].name", is("Texas"));
    }

    @Test
    void testGetAllRegions_InvalidPage() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/regions?page=-1&size=20")
                .then()
                .statusCode(200)
                .body(containsString("[]"));
    }

    @Test
    void testGetAllRegions_InvalidSize() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/regions?page=0&size=0")
                .then()
                .statusCode(200)
                .body(containsString("[]"));
    }

    @Test
    void testGetRegionById_Success() {
        Region region = createTestRegion(1L, "California", "USA");

        when(regionService.findById(1L))
                .thenReturn(Uni.createFrom().item(region));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/regions/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("name", is("California"))
                .body("country", is("USA"));
    }

    @Test
    void testGetRegionById_NotFound() {
        when(regionService.findById(999L))
                .thenReturn(Uni.createFrom().item((Region) null));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/regions/999")
                .then()
                .statusCode(404);
    }

    @Test
    void testGetRegionsByCountry_Success() {
        List<Region> regions = Arrays.asList(
                createTestRegion(1L, "California", "USA"),
                createTestRegion(2L, "Texas", "USA")
        );

        when(regionService.findByCountry("USA"))
                .thenReturn(Uni.createFrom().item(regions));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/regions/country/USA")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("California"))
                .body("[1].name", is("Texas"));
    }

    @Test
    void testSearchRegions_Success() {
        List<Region> regions = Arrays.asList(
                createTestRegion(1L, "Northern California", "USA"),
                createTestRegion(2L, "Southern California", "USA")
        );

        when(regionService.searchRegionsPaginated(anyString(), anyInt(), anyInt()))
                .thenReturn(Uni.createFrom().item(regions));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/regions/search?q=california&page=0&size=20")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("Northern California"))
                .body("[1].name", is("Southern California"));
    }

    @Test
    void testSearchRegions_MissingTerm() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/regions/search?q=")
                .then()
                .statusCode(400)
                .body(containsString("Search term cannot be empty"));
    }

    @Test
    void testGetDistinctCountries_Success() {
        List<String> countries = Arrays.asList("USA", "Canada", "Mexico");

        when(regionService.findDistinctCountries())
                .thenReturn(Uni.createFrom().item(countries));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/regions/countries")
                .then()
                .statusCode(200)
                .body("size()", is(3))
                .body("[0]", is("USA"))
                .body("[1]", is("Canada"))
                .body("[2]", is("Mexico"));
    }

    @Test
    void testGetRegionsByCountryContaining_Success() {
        List<Region> regions = Arrays.asList(
                createTestRegion(1L, "California", "United States"),
                createTestRegion(2L, "Texas", "United States")
        );

        when(regionService.findByCountryContaining("United"))
                .thenReturn(Uni.createFrom().item(regions));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/regions/country-contains/United")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    void testGetRegionsByNameContaining_Success() {
        List<Region> regions = Arrays.asList(
                createTestRegion(1L, "Northern California", "USA"),
                createTestRegion(2L, "Southern California", "USA")
        );

        when(regionService.findByNameContaining("California"))
                .thenReturn(Uni.createFrom().item(regions));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/regions/name-contains/California")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    void testGetRegionsByCountryAndName_Success() {
        List<Region> regions = List.of(
                createTestRegion(1L, "Northern California", "USA")
        );

        when(regionService.findByCountryAndName("USA", "California"))
                .thenReturn(Uni.createFrom().item(regions));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/regions/country/USA/name-contains/California")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].name", is("Northern California"));
    }

    @Test
    void testCreateRegion_Success() {
        Region newRegion = createTestRegion(1L, "New Region", "USA");

        when(regionService.existsByNameAndCountry("New Region", "USA"))
                .thenReturn(Uni.createFrom().item(false));

        when(regionService.save(any(Region.class)))
                .thenReturn(Uni.createFrom().item(newRegion));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .contentType(ContentType.JSON)
                .body("{\"name\": \"New Region\", \"country\": \"USA\"}")
                .when()
                .post("/api/regions")
                .then()
                .statusCode(201)
                .body("id", is(1))
                .body("name", is("New Region"))
                .body("country", is("USA"));
    }

    @Test
    void testCreateRegion_MissingName() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .contentType(ContentType.JSON)
                .body("{\"country\": \"USA\"}")
                .when()
                .post("/api/regions")
                .then()
                .statusCode(400)
                .body(containsString("Region name is required"));
    }

    @Test
    void testCreateRegion_MissingCountry() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .contentType(ContentType.JSON)
                .body("{\"name\": \"New Region\"}")
                .when()
                .post("/api/regions")
                .then()
                .statusCode(400)
                .body(containsString("Country is required"));
    }

    @Test
    void testCreateRegion_AlreadyExists() {
        when(regionService.existsByNameAndCountry("Existing Region", "USA"))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .contentType(ContentType.JSON)
                .body("{\"name\": \"Existing Region\", \"country\": \"USA\"}")
                .when()
                .post("/api/regions")
                .then()
                .statusCode(409)
                .body(containsString("already exists"));
    }

    @Test
    void testUpdateRegion_Success() {
        Region updatedRegion = createTestRegion(1L, "Updated Region", "USA");

        when(regionService.update(any(Region.class)))
                .thenReturn(Uni.createFrom().item(updatedRegion));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .contentType(ContentType.JSON)
                .body("{\"id\": 1, \"name\": \"Updated Region\", \"country\": \"USA\"}")
                .when()
                .put("/api/regions/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("name", is("Updated Region"));
    }

    @Test
    void testUpdateRegion_IdMismatch() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .contentType(ContentType.JSON)
                .body("{\"id\": 2, \"name\": \"Updated Region\", \"country\": \"USA\"}")
                .when()
                .put("/api/regions/1")
                .then()
                .statusCode(400)
                .body(containsString("ID in path does not match ID in request body"));
    }

    @Test
    void testUpdateCountryForRegions_Success() {
        when(regionService.updateCountryForRegions("USA", "United States"))
                .thenReturn(Uni.createFrom().item(5));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .put("/api/regions/country/update?oldCountry=USA&newCountry=United States")
                .then()
                .statusCode(200)
                .body(containsString("Updated country for 5 regions"));
    }

    @Test
    void testUpdateCountryForRegions_MissingParameters() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .put("/api/regions/country/update?oldCountry=USA")
                .then()
                .statusCode(400)
                .body(containsString("Both oldCountry and newCountry parameters are required"));
    }

    @Test
    void testDeleteRegion_Success() {
        when(regionService.deleteById(1L))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .delete("/api/regions/1")
                .then()
                .statusCode(204);
    }

    @Test
    void testDeleteRegion_NotFound() {
        when(regionService.deleteById(999L))
                .thenReturn(Uni.createFrom().item(false));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .delete("/api/regions/999")
                .then()
                .statusCode(404);
    }

    @Test
    void testDeleteRegionsByCountry_Success() {
        when(regionService.deleteByCountry("USA"))
                .thenReturn(Uni.createFrom().item(3L));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .delete("/api/regions/country/USA")
                .then()
                .statusCode(200)
                .body(containsString("Deleted 3 regions"));
    }

    @Test
    void testGetRegionCountByCountry_Success() {
        when(regionService.countByCountry("USA"))
                .thenReturn(Uni.createFrom().item(10L));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/regions/stats/country/USA")
                .then()
                .statusCode(200)
                .body(is("10"));
    }

    @Test
    void testGetRegionsWithoutSettlements_Success() {
        List<Region> regions = Arrays.asList(
                createTestRegion(1L, "Empty Region 1", "USA"),
                createTestRegion(2L, "Empty Region 2", "Canada")
        );

        when(regionService.findRegionsWithoutSettlements())
                .thenReturn(Uni.createFrom().item(regions));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/regions/stats/no-settlements")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    void testGetSettlementCountByRegion_Success() {
        when(regionService.getSettlementCountByRegion())
                .thenReturn(Uni.createFrom().item(List.of()));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/regions/stats/settlement-count")
                .then()
                .statusCode(200);
    }

    @Test
    void testCheckRegionExistsByNameAndCountry_Success() {
        when(regionService.existsByNameAndCountry("California", "USA"))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/regions/exists/name-country?name=California&country=USA")
                .then()
                .statusCode(200)
                .body(is("true"));
    }

    @Test
    void testCheckRegionExistsByNameAndCountry_MissingParameters() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/regions/exists/name-country?name=California")
                .then()
                .statusCode(400)
                .body(containsString("Both name and country parameters are required"));
    }

    @Test
    void testUnauthenticatedAccess() {
        given()
                .when()
                .get("/api/regions")
                .then()
                .statusCode(401);
    }

    @Test
    void testServiceErrorHandling() {
        when(regionService.findById(1L))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Database error")));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/api/regions/1")
                .then()
                .statusCode(500)
                .body(containsString("Error retrieving region"));
    }
}