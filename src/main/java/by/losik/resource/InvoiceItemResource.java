package by.losik.resource;

import by.losik.entity.Invoice;
import by.losik.entity.InvoiceItem;
import by.losik.service.InvoiceItemService;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;

@Path("/api/invoice-items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
@Authenticated
public class InvoiceItemResource {

    @Inject
    InvoiceItemService invoiceItemService;

    @GET
    public Uni<Response> getAllInvoiceItems(
            @QueryParam("sort") String sortField,
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        log.info("Getting all invoice items, page: {}, size: {}", page, size);

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
                            : Sort.by("id", sortDirection);

                    return invoiceItemService.findPaginatedSorted(page, size, sort)
                            .onItem().transform(items -> Response.ok(items).build());
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting all invoice items", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving invoice items: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getInvoiceItemById(@PathParam("id") Long id) {
        log.info("Getting invoice item by id: {}", id);

        return Uni.createFrom().item(id)
                .onItem().transform(i -> i == null || i <= 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid invoice item ID")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        invoiceItemService.findById(id)
                                .onItem().transform(item -> {
                                    if (item == null) {
                                        log.warn("Invoice item with id {} not found", id);
                                        return Response.status(Response.Status.NOT_FOUND).build();
                                    }
                                    return Response.ok(item).build();
                                })
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting invoice item by id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving invoice item: " + throwable.getMessage())
                            .build();
                });
    }

    @POST
    public Uni<Response> createInvoiceItem(InvoiceItem invoiceItem) {
        log.info("Creating new invoice item for invoice: {}, product: {}",
                invoiceItem != null && invoiceItem.invoice() != null ? invoiceItem.invoice().id() : "null",
                invoiceItem != null && invoiceItem.product() != null ? invoiceItem.product().id() : "null");

        return Uni.createFrom().item(invoiceItem)
                .onItem().transform(item -> item == null || item.invoice() == null || item.product() == null)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invoice and product are required")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        Uni.createFrom().item(invoiceItem.quantity() == null || invoiceItem.quantity().compareTo(BigInteger.ZERO) <= 0)
                                .onItem().transform(isInvalid -> {
                                    if (isInvalid) {
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Quantity must be greater than 0")
                                                .build();
                                    }
                                    return null;
                                })
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        Uni.createFrom().item(invoiceItem.price() == null || invoiceItem.price().compareTo(BigDecimal.ZERO) <= 0)
                                .onItem().transform(isInvalid -> {
                                    if (isInvalid) {
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Price must be greater than 0")
                                                .build();
                                    }
                                    return null;
                                })
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        invoiceItemService.save(invoiceItem)
                                .onItem().transform(savedItem ->
                                        Response.status(Response.Status.CREATED)
                                                .entity(savedItem)
                                                .build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error creating invoice item", throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error creating invoice item: " + throwable.getMessage())
                            .build();
                });
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> updateInvoiceItem(@PathParam("id") Long id, InvoiceItem invoiceItem) {
        log.info("Updating invoice item with id: {}", id);

        return Uni.createFrom().item(id)
                .onItem().transform(i -> i == null || i <= 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid invoice item ID")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        Uni.createFrom().item(invoiceItem)
                                .onItem().transform(item -> item == null || item.invoice() == null || item.product() == null)
                                .onItem().transform(isInvalid -> {
                                    if (isInvalid) {
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Invoice and product are required")
                                                .build();
                                    }
                                    return null;
                                })
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() -> {
                    invoiceItem.id(id);
                    return invoiceItemService.update(invoiceItem)
                            .onItem().transform(updatedItem -> Response.ok(updatedItem).build());
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating invoice item with id: {}", id, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error updating invoice item: " + throwable.getMessage())
                            .build();
                });
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> deleteInvoiceItem(@PathParam("id") Long id) {
        log.info("Deleting invoice item with id: {}", id);

        return Uni.createFrom().item(id)
                .onItem().transform(i -> i == null || i <= 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid invoice item ID")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        invoiceItemService.deleteById(id)
                                .onItem().transform(deleted -> {
                                    if (deleted) {
                                        return Response.noContent().build();
                                    } else {
                                        return Response.status(Response.Status.NOT_FOUND).build();
                                    }
                                })
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error deleting invoice item with id: {}", id, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error deleting invoice item: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/invoice/{invoiceId}")
    public Uni<Response> getItemsByInvoiceId(@PathParam("invoiceId") Long invoiceId) {
        log.info("Getting invoice items by invoice id: {}", invoiceId);

        return Uni.createFrom().item(invoiceId)
                .onItem().transform(id -> id == null || id <= 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid invoice ID")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        invoiceItemService.findByInvoiceId(invoiceId)
                                .onItem().transform(items -> Response.ok(items).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting invoice items by invoice id: {}", invoiceId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving invoice items: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/invoice/{invoiceId}/paginated")
    public Uni<Response> getItemsByInvoiceIdPaginated(
            @PathParam("invoiceId") Long invoiceId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        log.info("Getting invoice items by invoice id: {}, page: {}, size: {}", invoiceId, page, size);

        return Uni.createFrom().item(invoiceId)
                .onItem().transform(id -> id == null || id <= 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid invoice ID")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        invoiceItemService.findByInvoiceIdPaginated(invoiceId, page, size)
                                .onItem().transform(items -> Response.ok(items).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting invoice items by invoice id paginated: {}", invoiceId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving invoice items: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/product/{productId}")
    public Uni<Response> getItemsByProductId(@PathParam("productId") Long productId) {
        log.info("Getting invoice items by product id: {}", productId);

        return Uni.createFrom().item(productId)
                .onItem().transform(id -> id == null || id <= 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid product ID")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        invoiceItemService.findByProductId(productId)
                                .onItem().transform(items -> Response.ok(items).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting invoice items by product id: {}", productId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving invoice items: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/quantity/greater-than/{minQuantity}")
    public Uni<Response> getItemsByQuantityGreaterThan(@PathParam("minQuantity") BigInteger minQuantity) {
        log.info("Getting invoice items with quantity greater than: {}", minQuantity);

        return Uni.createFrom().item(minQuantity)
                .onItem().transform(qty -> qty == null || qty.compareTo(BigInteger.ZERO) < 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Minimum quantity must be non-negative")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        invoiceItemService.findByQuantityGreaterThan(minQuantity)
                                .onItem().transform(items -> Response.ok(items).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting invoice items with quantity greater than: {}", minQuantity, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving invoice items: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/quantity/between")
    public Uni<Response> getItemsByQuantityBetween(
            @QueryParam("min") BigInteger minQuantity,
            @QueryParam("max") BigInteger maxQuantity) {

        log.info("Getting invoice items with quantity between: {} and {}", minQuantity, maxQuantity);

        return Uni.createFrom().item(minQuantity)
                .onItem().transform(min -> min == null || maxQuantity == null || min.compareTo(BigInteger.ZERO) < 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Minimum quantity must be non-negative")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        Uni.createFrom().item(maxQuantity.compareTo(minQuantity) <= 0)
                                .onItem().transform(isInvalid -> {
                                    if (isInvalid) {
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Maximum quantity must be greater than minimum quantity")
                                                .build();
                                    }
                                    return null;
                                })
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        invoiceItemService.findByQuantityBetween(minQuantity, maxQuantity)
                                .onItem().transform(items -> Response.ok(items).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting invoice items with quantity between: {} and {}", minQuantity, maxQuantity, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving invoice items: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/price/greater-than/{minPrice}")
    public Uni<Response> getItemsByPriceGreaterThan(@PathParam("minPrice") BigDecimal minPrice) {
        log.info("Getting invoice items with price greater than: {}", minPrice);

        return Uni.createFrom().item(minPrice)
                .onItem().transform(price -> price == null || price.compareTo(BigDecimal.ZERO) < 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Minimum price must be non-negative")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        invoiceItemService.findByPriceGreaterThan(minPrice)
                                .onItem().transform(items -> Response.ok(items).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting invoice items with price greater than: {}", minPrice, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving invoice items: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/price/between")
    public Uni<Response> getItemsByPriceBetween(
            @QueryParam("min") BigDecimal minPrice,
            @QueryParam("max") BigDecimal maxPrice) {

        log.info("Getting invoice items with price between: {} and {}", minPrice, maxPrice);

        return Uni.createFrom().item(minPrice)
                .onItem().transform(min -> min == null || maxPrice == null || min.compareTo(BigDecimal.ZERO) < 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Minimum price must be non-negative")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        Uni.createFrom().item(maxPrice.compareTo(minPrice) <= 0)
                                .onItem().transform(isInvalid -> {
                                    if (isInvalid) {
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Maximum price must be greater than minimum price")
                                                .build();
                                    }
                                    return null;
                                })
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        invoiceItemService.findByPriceBetween(minPrice, maxPrice)
                                .onItem().transform(items -> Response.ok(items).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting invoice items with price between: {} and {}", minPrice, maxPrice, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving invoice items: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/top-selling")
    public Uni<Response> getTopSellingItems(@QueryParam("limit") @DefaultValue("10") int limit) {
        log.info("Getting top {} selling items", limit);

        return Uni.createFrom().item(limit)
                .onItem().transform(l -> l <= 0 || l > 100)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Limit must be between 1 and 100")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        invoiceItemService.findTopSellingItems(limit)
                                .onItem().transform(items -> Response.ok(items).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting top selling items", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving top selling items: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/high-value")
    public Uni<Response> getHighValueItems(@QueryParam("minValue") @DefaultValue("1000") BigDecimal minValue) {
        log.info("Getting high value items with min value: {}", minValue);

        return Uni.createFrom().item(minValue)
                .onItem().transform(value -> value == null || value.compareTo(BigDecimal.ZERO) < 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Minimum value must be non-negative")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        invoiceItemService.findHighValueItems(minValue)
                                .onItem().transform(items -> Response.ok(items).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting high value items", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving high value items: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/total-revenue")
    public Uni<Response> getTotalRevenue() {
        log.info("Calculating total revenue");

        return invoiceItemService.getTotalRevenue()
                .onItem().transform(revenue -> Response.ok(revenue).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error calculating total revenue", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error calculating total revenue: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/exists")
    public Uni<Response> existsByInvoiceAndProduct(
            @QueryParam("invoiceId") Long invoiceId,
            @QueryParam("productId") Long productId) {

        log.info("Checking if invoice item exists by invoiceId: {} and productId: {}", invoiceId, productId);

        return Uni.createFrom().item(invoiceId)
                .onItem().transform(invId -> invId == null || invId <= 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid invoice ID")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        Uni.createFrom().item(productId)
                                .onItem().transform(prodId -> prodId == null || prodId <= 0)
                                .onItem().transform(isInvalid -> {
                                    if (isInvalid) {
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Invalid product ID")
                                                .build();
                                    }
                                    return null;
                                })
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        invoiceItemService.existsByInvoiceIdAndProductId(invoiceId, productId)
                                .onItem().transform(exists -> Response.ok(exists).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error checking invoice item existence by invoiceId: {} and productId: {}", invoiceId, productId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error checking invoice item existence: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/invoice/{invoiceId}/count")
    public Uni<Response> countByInvoiceId(@PathParam("invoiceId") Long invoiceId) {
        log.info("Counting invoice items for invoice id: {}", invoiceId);

        return Uni.createFrom().item(invoiceId)
                .onItem().transform(id -> id == null || id <= 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid invoice ID")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() -> {
                    Invoice invoice = new Invoice().id(invoiceId);
                    return invoiceItemService.countByInvoice(invoice)
                            .onItem().transform(count -> Response.ok(count).build());
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error counting invoice items for invoice id: {}", invoiceId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error counting invoice items: " + throwable.getMessage())
                            .build();
                });
    }

    @PUT
    @Path("/{id}/quantity")
    public Uni<Response> updateQuantity(
            @PathParam("id") Long id,
            @QueryParam("quantity") BigInteger newQuantity) {

        log.info("Updating quantity for invoice item id: {}, new quantity: {}", id, newQuantity);

        return Uni.createFrom().item(id)
                .onItem().transform(i -> i == null || i <= 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid invoice item ID")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        Uni.createFrom().item(newQuantity)
                                .onItem().transform(qty -> qty == null || qty.compareTo(BigInteger.ZERO) <= 0)
                                .onItem().transform(isInvalid -> {
                                    if (isInvalid) {
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Quantity must be greater than 0")
                                                .build();
                                    }
                                    return null;
                                })
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        invoiceItemService.updateQuantity(id, newQuantity)
                                .onItem().transform(updatedCount -> {
                                    if (updatedCount > 0) {
                                        return Response.ok().entity("Quantity updated successfully").build();
                                    } else {
                                        return Response.status(Response.Status.NOT_FOUND).build();
                                    }
                                })
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating quantity for invoice item id: {}", id, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error updating quantity: " + throwable.getMessage())
                            .build();
                });
    }

    @PUT
    @Path("/{id}/price")
    public Uni<Response> updatePrice(
            @PathParam("id") Long id,
            @QueryParam("price") BigDecimal newPrice) {

        log.info("Updating price for invoice item id: {}, new price: {}", id, newPrice);

        return Uni.createFrom().item(id)
                .onItem().transform(i -> i == null || i <= 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid invoice item ID")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        Uni.createFrom().item(newPrice)
                                .onItem().transform(price -> price == null || price.compareTo(BigDecimal.ZERO) <= 0)
                                .onItem().transform(isInvalid -> {
                                    if (isInvalid) {
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Price must be greater than 0")
                                                .build();
                                    }
                                    return null;
                                })
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        invoiceItemService.updatePrice(id, newPrice)
                                .onItem().transform(updatedCount -> {
                                    if (updatedCount > 0) {
                                        return Response.ok().entity("Price updated successfully").build();
                                    } else {
                                        return Response.status(Response.Status.NOT_FOUND).build();
                                    }
                                })
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating price for invoice item id: {}", id, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error updating price: " + throwable.getMessage())
                            .build();
                });
    }

    @PUT
    @Path("/{id}/quantity-and-price")
    public Uni<Response> updateQuantityAndPrice(
            @PathParam("id") Long id,
            @QueryParam("quantity") BigInteger newQuantity,
            @QueryParam("price") BigDecimal newPrice) {

        log.info("Updating quantity and price for invoice item id: {}, quantity: {}, price: {}", id, newQuantity, newPrice);

        return Uni.createFrom().item(id)
                .onItem().transform(i -> i == null || i <= 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid invoice item ID")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        Uni.createFrom().item(newQuantity)
                                .onItem().transform(qty -> qty == null || qty.compareTo(BigInteger.ZERO) <= 0)
                                .onItem().transform(isInvalid -> {
                                    if (isInvalid) {
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Quantity must be greater than 0")
                                                .build();
                                    }
                                    return null;
                                })
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        Uni.createFrom().item(newPrice)
                                .onItem().transform(price -> price == null || price.compareTo(BigDecimal.ZERO) <= 0)
                                .onItem().transform(isInvalid -> {
                                    if (isInvalid) {
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Price must be greater than 0")
                                                .build();
                                    }
                                    return null;
                                })
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        invoiceItemService.updateQuantityAndPrice(id, newQuantity, newPrice)
                                .onItem().transform(updatedCount -> {
                                    if (updatedCount > 0) {
                                        return Response.ok().entity("Quantity and price updated successfully").build();
                                    } else {
                                        return Response.status(Response.Status.NOT_FOUND).build();
                                    }
                                })
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating quantity and price for invoice item id: {}", id, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error updating quantity and price: " + throwable.getMessage())
                            .build();
                });
    }

    @DELETE
    @Path("/invoice/{invoiceId}")
    public Uni<Response> deleteItemsByInvoiceId(@PathParam("invoiceId") Long invoiceId) {
        log.info("Deleting all invoice items for invoice id: {}", invoiceId);

        return Uni.createFrom().item(invoiceId)
                .onItem().transform(id -> id == null || id <= 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid invoice ID")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        invoiceItemService.deleteByInvoiceId(invoiceId)
                                .onItem().transform(count -> {
                                    log.info("Deleted {} invoice items for invoice id: {}", count, invoiceId);
                                    return Response.ok().entity("Deleted " + count + " invoice items").build();
                                })
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error deleting invoice items for invoice id: {}", invoiceId, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error deleting invoice items: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/stats/invoice/{invoiceId}/page-count")
    public Uni<Response> getPageCountByInvoice(
            @PathParam("invoiceId") Long invoiceId,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize) {

        log.info("Calculating page count for invoice id: {}, page size: {}", invoiceId, pageSize);

        return Uni.createFrom().item(invoiceId)
                .onItem().transform(id -> id == null || id <= 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid invoice ID")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() -> {
                    Invoice invoice = new Invoice().id(invoiceId);
                    return invoiceItemService.getPageCountByInvoice(invoice, pageSize)
                            .onItem().transform(pageCount -> Response.ok(pageCount).build());
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error calculating page count for invoice id: {}", invoiceId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error calculating page count: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/total/count")
    public Uni<Response> getTotalCount() {
        log.info("Getting total invoice items count");

        return invoiceItemService.countAll()
                .onItem().transform(count -> Response.ok(count).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting total invoice items count", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error counting invoice items: " + throwable.getMessage())
                            .build();
                });
    }
}