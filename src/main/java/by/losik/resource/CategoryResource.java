package by.losik.resource;

import by.losik.entity.Category;
import by.losik.service.CategoryService;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Path("/api/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
@Authenticated
public class CategoryResource {

    @Inject
    CategoryService categoryService;

    @GET
    public Uni<Response> getAll(
            @QueryParam("sort") String sortField,
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        log.info("Getting all categories, page: {}, size: {}, sort: {}", page, size, sortField);

        return Uni.createFrom().item(page)
                .onItem().transform(p -> p < 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Page index cannot be negative")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        Uni.createFrom().item(size)
                                .onItem().transform(s -> s <= 0 || s > 100)
                                .onItem().transform(isInvalid -> {
                                    if (isInvalid) {
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Page size must be between 1 and 100")
                                                .build();
                                    }
                                    return null;
                                })
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() -> {
                    Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                            ? Sort.Direction.Descending
                            : Sort.Direction.Ascending;

                    Sort sort = sortField != null
                            ? Sort.by(sortField, sortDirection)
                            : Sort.by("name", sortDirection);

                    return categoryService.findPaginatedSorted(page, size, sort)
                            .onItem().transform(categories -> Response.ok(categories).build());
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting all categories", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving categories: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getById(@PathParam("id") Long id) {
        log.info("Getting category by id: {}", id);

        return Uni.createFrom().item(id)
                .onItem().transform(i -> i == null || i <= 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid category ID")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        categoryService.findById(id)
                                .onItem().transform(category -> {
                                    if (category == null) {
                                        log.warn("Category with id {} not found", id);
                                        return Response.status(Response.Status.NOT_FOUND).build();
                                    }
                                    return Response.ok(category).build();
                                })
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting category by id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving category: " + throwable.getMessage())
                            .build();
                });
    }

    @POST
    public Uni<Response> create(Category category) {
        log.info("Creating new category: {}", category != null ? category.getName() : "null");

        return Uni.createFrom().item(category)
                .onItem().transform(c -> c == null || c.getName() == null || c.getName().trim().isEmpty())
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Category name is required")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        Uni.createFrom().item(category.getName().length() > 100)
                                .onItem().transform(isInvalid -> {
                                    if (isInvalid) {
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Category name must not exceed 100 characters")
                                                .build();
                                    }
                                    return null;
                                })
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        categoryService.save(category)
                                .onItem().transform(savedCategory ->
                                        Response.status(Response.Status.CREATED)
                                                .entity(savedCategory)
                                                .build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error creating category: {}", category != null ? category.getName() : "null", throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error creating category: " + throwable.getMessage())
                            .build();
                });
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> update(@PathParam("id") Long id, Category category) {
        log.info("Updating category with id: {}", id);

        return Uni.createFrom().item(id)
                .onItem().transform(i -> i == null || i <= 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid category ID")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        Uni.createFrom().item(category)
                                .onItem().transform(c -> c == null || c.getName() == null || c.getName().trim().isEmpty())
                                .onItem().transform(isInvalid -> {
                                    if (isInvalid) {
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Category name is required")
                                                .build();
                                    }
                                    return null;
                                })
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        Uni.createFrom().item(category.getName().length() > 100)
                                .onItem().transform(isInvalid -> {
                                    if (isInvalid) {
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Category name must not exceed 100 characters")
                                                .build();
                                    }
                                    return null;
                                })
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() -> {
                    category.setId(id);
                    return categoryService.update(category)
                            .onItem().transform(updatedCategory -> Response.ok(updatedCategory).build());
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating category with id: {}", id, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error updating category: " + throwable.getMessage())
                            .build();
                });
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> delete(@PathParam("id") Long id) {
        log.info("Deleting category with id: {}", id);

        return Uni.createFrom().item(id)
                .onItem().transform(i -> i == null || i <= 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid category ID")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        categoryService.deleteById(id)
                                .onItem().transform(deleted -> {
                                    if (deleted) {
                                        return Response.noContent().build();
                                    } else {
                                        return Response.status(Response.Status.NOT_FOUND).build();
                                    }
                                })
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error deleting category with id: {}", id, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error deleting category: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/letter/{letter}")
    public Uni<Response> getByLetter(
            @PathParam("letter") String letter,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        log.info("Getting categories starting with letter: {}, page: {}, size: {}", letter, page, size);

        return Uni.createFrom().item(letter)
                .onItem().transform(l -> l == null || l.length() != 1 || !Character.isLetter(l.charAt(0)))
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Letter must be a single character")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        categoryService.findCategoriesStartingWithLetter(letter, page, size)
                                .onItem().transform(categories -> Response.ok(categories).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting categories by letter: {}", letter, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving categories: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/letter/{letter}/count")
    public Uni<Response> countByLetter(@PathParam("letter") String letter) {
        log.info("Counting categories starting with letter: {}", letter);

        return Uni.createFrom().item(letter)
                .onItem().transform(l -> l == null || l.length() != 1 || !Character.isLetter(l.charAt(0)))
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Letter must be a single character")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        categoryService.countCategoriesStartingWithLetter(letter)
                                .onItem().transform(count -> Response.ok(count).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error counting categories by letter: {}", letter, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error counting categories: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/search")
    public Uni<Response> search(
            @QueryParam("q") String searchTerm,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        log.info("Searching categories with term: {}, page: {}, size: {}", searchTerm, page, size);

        return Uni.createFrom().item(searchTerm)
                .onItem().transform(term -> term == null || term.trim().isEmpty())
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Search term is required")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        categoryService.findByNamePaginated("%" + searchTerm + "%", page, size)
                                .onItem().transform(categories -> Response.ok(categories).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error searching categories with term: {}", searchTerm, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error searching categories: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/exists/name/{name}")
    public Uni<Response> existsByName(@PathParam("name") String name) {
        log.info("Checking if category exists by name: {}", name);

        return Uni.createFrom().item(name)
                .onItem().transform(n -> n == null || n.trim().isEmpty())
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Category name is required")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        categoryService.existsByName(name)
                                .onItem().transform(exists -> Response.ok(exists).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error checking category existence by name: {}", name, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error checking category existence: " + throwable.getMessage())
                            .build();
                });
    }
}