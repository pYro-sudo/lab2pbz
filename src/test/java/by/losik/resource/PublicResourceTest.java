package by.losik.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class PublicResourceTest {

    @ConfigProperty(name = "app.auth.username")
    String VALID_USERNAME;
    @ConfigProperty(name = "app.auth.password")
    String VALID_PASSWORD;

    @Test
    void testHealthCheck() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/public/health")
                .then()
                .statusCode(200)
                .contentType(ContentType.TEXT)
                .body(is("OK"));
    }

    @Test
    void testHealthCheckContentType() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/public/health")
                .then()
                .statusCode(200)
                .contentType(ContentType.TEXT.withCharset("UTF-8"));
    }

    @Test
    void testInfoContentType() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/public/info")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    void testNonExistentEndpoint() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/public/nonexistent")
                .then()
                .statusCode(404);
    }

    @Test
    void testRootPublicPath() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/public")
                .then()
                .statusCode(404);
    }

    @Test
    void testHealthCheckWithQueryParams() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .queryParam("check", "deep")
                .when()
                .get("/public/health")
                .then()
                .statusCode(200)
                .body(is("OK"));
    }

    @Test
    void testInfoWithQueryParams() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .queryParam("details", "true")
                .when()
                .get("/public/info")
                .then()
                .statusCode(200)
                .body("name", is("Quarkus Lab API"))
                .body("version", is("1.0"));
    }

    @Test
    void testMultipleHealthCalls() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/public/health")
                .then()
                .statusCode(200)
                .body(is("OK"));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/public/health")
                .then()
                .statusCode(200)
                .body(is("OK"));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/public/health")
                .then()
                .statusCode(200)
                .body(is("OK"));
    }

    @Test
    void testMultipleInfoCalls() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/public/info")
                .then()
                .statusCode(200)
                .body("name", is("Quarkus Lab API"))
                .body("version", is("1.0"));

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/public/info")
                .then()
                .statusCode(200)
                .body("name", is("Quarkus Lab API"))
                .body("version", is("1.0"));
    }

    @Test
    void testHttpMethodsNotAllowed() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .post("/public/health")
                .then()
                .statusCode(405); // Method Not Allowed

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .put("/public/health")
                .then()
                .statusCode(405);

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .delete("/public/health")
                .then()
                .statusCode(405);

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .post("/public/info")
                .then()
                .statusCode(405);

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .put("/public/info")
                .then()
                .statusCode(405);

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .delete("/public/info")
                .then()
                .statusCode(405);
    }

    @Test
    void testResponseHeaders() {
        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/public/health")
                .then()
                .statusCode(200)
                .header("Content-Type", "text/plain;charset=UTF-8");

        given()
                .auth().preemptive().basic(VALID_USERNAME, VALID_PASSWORD)
                .when()
                .get("/public/info")
                .then()
                .statusCode(200)
                .header("Content-Type", "application/json;charset=UTF-8");
    }
}