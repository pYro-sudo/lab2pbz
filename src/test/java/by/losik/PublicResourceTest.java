package by.losik;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class PublicResourceTest {

    @Test
    void testHealthCheck() {
        given()
                .when()
                .get("/public/health")
                .then()
                .statusCode(200)
                .contentType(ContentType.TEXT)
                .body(is("OK"));
    }

    @Test
    void testInfo() {
        given()
                .when()
                .get("/public/info")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("name", is("Quarkus Lab API"))
                .body("version", is("1.0"));
    }

    @Test
    void testHealthCheckContentType() {
        given()
                .when()
                .get("/public/health")
                .then()
                .statusCode(200)
                .contentType(ContentType.TEXT.withCharset("UTF-8"));
    }

    @Test
    void testInfoContentType() {
        given()
                .when()
                .get("/public/info")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    void testNonExistentEndpoint() {
        given()
                .when()
                .get("/public/nonexistent")
                .then()
                .statusCode(404);
    }

    @Test
    void testRootPublicPath() {
        given()
                .when()
                .get("/public")
                .then()
                .statusCode(404);
    }

    @Test
    void testHealthCheckWithQueryParams() {
        given()
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
                .when()
                .get("/public/health")
                .then()
                .statusCode(200)
                .body(is("OK"));

        given()
                .when()
                .get("/public/health")
                .then()
                .statusCode(200)
                .body(is("OK"));

        given()
                .when()
                .get("/public/health")
                .then()
                .statusCode(200)
                .body(is("OK"));
    }

    @Test
    void testMultipleInfoCalls() {
        given()
                .when()
                .get("/public/info")
                .then()
                .statusCode(200)
                .body("name", is("Quarkus Lab API"))
                .body("version", is("1.0"));

        given()
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
                .when()
                .post("/public/health")
                .then()
                .statusCode(405); // Method Not Allowed

        given()
                .when()
                .put("/public/health")
                .then()
                .statusCode(405);

        given()
                .when()
                .delete("/public/health")
                .then()
                .statusCode(405);

        given()
                .when()
                .post("/public/info")
                .then()
                .statusCode(405);

        given()
                .when()
                .put("/public/info")
                .then()
                .statusCode(405);

        given()
                .when()
                .delete("/public/info")
                .then()
                .statusCode(405);
    }

    @Test
    void testResponseHeaders() {
        given()
                .when()
                .get("/public/health")
                .then()
                .statusCode(200)
                .header("Content-Type", "text/plain;charset=UTF-8");

        given()
                .when()
                .get("/public/info")
                .then()
                .statusCode(200)
                .header("Content-Type", "application/json;charset=UTF-8");
    }
}