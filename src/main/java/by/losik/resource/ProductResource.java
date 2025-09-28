package by.losik.resource;

import by.losik.entity.Product;
import by.losik.service.ProductService;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Resource
@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
@Authenticated
public class ProductResource {

    @Inject
    ProductService productService;

    @GET
    public Uni<Response> getAllProducts(
            @QueryParam("sort") @DefaultValue("name") String sortField,
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @QueryParam("page") @DefaultValue("0") int pageIndex,
            @QueryParam("size") @DefaultValue("20") int pageSize) {

        log.info("Getting all products sorted by {} {}, page: {}, size: {}",
                sortField, direction, pageIndex, pageSize);

        return productService.findPaginatedSorted(pageIndex, pageSize,
                        io.quarkus.panache.common.Sort.by(sortField,
                                io.quarkus.panache.common.Sort.Direction.valueOf(direction.toUpperCase())))
                .onItem().transform(products -> {
                    log.debug("Retrieved {} products", products.size());
                    return Response.ok(products).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting all products", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving products: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getProductById(@PathParam("id") Long id) {
        log.info("Getting product by id: {}", id);

        return productService.findById(id)
                .onItem().ifNotNull().transform(product -> {
                    log.debug("Found product with id: {}", id);
                    return Response.ok(product).build();
                })
                .onItem().ifNull().continueWith(() -> {
                    log.warn("Product with id {} not found", id);
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("Product not found with id: " + id)
                            .build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting product by id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving product: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/code/{code}")
    public Uni<Response> getProductByCode(@PathParam("code") String code) {
        log.info("Getting product by code: {}", code);

        return productService.findByCode(code)
                .onItem().transform(optionalProduct -> optionalProduct
                        .map(product -> {
                            log.debug("Found product with code: {}", code);
                            return Response.ok(product).build();
                        })
                        .orElseGet(() -> {
                            log.warn("Product with code {} not found", code);
                            return Response.status(Response.Status.NOT_FOUND)
                                    .entity("Product not found with code: " + code)
                                    .build();
                        }))
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting product by code: {}", code, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving product: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/category/{categoryId}")
    public Uni<Response> getProductsByCategory(@PathParam("categoryId") Long categoryId) {
        log.info("Getting products by category id: {}", categoryId);

        return productService.findByCategoryId(categoryId)
                .onItem().transform(products -> {
                    log.debug("Found {} products for category id: {}", products.size(), categoryId);
                    return Response.ok(products).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting products by category id: {}", categoryId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving products: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/manufacturer/{manufacturer}")
    public Uni<Response> getProductsByManufacturer(@PathParam("manufacturer") String manufacturer) {
        log.info("Getting products by manufacturer: {}", manufacturer);

        return productService.findByManufacturer(manufacturer)
                .onItem().transform(products -> {
                    log.debug("Found {} products by manufacturer: {}", products.size(), manufacturer);
                    return Response.ok(products).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting products by manufacturer: {}", manufacturer, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving products: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/search")
    public Uni<Response> searchProducts(
            @QueryParam("q") String searchTerm,
            @QueryParam("page") @DefaultValue("0") int pageIndex,
            @QueryParam("size") @DefaultValue("20") int pageSize) {

        log.info("Searching products with term: {}, page: {}, size: {}", searchTerm, pageIndex, pageSize);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Search term cannot be empty")
                    .build());
        }

        Uni<Response> searchUni;

        if (pageIndex >= 0 && pageSize > 0) {
            searchUni = productService.searchProductsPaginated(searchTerm, pageIndex, pageSize)
                    .onItem().transform(products -> {
                        log.debug("Found {} products matching search term: {}", products.size(), searchTerm);
                        return Response.ok(products).build();
                    });
        } else {
            searchUni = productService.searchProducts(searchTerm)
                    .onItem().transform(products -> {
                        log.debug("Found {} products matching search term: {}", products.size(), searchTerm);
                        return Response.ok(products).build();
                    });
        }

        return searchUni.onFailure().recoverWithItem(throwable -> {
            log.error("Error searching products with term: {}", searchTerm, throwable);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error searching products: " + throwable.getMessage())
                    .build();
        });
    }

    @GET
    @Path("/code-contains/{codePart}")
    public Uni<Response> getProductsByCodeContaining(@PathParam("codePart") String codePart) {
        log.info("Getting products by code containing: {}", codePart);

        return productService.findByCodeContaining(codePart)
                .onItem().transform(products -> {
                    log.debug("Found {} products with code containing: {}", products.size(), codePart);
                    return Response.ok(products).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting products by code containing: {}", codePart, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving products: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/name-contains/{namePart}")
    public Uni<Response> getProductsByNameContaining(@PathParam("namePart") String namePart) {
        log.info("Getting products by name containing: {}", namePart);

        return productService.findByNameContaining(namePart)
                .onItem().transform(products -> {
                    log.debug("Found {} products with name containing: {}", products.size(), namePart);
                    return Response.ok(products).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting products by name containing: {}", namePart, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving products: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/manufacturer-contains/{manufacturerPart}")
    public Uni<Response> getProductsByManufacturerContaining(@PathParam("manufacturerPart") String manufacturerPart) {
        log.info("Getting products by manufacturer containing: {}", manufacturerPart);

        return productService.findByManufacturerContaining(manufacturerPart)
                .onItem().transform(products -> {
                    log.debug("Found {} products with manufacturer containing: {}", products.size(), manufacturerPart);
                    return Response.ok(products).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting products by manufacturer containing: {}", manufacturerPart, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving products: " + throwable.getMessage())
                            .build();
                });
    }

    @POST
    public Uni<Response> createProduct(Product product) {
        log.info("Creating new product: {}, code: {}", product.name(), product.code());

        // Валидация обязательных полей
        if (product.code() == null || product.code().trim().isEmpty()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Product code is required")
                    .build());
        }

        if (product.name() == null || product.name().trim().isEmpty()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Product name is required")
                    .build());
        }

        return productService.existsByCode(product.code())
                .onItem().transformToUni(exists -> {
                    if (exists) {
                        log.warn("Product with code {} already exists", product.code());
                        return Uni.createFrom().item(Response.status(Response.Status.CONFLICT)
                                .entity("Product with code " + product.code() + " already exists")
                                .build());
                    } else {
                        return productService.save(product)
                                .onItem().transform(savedProduct -> {
                                    log.info("Successfully created product with id: {}", savedProduct.id());
                                    return Response.status(Response.Status.CREATED)
                                            .entity(savedProduct)
                                            .build();
                                });
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error creating product", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error creating product: " + throwable.getMessage())
                            .build();
                });
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> updateProduct(@PathParam("id") Long id, Product product) {
        log.info("Updating product with id: {}", id);

        // Убеждаемся, что ID в пути совпадает с ID в теле
        if (!id.equals(product.id())) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("ID in path does not match ID in request body")
                    .build());
        }

        return productService.update(product)
                .onItem().transform(updatedProduct -> {
                    log.info("Successfully updated product with id: {}", id);
                    return Response.ok(updatedProduct).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating product with id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error updating product: " + throwable.getMessage())
                            .build();
                });
    }

    @PATCH
    @Path("/{id}/code")
    public Uni<Response> updateProductCode(@PathParam("id") Long id, String newCode) {
        log.info("Updating product code for id: {} to {}", id, newCode);

        if (newCode == null || newCode.trim().isEmpty()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Product code cannot be empty")
                    .build());
        }

        return productService.updateProductCode(id, newCode)
                .onItem().transform(updatedCount -> {
                    if (updatedCount > 0) {
                        log.info("Successfully updated product code for id: {}", id);
                        return Response.ok().entity("Product code updated successfully").build();
                    } else {
                        log.warn("No product found with id: {}", id);
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("Product not found with id: " + id)
                                .build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating product code for id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error updating product code: " + throwable.getMessage())
                            .build();
                });
    }

    @PATCH
    @Path("/{id}/name")
    public Uni<Response> updateProductName(@PathParam("id") Long id, String newName) {
        log.info("Updating product name for id: {} to {}", id, newName);

        if (newName == null || newName.trim().isEmpty()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Product name cannot be empty")
                    .build());
        }

        return productService.updateProductName(id, newName)
                .onItem().transform(updatedCount -> {
                    if (updatedCount > 0) {
                        log.info("Successfully updated product name for id: {}", id);
                        return Response.ok().entity("Product name updated successfully").build();
                    } else {
                        log.warn("No product found with id: {}", id);
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("Product not found with id: " + id)
                                .build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating product name for id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error updating product name: " + throwable.getMessage())
                            .build();
                });
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> deleteProduct(@PathParam("id") Long id) {
        log.info("Deleting product with id: {}", id);

        return productService.deleteById(id)
                .onItem().transform(deleted -> {
                    if (deleted) {
                        log.info("Successfully deleted product with id: {}", id);
                        return Response.noContent().build();
                    } else {
                        log.warn("Product with id {} not found for deletion", id);
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("Product not found with id: " + id)
                                .build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error deleting product with id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error deleting product: " + throwable.getMessage())
                            .build();
                });
    }

    @DELETE
    @Path("/category/{categoryId}")
    public Uni<Response> deleteProductsByCategory(@PathParam("categoryId") Long categoryId) {
        log.info("Deleting products by category id: {}", categoryId);

        return productService.deleteByCategory(categoryId)
                .onItem().transform(deletedCount -> {
                    log.info("Successfully deleted {} products for category id: {}", deletedCount, categoryId);
                    return Response.ok().entity("Deleted " + deletedCount + " products").build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error deleting products by category id: {}", categoryId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error deleting products: " + throwable.getMessage())
                            .build();
                });
    }

    @DELETE
    @Path("/manufacturer/{manufacturer}")
    public Uni<Response> deleteProductsByManufacturer(@PathParam("manufacturer") String manufacturer) {
        log.info("Deleting products by manufacturer: {}", manufacturer);

        return productService.deleteByManufacturer(manufacturer)
                .onItem().transform(deletedCount -> {
                    log.info("Successfully deleted {} products by manufacturer: {}", deletedCount, manufacturer);
                    return Response.ok().entity("Deleted " + deletedCount + " products").build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error deleting products by manufacturer: {}", manufacturer, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error deleting products: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/stats/category/{categoryId}")
    public Uni<Response> getProductCountByCategory(@PathParam("categoryId") Long categoryId) {
        log.info("Getting product count by category id: {}", categoryId);

        return productService.countByCategory(categoryId)
                .onItem().transform(count -> {
                    log.debug("Found {} products for category id: {}", count, categoryId);
                    return Response.ok().entity(count).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting product count by category id: {}", categoryId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving product count: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/stats/manufacturer/{manufacturer}")
    public Uni<Response> getProductCountByManufacturer(@PathParam("manufacturer") String manufacturer) {
        log.info("Getting product count by manufacturer: {}", manufacturer);

        return productService.countByManufacturer(manufacturer)
                .onItem().transform(count -> {
                    log.debug("Found {} products by manufacturer: {}", count, manufacturer);
                    return Response.ok().entity(count).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting product count by manufacturer: {}", manufacturer, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving product count: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/exists/code/{code}")
    public Uni<Response> checkProductExistsByCode(@PathParam("code") String code) {
        log.info("Checking if product exists by code: {}", code);

        return productService.existsByCode(code)
                .onItem().transform(exists -> {
                    log.debug("Product exists by code {}: {}", code, exists);
                    return Response.ok().entity(exists).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error checking if product exists by code: {}", code, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error checking product existence: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/exists/name-manufacturer")
    public Uni<Response> checkProductExistsByNameAndManufacturer(
            @QueryParam("name") String name,
            @QueryParam("manufacturer") String manufacturer) {

        log.info("Checking if product exists by name: {} and manufacturer: {}", name, manufacturer);

        if (name == null || manufacturer == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Both name and manufacturer parameters are required")
                    .build());
        }

        return productService.existsByNameAndManufacturer(name, manufacturer)
                .onItem().transform(exists -> {
                    log.debug("Product exists by name {} and manufacturer {}: {}", name, manufacturer, exists);
                    return Response.ok().entity(exists).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error checking if product exists by name and manufacturer", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error checking product existence: " + throwable.getMessage())
                            .build();
                });
    }
}