package by.losik;

import by.losik.entity.Product;
import by.losik.service.ProductService;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@QuarkusTest
class ProductResourceTest {

    @InjectMock
    ProductService productService;

    private Product createTestProduct(Long id, String name, String code, String manufacturer) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setCode(code);
        product.setManufacturer(manufacturer);
        return product;
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetAllProducts_Success() {
        List<Product> products = Arrays.asList(
                createTestProduct(1L, "Laptop", "LP001", "Dell"),
                createTestProduct(2L, "Mouse", "MS001", "Logitech")
        );

        when(productService.findPaginatedSorted(anyInt(), anyInt(), any(Sort.class)))
                .thenReturn(Uni.createFrom().item(products));

        given()
                .when()
                .get("/api/products?page=0&size=20&sort=name&direction=asc")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("Laptop"))
                .body("[1].name", is("Mouse"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetProductById_Success() {
        Product product = createTestProduct(1L, "Laptop", "LP001", "Dell");

        when(productService.findById(1L))
                .thenReturn(Uni.createFrom().item(product));

        given()
                .when()
                .get("/api/products/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("name", is("Laptop"))
                .body("code", is("LP001"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetProductById_NotFound() {
        when(productService.findById(999L))
                .thenReturn(Uni.createFrom().item((Product) null));

        given()
                .when()
                .get("/api/products/999")
                .then()
                .statusCode(404)
                .body(containsString("Product not found"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetProductByCode_Success() {
        Product product = createTestProduct(1L, "Laptop", "LP001", "Dell");

        when(productService.findByCode("LP001"))
                .thenReturn(Uni.createFrom().item(Optional.of(product)));

        given()
                .when()
                .get("/api/products/code/LP001")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("code", is("LP001"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetProductByCode_NotFound() {
        when(productService.findByCode("INVALID"))
                .thenReturn(Uni.createFrom().item(Optional.empty()));

        given()
                .when()
                .get("/api/products/code/INVALID")
                .then()
                .statusCode(404)
                .body(containsString("Product not found"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetProductsByCategory_Success() {
        List<Product> products = Arrays.asList(
                createTestProduct(1L, "Laptop", "LP001", "Dell"),
                createTestProduct(2L, "Desktop", "DT001", "HP")
        );

        when(productService.findByCategoryId(1L))
                .thenReturn(Uni.createFrom().item(products));

        given()
                .when()
                .get("/api/products/category/1")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("Laptop"))
                .body("[1].name", is("Desktop"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetProductsByManufacturer_Success() {
        List<Product> products = Arrays.asList(
                createTestProduct(1L, "Laptop", "LP001", "Dell"),
                createTestProduct(2L, "Monitor", "MN001", "Dell")
        );

        when(productService.findByManufacturer("Dell"))
                .thenReturn(Uni.createFrom().item(products));

        given()
                .when()
                .get("/api/products/manufacturer/Dell")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].manufacturer", is("Dell"))
                .body("[1].manufacturer", is("Dell"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testSearchProducts_Success() {
        List<Product> products = Arrays.asList(
                createTestProduct(1L, "Gaming Laptop", "GL001", "Dell"),
                createTestProduct(2L, "Gaming Mouse", "GM001", "Logitech")
        );

        when(productService.searchProductsPaginated(anyString(), anyInt(), anyInt()))
                .thenReturn(Uni.createFrom().item(products));

        given()
                .when()
                .get("/api/products/search?q=gaming&page=0&size=20")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("Gaming Laptop"))
                .body("[1].name", is("Gaming Mouse"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testSearchProducts_MissingTerm() {
        given()
                .when()
                .get("/api/products/search?q=")
                .then()
                .statusCode(400)
                .body(containsString("Search term cannot be empty"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetProductsByCodeContaining_Success() {
        List<Product> products = Arrays.asList(
                createTestProduct(1L, "Laptop", "LP001", "Dell"),
                createTestProduct(2L, "Laptop Stand", "LP002", "Dell")
        );

        when(productService.findByCodeContaining("LP"))
                .thenReturn(Uni.createFrom().item(products));

        given()
                .when()
                .get("/api/products/code-contains/LP")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].code", is("LP001"))
                .body("[1].code", is("LP002"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetProductsByNameContaining_Success() {
        List<Product> products = Arrays.asList(
                createTestProduct(1L, "Gaming Laptop", "GL001", "Dell"),
                createTestProduct(2L, "Gaming Keyboard", "GK001", "Logitech")
        );

        when(productService.findByNameContaining("Gaming"))
                .thenReturn(Uni.createFrom().item(products));

        given()
                .when()
                .get("/api/products/name-contains/Gaming")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("Gaming Laptop"))
                .body("[1].name", is("Gaming Keyboard"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetProductsByManufacturerContaining_Success() {
        List<Product> products = Arrays.asList(
                createTestProduct(1L, "Laptop", "LP001", "Dell Inc."),
                createTestProduct(2L, "Monitor", "MN001", "Dell Technologies")
        );

        when(productService.findByManufacturerContaining("Dell"))
                .thenReturn(Uni.createFrom().item(products));

        given()
                .when()
                .get("/api/products/manufacturer-contains/Dell")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].manufacturer", is("Dell Inc."))
                .body("[1].manufacturer", is("Dell Technologies"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateProduct_Success() {
        Product savedProduct = createTestProduct(1L, "New Product", "NP001", "New Manufacturer");

        when(productService.existsByCode("NP001"))
                .thenReturn(Uni.createFrom().item(false));
        when(productService.save(any(Product.class)))
                .thenReturn(Uni.createFrom().item(savedProduct));

        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"New Product\", \"code\": \"NP001\", \"manufacturer\": \"New Manufacturer\"}")
                .when()
                .post("/api/products")
                .then()
                .statusCode(201)
                .body("id", is(1))
                .body("name", is("New Product"))
                .body("code", is("NP001"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateProduct_MissingCode() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"New Product\", \"manufacturer\": \"New Manufacturer\"}")
                .when()
                .post("/api/products")
                .then()
                .statusCode(400)
                .body(containsString("Product code is required"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateProduct_MissingName() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"code\": \"NP001\", \"manufacturer\": \"New Manufacturer\"}")
                .when()
                .post("/api/products")
                .then()
                .statusCode(400)
                .body(containsString("Product name is required"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateProduct_DuplicateCode() {
        when(productService.existsByCode("EXISTING"))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"New Product\", \"code\": \"EXISTING\", \"manufacturer\": \"New Manufacturer\"}")
                .when()
                .post("/api/products")
                .then()
                .statusCode(409)
                .body(containsString("already exists"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateProduct_Success() {
        Product updatedProduct = createTestProduct(1L, "Updated Product", "UP001", "Updated Manufacturer");

        when(productService.update(any(Product.class)))
                .thenReturn(Uni.createFrom().item(updatedProduct));

        given()
                .contentType(ContentType.JSON)
                .body("{\"id\": 1, \"name\": \"Updated Product\", \"code\": \"UP001\", \"manufacturer\": \"Updated Manufacturer\"}")
                .when()
                .put("/api/products/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("name", is("Updated Product"))
                .body("code", is("UP001"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateProduct_IdMismatch() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"id\": 2, \"name\": \"Updated Product\", \"code\": \"UP001\", \"manufacturer\": \"Updated Manufacturer\"}")
                .when()
                .put("/api/products/1")
                .then()
                .statusCode(400)
                .body(containsString("ID in path does not match ID in request body"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateProductCode_Success() {
        when(productService.updateProductCode(1L, "NEWCODE"))
                .thenReturn(Uni.createFrom().item(1));

        given()
                .contentType(ContentType.JSON)
                .body("NEWCODE")
                .when()
                .patch("/api/products/1/code")
                .then()
                .statusCode(200)
                .body(containsString("Product code updated successfully"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateProductCode_EmptyCode() {
        given()
                .contentType(ContentType.JSON)
                .body("")
                .when()
                .patch("/api/products/1/code")
                .then()
                .statusCode(400)
                .body(containsString("Product code cannot be empty"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateProductCode_NotFound() {
        when(productService.updateProductCode(999L, "NEWCODE"))
                .thenReturn(Uni.createFrom().item(0));

        given()
                .contentType(ContentType.JSON)
                .body("NEWCODE")
                .when()
                .patch("/api/products/999/code")
                .then()
                .statusCode(404)
                .body(containsString("Product not found"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateProductName_Success() {
        when(productService.updateProductName(1L, "New Name"))
                .thenReturn(Uni.createFrom().item(1));

        given()
                .contentType(ContentType.JSON)
                .body("New Name")
                .when()
                .patch("/api/products/1/name")
                .then()
                .statusCode(200)
                .body(containsString("Product name updated successfully"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateProductName_EmptyName() {
        given()
                .contentType(ContentType.JSON)
                .body("")
                .when()
                .patch("/api/products/1/name")
                .then()
                .statusCode(400)
                .body(containsString("Product name cannot be empty"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testDeleteProduct_Success() {
        when(productService.deleteById(1L))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .delete("/api/products/1")
                .then()
                .statusCode(204);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testDeleteProduct_NotFound() {
        when(productService.deleteById(999L))
                .thenReturn(Uni.createFrom().item(false));

        given()
                .when()
                .delete("/api/products/999")
                .then()
                .statusCode(404)
                .body(containsString("Product not found"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testDeleteProductsByCategory_Success() {
        when(productService.deleteByCategory(1L))
                .thenReturn(Uni.createFrom().item(5L));

        given()
                .when()
                .delete("/api/products/category/1")
                .then()
                .statusCode(200)
                .body(containsString("Deleted 5 products"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testDeleteProductsByManufacturer_Success() {
        when(productService.deleteByManufacturer("Dell"))
                .thenReturn(Uni.createFrom().item(3L));

        given()
                .when()
                .delete("/api/products/manufacturer/Dell")
                .then()
                .statusCode(200)
                .body(containsString("Deleted 3 products"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetProductCountByCategory_Success() {
        when(productService.countByCategory(1L))
                .thenReturn(Uni.createFrom().item(10L));

        given()
                .when()
                .get("/api/products/stats/category/1")
                .then()
                .statusCode(200)
                .body(is("10"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetProductCountByManufacturer_Success() {
        when(productService.countByManufacturer("Dell"))
                .thenReturn(Uni.createFrom().item(15L));

        given()
                .when()
                .get("/api/products/stats/manufacturer/Dell")
                .then()
                .statusCode(200)
                .body(is("15"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCheckProductExistsByCode_Success() {
        when(productService.existsByCode("EXISTING"))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .get("/api/products/exists/code/EXISTING")
                .then()
                .statusCode(200)
                .body(is("true"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCheckProductExistsByNameAndManufacturer_Success() {
        when(productService.existsByNameAndManufacturer("Laptop", "Dell"))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .get("/api/products/exists/name-manufacturer?name=Laptop&manufacturer=Dell")
                .then()
                .statusCode(200)
                .body(is("true"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCheckProductExistsByNameAndManufacturer_MissingParams() {
        given()
                .when()
                .get("/api/products/exists/name-manufacturer?name=Laptop")
                .then()
                .statusCode(400)
                .body(containsString("Both name and manufacturer parameters are required"));
    }

    @Test
    void testUnauthenticatedAccess() {
        given()
                .when()
                .get("/api/products")
                .then()
                .statusCode(401); // Unauthorized
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testServiceErrorHandling() {
        when(productService.findById(1L))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Database error")));

        given()
                .when()
                .get("/api/products/1")
                .then()
                .statusCode(500)
                .body(containsString("Error retrieving product"));
    }
}