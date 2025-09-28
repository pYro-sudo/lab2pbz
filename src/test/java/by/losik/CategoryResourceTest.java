package by.losik;

import by.losik.entity.Category;
import by.losik.service.CategoryService;
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
class CategoryResourceTest {

    @InjectMock
    CategoryService categoryService;

    private Category createTestCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetAllCategories_Success() {
        List<Category> categories = Arrays.asList(
                createTestCategory(1L, "Electronics"),
                createTestCategory(2L, "Books")
        );

        when(categoryService.findPaginatedSorted(anyInt(), anyInt(), any(Sort.class)))
                .thenReturn(Uni.createFrom().item(categories));

        given()
                .when()
                .get("/api/categories?page=0&size=20&sort=name&direction=asc")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("Electronics"))
                .body("[1].name", is("Books"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetAllCategories_InvalidPage() {
        given()
                .when()
                .get("/api/categories?page=-1&size=20")
                .then()
                .statusCode(400)
                .body(containsString("Page index cannot be negative"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetAllCategories_InvalidSize() {
        given()
                .when()
                .get("/api/categories?page=0&size=0")
                .then()
                .statusCode(400)
                .body(containsString("Page size must be between 1 and 100"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetCategoryById_Success() {
        Category category = createTestCategory(1L, "Electronics");

        when(categoryService.findById(1L))
                .thenReturn(Uni.createFrom().item(category));

        given()
                .when()
                .get("/api/categories/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("name", is("Electronics"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetCategoryById_NotFound() {
        when(categoryService.findById(999L))
                .thenReturn(Uni.createFrom().item((Category) null));

        given()
                .when()
                .get("/api/categories/999")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetCategoryById_InvalidId() {
        given()
                .when()
                .get("/api/categories/0")
                .then()
                .statusCode(400)
                .body(containsString("Invalid category ID"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateCategory_Success() {
        Category savedCategory = createTestCategory(1L, "New Category");

        when(categoryService.save(any(Category.class)))
                .thenReturn(Uni.createFrom().item(savedCategory));

        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"New Category\"}")
                .when()
                .post("/api/categories")
                .then()
                .statusCode(201)
                .body("id", is(1))
                .body("name", is("New Category"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateCategory_MissingName() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"\"}")
                .when()
                .post("/api/categories")
                .then()
                .statusCode(400)
                .body(containsString("Category name is required"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCreateCategory_NameTooLong() {
        String longName = "A".repeat(101);

        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"" + longName + "\"}")
                .when()
                .post("/api/categories")
                .then()
                .statusCode(400)
                .body(containsString("Category name must not exceed 100 characters"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateCategory_Success() {
        Category updatedCategory = createTestCategory(1L, "Updated Category");

        when(categoryService.update(any(Category.class)))
                .thenReturn(Uni.createFrom().item(updatedCategory));

        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"Updated Category\"}")
                .when()
                .put("/api/categories/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("name", is("Updated Category"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testUpdateCategory_InvalidId() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"Updated Category\"}")
                .when()
                .put("/api/categories/0")
                .then()
                .statusCode(400)
                .body(containsString("Invalid category ID"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testDeleteCategory_Success() {
        when(categoryService.deleteById(1L))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .delete("/api/categories/1")
                .then()
                .statusCode(204);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testDeleteCategory_NotFound() {
        when(categoryService.deleteById(999L))
                .thenReturn(Uni.createFrom().item(false));

        given()
                .when()
                .delete("/api/categories/999")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetCategoriesByLetter_Success() {
        List<Category> categories = Arrays.asList(
                createTestCategory(1L, "Apple"),
                createTestCategory(2L, "Amazon")
        );

        when(categoryService.findCategoriesStartingWithLetter(anyString(), anyInt(), anyInt()))
                .thenReturn(Uni.createFrom().item(categories));

        given()
                .when()
                .get("/api/categories/letter/A?page=0&size=20")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("Apple"))
                .body("[1].name", is("Amazon"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testGetCategoriesByLetter_InvalidLetter() {
        given()
                .when()
                .get("/api/categories/letter/123")
                .then()
                .statusCode(400)
                .body(containsString("Letter must be a single character"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testCountCategoriesByLetter_Success() {
        when(categoryService.countCategoriesStartingWithLetter("A"))
                .thenReturn(Uni.createFrom().item(5L));

        given()
                .when()
                .get("/api/categories/letter/A/count")
                .then()
                .statusCode(200)
                .body(is("5"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testSearchCategories_Success() {
        List<Category> categories = Arrays.asList(
                createTestCategory(1L, "Electronics"),
                createTestCategory(2L, "Electronic Gadgets")
        );

        when(categoryService.findByNamePaginated(anyString(), anyInt(), anyInt()))
                .thenReturn(Uni.createFrom().item(categories));

        given()
                .when()
                .get("/api/categories/search?q=electronic&page=0&size=20")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].name", is("Electronics"))
                .body("[1].name", is("Electronic Gadgets"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testSearchCategories_MissingTerm() {
        given()
                .when()
                .get("/api/categories/search?q=")
                .then()
                .statusCode(400)
                .body(containsString("Search term is required"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testExistsByName_Success() {
        when(categoryService.existsByName("Electronics"))
                .thenReturn(Uni.createFrom().item(true));

        given()
                .when()
                .get("/api/categories/exists/name/Electronics")
                .then()
                .statusCode(200)
                .body(is("true"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testExistsByName_MissingName() {
        given()
                .when()
                .get("/api/categories/exists/name/")
                .then()
                .statusCode(404);
    }

    @Test
    void testUnauthenticatedAccess() {
        given()
                .when()
                .get("/api/categories")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"user"})
    void testServiceErrorHandling() {
        when(categoryService.findById(1L))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Database error")));

        given()
                .when()
                .get("/api/categories/1")
                .then()
                .statusCode(500)
                .body(containsString("Error retrieving category"));
    }
}