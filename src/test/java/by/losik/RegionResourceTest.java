package by.losik;

import by.losik.entity.Region;
import by.losik.service.RegionService;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
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
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetAllRegions_Success() {
        List<Region> regions = Arrays.asList(
                createTestRegion(1L, "California", "USA"),
                createTestRegion(2L, "Texas", "USA")
        );

        when(regionService.findPaginatedSorted(anyInt(), anyInt(), any(Sort.class)))
                .thenReturn(Uni.createFrom().item(regions));

        given()
                .when()
                .get("/api/regions?page=0&size=20&sort=name&direction=asc")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("California"))
                .body("[1].name", is("Texas"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetRegionById_Success() {
        Region region = createTestRegion(1L, "California", "USA");

        when(regionService.findById(1L))
                .thenReturn(Uni.createFrom().item(region));

        given()
                .when()
                .get("/api/regions/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("name", is("California"))
                .body("country", is("USA"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetRegionById_NotFound() {
        when(regionService.findById(999L))
                .thenReturn(Uni.createFrom().item((Region) null));

        given()
                .when()
                .get("/api/regions/999")
                .then()
                .statusCode(404)
                .body(containsString("Region not found with id: 999"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetRegionsByCountry_Success() {
        List<Region> regions = Arrays.asList(
                createTestRegion(1L, "California", "USA"),
                createTestRegion(2L, "Texas", "USA")
        );

        when(regionService.findByCountry("USA"))
                .thenReturn(Uni.createFrom().item(regions));

        given()
                .when()
                .get("/api/regions/country/USA")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("California"))
                .body("[1].name", is("Texas"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testSearchRegions_Success() {
        List<Region> regions = Arrays.asList(
                createTestRegion(1L, "Northern California", "USA"),
                createTestRegion(2L, "Southern California", "USA")
        );

        when(regionService.searchRegionsPaginated(anyString(), anyInt(), anyInt()))
                .thenReturn(Uni.createFrom().item(regions));

        given()
                .when()
                .get("/api/regions/search?q=california&page=0&size=20")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("Northern California"))
                .body("[1].name", is("Southern California"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testSearchRegions_EmptyTerm() {
        given()
                .when()
                .get("/api/regions/search?q=")
                .then()
                .statusCode(400)
                .body(containsString("Search term cannot be empty"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetDistinctCountries_Success() {
        List<String> countries = Arrays.asList("USA", "Canada", "Mexico");

        when(regionService.findDistinctCountries())
                .thenReturn(Uni.createFrom().item(countries));

        given()
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
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetRegionsByCountryContaining_Success() {
        List<Region> regions = Arrays.asList(
                createTestRegion(1L, "California", "United States"),
                createTestRegion(2L, "Texas", "United States")
        );

        when(regionService.findByCountryContaining("United"))
                .thenReturn(Uni.createFrom().item(regions));

        given()
                .when()
                .get("/api/regions/country-contains/United")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetRegionsByNameContaining_Success() {
        List<Region> regions = Arrays.asList(
                createTestRegion(1L, "Northern California", "USA"),
                createTestRegion(2L, "Southern California", "USA")
        );

        when(regionService.findByNameContaining("California"))
                .thenReturn(Uni.createFrom().item(regions));

        given()
                .when()
                .get("/api/regions/name-contains/California")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetRegionsByCountryAndName_Success() {
        List<Region> regions = List.of(
                createTestRegion(1L, "Northern California", "USA")
        );

        when(regionService.findByCountryAndName("USA", "California"))
                .thenReturn(Uni.createFrom().item(regions));

        given()
                .when()
                .get("/api/regions/country/USA/name-contains/California")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].name", is("Northern California"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateRegion_Success() {
        Region newRegion = createTestRegion(1L, "New Region", "USA");

        when(regionService.existsByNameAndCountry("New Region", "USA"))
                .thenReturn(Uni.createFrom().item(false));

        when(regionService.save(any(Region.class)))
                .thenReturn(Uni.createFrom().item(newRegion));

        given()
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
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateRegion_MissingName() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"country\": \"USA\"}")
                .when()
                .post("/api/regions")
                .then()
                .statusCode(400)
                .body(containsString("Region name is required"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateRegion_MissingCountry() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"New Region\"}")
                .when()
                .post("/api/regions")
                .then()
                .statusCode(400)
                .body(containsString("Country is required"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateRegion_AlreadyExists() {
        when(regionService.existsByNameAndCountry("Existing Region", "USA"))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"Existing Region\", \"country\": \"USA\"}")
                .when()
                .post("/api/regions")
                .then()
                .statusCode(409)
                .body(containsString("already exists"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateRegion_Success() {
        Region updatedRegion = createTestRegion(1L, "Updated Region", "USA");

        when(regionService.update(any(Region.class)))
                .thenReturn(Uni.createFrom().item(updatedRegion));

        given()
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
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateRegion_IdMismatch() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"id\": 2, \"name\": \"Updated Region\", \"country\": \"USA\"}")
                .when()
                .put("/api/regions/1")
                .then()
                .statusCode(400)
                .body(containsString("ID in path does not match ID in request body"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateRegionName_Success() {
        when(regionService.updateRegionName(1L, "New Name"))
                .thenReturn(Uni.createFrom().item(1));

        given()
                .contentType(ContentType.TEXT)
                .body("New Name")
                .when()
                .patch("/api/regions/1/name")
                .then()
                .statusCode(200)
                .body(containsString("Region name updated successfully"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateRegionName_EmptyName() {
        given()
                .contentType(ContentType.TEXT)
                .body("")
                .when()
                .patch("/api/regions/1/name")
                .then()
                .statusCode(400)
                .body(containsString("Region name cannot be empty"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateRegionName_NotFound() {
        when(regionService.updateRegionName(999L, "New Name"))
                .thenReturn(Uni.createFrom().item(0));

        given()
                .contentType(ContentType.TEXT)
                .body("New Name")
                .when()
                .patch("/api/regions/999/name")
                .then()
                .statusCode(404)
                .body(containsString("Region not found with id: 999"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateRegionCountry_Success() {
        when(regionService.updateRegionCountry(1L, "Canada"))
                .thenReturn(Uni.createFrom().item(1));

        given()
                .contentType(ContentType.TEXT)
                .body("Canada")
                .when()
                .patch("/api/regions/1/country")
                .then()
                .statusCode(200)
                .body(containsString("Region country updated successfully"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateCountryForRegions_Success() {
        when(regionService.updateCountryForRegions("USA", "United States"))
                .thenReturn(Uni.createFrom().item(5));

        given()
                .when()
                .put("/api/regions/country/update?oldCountry=USA&newCountry=United States")
                .then()
                .statusCode(200)
                .body(containsString("Updated country for 5 regions"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateCountryForRegions_MissingParameters() {
        given()
                .when()
                .put("/api/regions/country/update?oldCountry=USA")
                .then()
                .statusCode(400)
                .body(containsString("Both oldCountry and newCountry parameters are required"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testDeleteRegion_Success() {
        when(regionService.deleteById(1L))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .delete("/api/regions/1")
                .then()
                .statusCode(204);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testDeleteRegion_NotFound() {
        when(regionService.deleteById(999L))
                .thenReturn(Uni.createFrom().item(false));

        given()
                .when()
                .delete("/api/regions/999")
                .then()
                .statusCode(404)
                .body(containsString("Region not found with id: 999"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testDeleteRegionsByCountry_Success() {
        when(regionService.deleteByCountry("USA"))
                .thenReturn(Uni.createFrom().item(3L));

        given()
                .when()
                .delete("/api/regions/country/USA")
                .then()
                .statusCode(200)
                .body(containsString("Deleted 3 regions"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetRegionCountByCountry_Success() {
        when(regionService.countByCountry("USA"))
                .thenReturn(Uni.createFrom().item(10L));

        given()
                .when()
                .get("/api/regions/stats/country/USA")
                .then()
                .statusCode(200)
                .body(is("10"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetRegionsWithoutSettlements_Success() {
        List<Region> regions = Arrays.asList(
                createTestRegion(1L, "Empty Region 1", "USA"),
                createTestRegion(2L, "Empty Region 2", "Canada")
        );

        when(regionService.findRegionsWithoutSettlements())
                .thenReturn(Uni.createFrom().item(regions));

        given()
                .when()
                .get("/api/regions/stats/no-settlements")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetSettlementCountByRegion_Success() {
        // Assuming this returns a list of objects with region and count
        when(regionService.getSettlementCountByRegion())
                .thenReturn(Uni.createFrom().item(List.of()));

        given()
                .when()
                .get("/api/regions/stats/settlement-count")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCheckRegionExistsByNameAndCountry_Success() {
        when(regionService.existsByNameAndCountry("California", "USA"))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .get("/api/regions/exists/name-country?name=California&country=USA")
                .then()
                .statusCode(200)
                .body(is("true"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCheckRegionExistsByNameAndCountry_MissingParameters() {
        given()
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
                .statusCode(401); // Unauthorized
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testServiceErrorHandling() {
        when(regionService.findById(1L))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Database error")));

        given()
                .when()
                .get("/api/regions/1")
                .then()
                .statusCode(500)
                .body(containsString("Error retrieving region"));
    }
}