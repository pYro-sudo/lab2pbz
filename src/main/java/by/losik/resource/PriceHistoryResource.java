package by.losik.resource;

import by.losik.entity.PriceHistory;
import by.losik.entity.Product;
import by.losik.service.PriceHistoryService;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.sql.Date;

@Resource
@Path("/api/price-history")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
@Authenticated
public class PriceHistoryResource {

    @Inject
    PriceHistoryService priceHistoryService;

    @GET
    public Uni<Response> getAllPriceHistories(
            @QueryParam("sort") @DefaultValue("invoiceDate") String sortField,
            @QueryParam("direction") @DefaultValue("desc") String direction) {
        log.info("Getting all price histories sorted by {} {}", sortField, direction);

        return priceHistoryService.findAllSorted(io.quarkus.panache.common.Sort.by(sortField,
                        io.quarkus.panache.common.Sort.Direction.valueOf(direction.toUpperCase())))
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

        return priceHistoryService.findById(id)
                .onItem().ifNotNull().transform(history -> {
                    log.debug("Found price history with id: {}", id);
                    return Response.ok(history).build();
                })
                .onItem().ifNull().continueWith(() -> {
                    log.warn("Price history with id {} not found", id);
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("Price history not found with id: " + id)
                            .build();
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

        return priceHistoryService.findLatestByProductId(productId)
                .onItem().ifNotNull().transform(history -> {
                    log.debug("Found latest price history for product id: {}", productId);
                    return Response.ok(history).build();
                })
                .onItem().ifNull().continueWith(() -> {
                    log.warn("No price history found for product id: {}", productId);
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("No price history found for product id: " + productId)
                            .build();
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

        return priceHistoryService.findByChangeDateRange(startDate, endDate)
                .onItem().transform(histories -> {
                    log.debug("Found {} price histories for date range: {} to {}",
                            histories.size(), startDate, endDate);
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

        return priceHistoryService.findByPriceBetween(minPrice, maxPrice)
                .onItem().transform(histories -> {
                    log.debug("Found {} price histories for price range: {} to {}",
                            histories.size(), minPrice, maxPrice);
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

        // Создаем временный Product объект с нужным ID для использования в сервисе
        Product tempProduct = new Product().id(productId);

        return priceHistoryService.getCurrentPrice(tempProduct)
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
        log.info("Creating new price history for product: {}",
                priceHistory.productId() != null ? priceHistory.productId().id() : "unknown");

        return priceHistoryService.save(priceHistory)
                .onItem().transform(savedHistory -> {
                    log.info("Successfully created price history with id: {}", savedHistory.id());
                    return Response.status(Response.Status.CREATED)
                            .entity(savedHistory)
                            .build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error creating price history", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error creating price history: " + throwable.getMessage())
                            .build();
                });
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> updatePriceHistory(@PathParam("id") Long id, PriceHistory priceHistory) {
        log.info("Updating price history with id: {}", id);

        // Убеждаемся, что ID в пути совпадает с ID в теле
        if (!id.equals(priceHistory.id())) {
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
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error updating price history: " + throwable.getMessage())
                            .build();
                });
    }

    @PATCH
    @Path("/{id}/price")
    public Uni<Response> updatePrice(@PathParam("id") Long id, BigDecimal newPrice) {
        log.info("Updating price for price history id: {} to {}", id, newPrice);

        return priceHistoryService.updatePrice(id, newPrice)
                .onItem().transform(updatedCount -> {
                    if (updatedCount > 0) {
                        log.info("Successfully updated price for price history id: {}", id);
                        return Response.ok().entity("Price updated successfully").build();
                    } else {
                        log.warn("No price history found with id: {}", id);
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("Price history not found with id: " + id)
                                .build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating price for price history id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error updating price: " + throwable.getMessage())
                            .build();
                });
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> deletePriceHistory(@PathParam("id") Long id) {
        log.info("Deleting price history with id: {}", id);

        return priceHistoryService.deleteById(id)
                .onItem().transform(deleted -> {
                    if (deleted) {
                        log.info("Successfully deleted price history with id: {}", id);
                        return Response.noContent().build();
                    } else {
                        log.warn("Price history with id {} not found for deletion", id);
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("Price history not found with id: " + id)
                                .build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error deleting price history with id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error deleting price history: " + throwable.getMessage())
                            .build();
                });
    }

    @DELETE
    @Path("/product/{productId}")
    public Uni<Response> deletePriceHistoriesByProduct(@PathParam("productId") Long productId) {
        log.info("Deleting price histories for product id: {}", productId);

        // Создаем временный Product объект с нужным ID
        Product tempProduct = new Product().id(productId);

        return priceHistoryService.deleteByProduct(tempProduct)
                .onItem().transform(deletedCount -> {
                    log.info("Successfully deleted {} price histories for product id: {}", deletedCount, productId);
                    return Response.ok().entity("Deleted " + deletedCount + " price histories").build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error deleting price histories for product id: {}", productId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
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

        // Создаем временный Product объект с нужным ID
        Product tempProduct = new Product().id(productId);

        return priceHistoryService.getPriceTrend(tempProduct, limit)
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

        Product tempProduct = new Product().id(productId);

        return Uni.combine().all().unis(
                        priceHistoryService.getMaxPriceByProduct(tempProduct),
                        priceHistoryService.getMinPriceByProduct(tempProduct),
                        priceHistoryService.countPriceChangesByProduct(tempProduct)
                ).asTuple().onItem().transform(tuple -> {
                    PriceHistory maxPrice = tuple.getItem1();
                    PriceHistory minPrice = tuple.getItem2();
                    Long changeCount = tuple.getItem3();

                    log.debug("Price stats for product id {}: {}, {}, {}", productId, maxPrice.price(), minPrice.price(), changeCount);
                    return Response.ok(new JsonObject()
                            .put("min", minPrice.price())
                            .put("max", maxPrice.price())
                            .put("changeCount", changeCount)
                    ).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting price statistics for product id: {}", productId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving price statistics: " + throwable.getMessage())
                            .build();
                });
    }
}