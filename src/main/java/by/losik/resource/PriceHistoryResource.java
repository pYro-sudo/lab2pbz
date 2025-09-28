package by.losik.resource;

import by.losik.entity.PriceHistory;
import by.losik.entity.Product;
import by.losik.service.PriceHistoryService;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import java.math.BigDecimal;
import java.sql.Date;

@Path("/api/price-history")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
@Authenticated
@Timeout(5000)
@Retry(maxRetries = 3, delay = 1000)
@CircuitBreaker(
        requestVolumeThreshold = 4,
        failureRatio = 0.5,
        delay = 10000,
        successThreshold = 2
)
public class PriceHistoryResource {

    @Inject
    PriceHistoryService priceHistoryService;

    @GET
    public Uni<Response> getAllPriceHistories(
            @QueryParam("sort") @DefaultValue("changeDate") String sortField,
            @QueryParam("direction") @DefaultValue("desc") String direction) {
        log.info("Getting all price histories sorted by {} {}", sortField, direction);

        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.Ascending
                : Sort.Direction.Descending;

        return priceHistoryService.findAllSorted(Sort.by(sortField, sortDirection))
                .onItem().transform(histories -> {
                    log.debug("Retrieved {} price histories", histories.size());
                    return Response.ok(histories).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting all price histories", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving price histories: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getPriceHistoryById(@PathParam("id") Long id) {
        log.info("Getting price history by id: {}", id);

        if (id == null || id <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid price history ID")
                    .build());
        }

        return priceHistoryService.findById(id)
                .onItem().transform(history -> {
                    if (history == null) {
                        log.warn("Price history with id {} not found", id);
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                    return Response.ok(history).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting price history by id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving price history: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/product/{productId}")
    public Uni<Response> getPriceHistoriesByProductId(@PathParam("productId") Long productId) {
        log.info("Getting price histories by product id: {}", productId);

        if (productId == null || productId <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid product ID")
                    .build());
        }

        return priceHistoryService.findByProductId(productId)
                .onItem().transform(histories -> {
                    log.debug("Found {} price histories for product id: {}", histories.size(), productId);
                    return Response.ok(histories).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting price histories by product id: {}", productId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving price histories: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/product/{productId}/latest")
    public Uni<Response> getLatestPriceHistoryByProductId(@PathParam("productId") Long productId) {
        log.info("Getting latest price history for product id: {}", productId);

        if (productId == null || productId <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid product ID")
                    .build());
        }

        return priceHistoryService.findLatestByProductId(productId)
                .onItem().transform(history -> {
                    if (history == null) {
                        log.warn("No price history found for product id: {}", productId);
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                    return Response.ok(history).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting latest price history for product id: {}", productId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving latest price history: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/date/{date}")
    public Uni<Response> getPriceHistoriesByDate(@PathParam("date") Date date) {
        log.info("Getting price histories by date: {}", date);

        if (date == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Date is required")
                    .build());
        }

        return priceHistoryService.findByChangeDate(date)
                .onItem().transform(histories -> {
                    log.debug("Found {} price histories for date: {}", histories.size(), date);
                    return Response.ok(histories).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting price histories by date: {}", date, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving price histories: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/date-range")
    public Uni<Response> getPriceHistoriesByDateRange(
            @QueryParam("startDate") Date startDate,
            @QueryParam("endDate") Date endDate) {
        log.info("Getting price histories by date range: {} to {}", startDate, endDate);

        if (startDate == null || endDate == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Start date and end date are required")
                    .build());
        }

        if (endDate.before(startDate)) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("End date must be after start date")
                    .build());
        }

        return priceHistoryService.findByChangeDateRange(startDate, endDate)
                .onItem().transform(histories -> {
                    log.debug("Found {} price histories for date range: {} to {}", histories.size(), startDate, endDate);
                    return Response.ok(histories).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting price histories by date range: {} to {}", startDate, endDate, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving price histories: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/price-range")
    public Uni<Response> getPriceHistoriesByPriceRange(
            @QueryParam("minPrice") BigDecimal minPrice,
            @QueryParam("maxPrice") BigDecimal maxPrice) {
        log.info("Getting price histories by price range: {} to {}", minPrice, maxPrice);

        if (minPrice == null || maxPrice == null || minPrice.compareTo(BigDecimal.ZERO) < 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Minimum price must be non-negative and both prices are required")
                    .build());
        }

        if (maxPrice.compareTo(minPrice) <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Maximum price must be greater than minimum price")
                    .build());
        }

        return priceHistoryService.findByPriceBetween(minPrice, maxPrice)
                .onItem().transform(histories -> {
                    log.debug("Found {} price histories for price range: {} to {}", histories.size(), minPrice, maxPrice);
                    return Response.ok(histories).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting price histories by price range: {} to {}", minPrice, maxPrice, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving price histories: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/product/{productId}/current-price")
    public Uni<Response> getCurrentPriceByProductId(@PathParam("productId") Long productId) {
        log.info("Getting current price for product id: {}", productId);

        if (productId == null || productId <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid product ID")
                    .build());
        }

        Product product = new Product();
        product.setId(productId);

        return priceHistoryService.getCurrentPrice(product)
                .onItem().transform(price -> {
                    log.debug("Current price for product id {}: {}", productId, price);
                    return Response.ok(price).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting current price for product id: {}", productId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving current price: " + throwable.getMessage())
                            .build();
                });
    }

    @POST
    public Uni<Response> createPriceHistory(PriceHistory priceHistory) {
        log.info("Creating new price history");

        if (priceHistory == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Price history is required")
                    .build());
        }

        // Validate required fields
        if (priceHistory.getProduct() == null || priceHistory.getProduct().getId() == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Product is required")
                    .build());
        }

        if (priceHistory.getChangeDate() == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Change date is required")
                    .build());
        }

        if (priceHistory.getPrice() == null || priceHistory.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Price must be greater than 0")
                    .build());
        }

        return priceHistoryService.save(priceHistory)
                .onItem().transform(savedHistory -> {
                    log.info("Successfully created price history with id: {}", savedHistory.getId());
                    return Response.status(Response.Status.CREATED).entity(savedHistory).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error creating price history", throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error creating price history: " + throwable.getMessage())
                            .build();
                });
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> updatePriceHistory(@PathParam("id") Long id, PriceHistory priceHistory) {
        log.info("Updating price history with id: {}", id);

        if (id == null || id <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid price history ID")
                    .build());
        }

        if (priceHistory == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Price history is required")
                    .build());
        }

        if (!id.equals(priceHistory.getId())) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("ID in path does not match ID in request body")
                    .build());
        }

        return priceHistoryService.update(priceHistory)
                .onItem().transform(updatedHistory -> {
                    log.info("Successfully updated price history with id: {}", id);
                    return Response.ok(updatedHistory).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating price history with id: {}", id, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error updating price history: " + throwable.getMessage())
                            .build();
                });
    }

    @PATCH
    @Path("/{id}/price")
    public Uni<Response> updatePrice(@PathParam("id") Long id, BigDecimal newPrice) {
        log.info("Updating price for price history id: {} to {}", id, newPrice);

        if (id == null || id <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid price history ID")
                    .build());
        }

        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Price must be greater than 0")
                    .build());
        }

        return priceHistoryService.updatePrice(id, newPrice)
                .onItem().transform(updatedCount -> {
                    if (updatedCount > 0) {
                        log.info("Successfully updated price for price history id: {}", id);
                        return Response.ok().entity("Price updated successfully").build();
                    } else {
                        log.warn("No price history found with id: {}", id);
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating price for price history id: {}", id, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error updating price: " + throwable.getMessage())
                            .build();
                });
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> deletePriceHistory(@PathParam("id") Long id) {
        log.info("Deleting price history with id: {}", id);

        if (id == null || id <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid price history ID")
                    .build());
        }

        return priceHistoryService.deleteById(id)
                .onItem().transform(deleted -> {
                    if (deleted) {
                        log.info("Successfully deleted price history with id: {}", id);
                        return Response.noContent().build();
                    } else {
                        log.warn("Price history with id {} not found for deletion", id);
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error deleting price history with id: {}", id, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error deleting price history: " + throwable.getMessage())
                            .build();
                });
    }

    @DELETE
    @Path("/product/{productId}")
    public Uni<Response> deletePriceHistoriesByProduct(@PathParam("productId") Long productId) {
        log.info("Deleting price histories for product id: {}", productId);

        if (productId == null || productId <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid product ID")
                    .build());
        }

        Product product = new Product();
        product.setId(productId);

        return priceHistoryService.deleteByProduct(product)
                .onItem().transform(deletedCount -> {
                    log.info("Successfully deleted {} price histories for product id: {}", deletedCount, productId);
                    return Response.ok().entity("Deleted " + deletedCount + " price histories").build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error deleting price histories for product id: {}", productId, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error deleting price histories: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/product/{productId}/trend")
    public Uni<Response> getPriceTrend(
            @PathParam("productId") Long productId,
            @QueryParam("limit") @DefaultValue("10") int limit) {
        log.info("Getting price trend for product id: {} with limit: {}", productId, limit);

        if (productId == null || productId <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid product ID")
                    .build());
        }

        if (limit <= 0 || limit > 100) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Limit must be between 1 and 100")
                    .build());
        }

        Product product = new Product();
        product.setId(productId);

        return priceHistoryService.getPriceTrend(product, limit)
                .onItem().transform(trend -> {
                    log.debug("Found {} price trend entries for product id: {}", trend.size(), productId);
                    return Response.ok(trend).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting price trend for product id: {}", productId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving price trend: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/recent")
    public Uni<Response> getRecentPriceChanges(@QueryParam("days") @DefaultValue("7") int days) {
        log.info("Getting recent price changes from last {} days", days);

        if (days <= 0 || days > 365) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Days must be between 1 and 365")
                    .build());
        }

        return priceHistoryService.findRecentPriceChanges(days)
                .onItem().transform(changes -> {
                    log.debug("Found {} recent price changes from last {} days", changes.size(), days);
                    return Response.ok(changes).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting recent price changes from last {} days", days, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving recent price changes: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/stats/product/{productId}")
    public Uni<Response> getPriceStatsByProductId(@PathParam("productId") Long productId) {
        log.info("Getting price statistics for product id: {}", productId);

        if (productId == null || productId <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid product ID")
                    .build());
        }

        Product product = new Product();
        product.setId(productId);

        return priceHistoryService.getMaxPriceByProduct(product)
                .onItem().transformToUni(maxPrice ->
                        priceHistoryService.getMinPriceByProduct(product)
                                .onItem().transformToUni(minPrice ->
                                        priceHistoryService.countPriceChangesByProduct(product)
                                                .onItem().transform(changeCount -> {
                                                    log.debug("Price stats for product id {}: max={}, min={}, changes={}",
                                                            productId, maxPrice.getPrice(), minPrice.getPrice(), changeCount);

                                                    JsonObject stats = new JsonObject()
                                                            .put("maxPrice", maxPrice.getPrice())
                                                            .put("minPrice", minPrice.getPrice())
                                                            .put("changeCount", changeCount);

                                                    return Response.ok(stats).build();
                                                })
                                )
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting price statistics for product id: {}", productId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving price statistics: " + throwable.getMessage())
                            .build();
                });
    }
}