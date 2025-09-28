package by.losik.resource;

import by.losik.entity.Customer;
import by.losik.entity.Invoice;
import by.losik.service.InvoiceService;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
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

@Path("/api/invoices")
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
public class InvoiceResource {

    @Inject
    InvoiceService invoiceService;

    @GET
    public Uni<Response> getAllInvoices(
            @QueryParam("sort") String sortField,
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        log.info("Getting all invoices, page: {}, size: {}", page, size);

        if (page < 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Page index cannot be negative")
                    .build());
        }

        if (size <= 0 || size > 100) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Page size must be between 1 and 100")
                    .build());
        }

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.Descending
                : Sort.Direction.Ascending;

        Sort sort = sortField != null
                ? Sort.by(sortField, sortDirection)
                : Sort.by("invoiceDate", sortDirection);

        return invoiceService.findPaginatedSorted(page, size, sort)
                .onItem().transform(invoices -> Response.ok(invoices).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting all invoices", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving invoices: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getInvoiceById(@PathParam("id") Long id) {
        log.info("Getting invoice by id: {}", id);

        if (id == null || id <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid invoice ID")
                    .build());
        }

        return invoiceService.findById(id)
                .onItem().transform(invoice -> {
                    if (invoice == null) {
                        log.warn("Invoice with id {} not found", id);
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                    return Response.ok(invoice).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting invoice by id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving invoice: " + throwable.getMessage())
                            .build();
                });
    }

    @POST
    public Uni<Response> createInvoice(Invoice invoice) {
        log.info("Creating new invoice");

        if (invoice == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invoice is required")
                    .build());
        }

        if (invoice.getCustomer() == null || invoice.getCustomer().getId() == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Customer is required")
                    .build());
        }

        if (invoice.getSettlement() == null || invoice.getSettlement().getId() == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Settlement is required")
                    .build());
        }

        if (invoice.getInvoiceDate() == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invoice date is required")
                    .build());
        }

        if (invoice.getTotalAmount() == null || invoice.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Total amount must be greater than 0")
                    .build());
        }

        if (invoice.getEnterprise() == null || invoice.getEnterprise().trim().isEmpty()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Enterprise name is required")
                    .build());
        }

        return invoiceService.save(invoice)
                .onItem().transform(savedInvoice ->
                        Response.status(Response.Status.CREATED).entity(savedInvoice).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error creating invoice", throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error creating invoice: " + throwable.getMessage())
                            .build();
                });
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> updateInvoice(@PathParam("id") Long id, Invoice invoice) {
        log.info("Updating invoice with id: {}", id);

        if (id == null || id <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid invoice ID")
                    .build());
        }

        if (invoice == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invoice is required")
                    .build());
        }

        if (invoice.getCustomer() == null || invoice.getCustomer().getId() == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Customer is required")
                    .build());
        }

        if (invoice.getSettlement() == null || invoice.getSettlement().getId() == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Settlement is required")
                    .build());
        }

        invoice.setId(id);
        return invoiceService.update(invoice)
                .onItem().transform(updatedInvoice -> Response.ok(updatedInvoice).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating invoice with id: {}", id, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error updating invoice: " + throwable.getMessage())
                            .build();
                });
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> deleteInvoice(@PathParam("id") Long id) {
        log.info("Deleting invoice with id: {}", id);

        if (id == null || id <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid invoice ID")
                    .build());
        }

        return invoiceService.deleteById(id)
                .onItem().transform(deleted -> {
                    if (deleted) {
                        return Response.noContent().build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error deleting invoice with id: {}", id, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error deleting invoice: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/customer/{customerId}")
    public Uni<Response> getInvoicesByCustomerId(@PathParam("customerId") Long customerId) {
        log.info("Getting invoices by customer id: {}", customerId);

        if (customerId == null || customerId <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid customer ID")
                    .build());
        }

        return invoiceService.findByCustomerId(customerId)
                .onItem().transform(invoices -> Response.ok(invoices).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting invoices by customer id: {}", customerId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving invoices: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/customer/{customerId}/paginated")
    public Uni<Response> getInvoicesByCustomerIdPaginated(
            @PathParam("customerId") Long customerId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        log.info("Getting invoices by customer id: {}, page: {}, size: {}", customerId, page, size);

        if (customerId == null || customerId <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid customer ID")
                    .build());
        }

        if (page < 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Page index cannot be negative")
                    .build());
        }

        if (size <= 0 || size > 100) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Page size must be between 1 and 100")
                    .build());
        }

        Customer customer = new Customer();
        customer.setId(customerId);
        return invoiceService.findByCustomerPaginated(customer, page, size)
                .onItem().transform(invoices -> Response.ok(invoices).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting invoices by customer id paginated: {}", customerId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving invoices: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/settlement/{settlementId}")
    public Uni<Response> getInvoicesBySettlementId(@PathParam("settlementId") Long settlementId) {
        log.info("Getting invoices by settlement id: {}", settlementId);

        if (settlementId == null || settlementId <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid settlement ID")
                    .build());
        }

        return invoiceService.findBySettlementId(settlementId)
                .onItem().transform(invoices -> Response.ok(invoices).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting invoices by settlement id: {}", settlementId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving invoices: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/date/{date}")
    public Uni<Response> getInvoicesByDate(@PathParam("date") Date date) {
        log.info("Getting invoices by date: {}", date);

        if (date == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Date is required")
                    .build());
        }

        return invoiceService.findByDate(date)
                .onItem().transform(invoices -> Response.ok(invoices).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting invoices by date: {}", date, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving invoices: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/date-range")
    public Uni<Response> getInvoicesByDateRange(
            @QueryParam("start") Date startDate,
            @QueryParam("end") Date endDate) {

        log.info("Getting invoices by date range: {} to {}", startDate, endDate);

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

        return invoiceService.findByDateRange(startDate, endDate)
                .onItem().transform(invoices -> Response.ok(invoices).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting invoices by date range: {} to {}", startDate, endDate, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving invoices: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/date-range/paginated")
    public Uni<Response> getInvoicesByDateRangePaginated(
            @QueryParam("start") Date startDate,
            @QueryParam("end") Date endDate,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        log.info("Getting invoices by date range paginated: {} to {}, page: {}, size: {}", startDate, endDate, page, size);

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

        if (page < 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Page index cannot be negative")
                    .build());
        }

        if (size <= 0 || size > 100) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Page size must be between 1 and 100")
                    .build());
        }

        return invoiceService.findByDateRangePaginated(startDate, endDate, page, size)
                .onItem().transform(invoices -> Response.ok(invoices).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting invoices by date range paginated: {} to {}", startDate, endDate, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving invoices: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/enterprise/{enterprise}")
    public Uni<Response> getInvoicesByEnterprise(@PathParam("enterprise") String enterprise) {
        log.info("Getting invoices by enterprise: {}", enterprise);

        if (enterprise == null || enterprise.trim().isEmpty()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Enterprise name is required")
                    .build());
        }

        return invoiceService.findByEnterprise(enterprise)
                .onItem().transform(invoices -> Response.ok(invoices).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting invoices by enterprise: {}", enterprise, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving invoices: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/amount/greater-than/{minAmount}")
    public Uni<Response> getInvoicesByAmountGreaterThan(@PathParam("minAmount") BigDecimal minAmount) {
        log.info("Getting invoices with amount greater than: {}", minAmount);

        if (minAmount == null || minAmount.compareTo(BigDecimal.ZERO) < 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Minimum amount must be non-negative")
                    .build());
        }

        return invoiceService.findByAmountGreaterThan(minAmount)
                .onItem().transform(invoices -> Response.ok(invoices).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting invoices with amount greater than: {}", minAmount, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving invoices: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/amount/between")
    public Uni<Response> getInvoicesByAmountBetween(
            @QueryParam("min") BigDecimal minAmount,
            @QueryParam("max") BigDecimal maxAmount) {

        log.info("Getting invoices with amount between: {} and {}", minAmount, maxAmount);

        if (minAmount == null || maxAmount == null || minAmount.compareTo(BigDecimal.ZERO) < 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Minimum amount must be non-negative")
                    .build());
        }

        if (maxAmount.compareTo(minAmount) <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Maximum amount must be greater than minimum amount")
                    .build());
        }

        return invoiceService.findByAmountBetween(minAmount, maxAmount)
                .onItem().transform(invoices -> Response.ok(invoices).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting invoices with amount between: {} and {}", minAmount, maxAmount, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving invoices: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/top-invoices")
    public Uni<Response> getTopInvoicesByAmount(@QueryParam("limit") @DefaultValue("10") int limit) {
        log.info("Getting top {} invoices by amount", limit);

        if (limit <= 0 || limit > 100) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Limit must be between 1 and 100")
                    .build());
        }

        return invoiceService.findTopInvoicesByAmount(limit)
                .onItem().transform(invoices -> Response.ok(invoices).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting top invoices by amount", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving top invoices: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/recent")
    public Uni<Response> getRecentInvoices(@QueryParam("days") @DefaultValue("30") int days) {
        log.info("Getting recent invoices from last {} days", days);

        if (days <= 0 || days > 365) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Days must be between 1 and 365")
                    .build());
        }

        return invoiceService.findRecentInvoices(days)
                .onItem().transform(invoices -> Response.ok(invoices).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting recent invoices", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving recent invoices: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/without-items")
    public Uni<Response> getInvoicesWithoutItems() {
        log.info("Getting invoices without items");

        return invoiceService.findInvoicesWithoutItems()
                .onItem().transform(invoices -> Response.ok(invoices).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting invoices without items", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving invoices without items: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/stats/total-revenue/customer/{customerId}")
    public Uni<Response> getTotalRevenueByCustomer(@PathParam("customerId") Long customerId) {
        log.info("Calculating total revenue by customer: {}", customerId);

        if (customerId == null || customerId <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid customer ID")
                    .build());
        }

        Customer customer = new Customer();
        customer.setId(customerId);
        return invoiceService.getTotalRevenueByCustomer(customer)
                .onItem().transform(revenue -> Response.ok(revenue).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error calculating total revenue by customer: {}", customerId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error calculating total revenue: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/stats/average-amount")
    public Uni<Response> getAverageInvoiceAmount() {
        log.info("Calculating average invoice amount");

        return invoiceService.getAverageInvoiceAmount()
                .onItem().transform(average -> Response.ok(average).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error calculating average invoice amount", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error calculating average amount: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/stats/max-amount")
    public Uni<Response> getMaxInvoiceAmount() {
        log.info("Finding maximum invoice amount");

        return invoiceService.getMaxInvoiceAmount()
                .onItem().transform(maxAmount -> Response.ok(maxAmount).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error finding maximum invoice amount", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error finding maximum amount: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/stats/min-amount")
    public Uni<Response> getMinInvoiceAmount() {
        log.info("Finding minimum invoice amount");

        return invoiceService.getMinInvoiceAmount()
                .onItem().transform(minAmount -> Response.ok(minAmount).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error finding minimum invoice amount", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error finding minimum amount: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/exists/customer-date")
    public Uni<Response> existsByCustomerAndDate(
            @QueryParam("customerId") Long customerId,
            @QueryParam("date") Date date) {

        log.info("Checking if invoice exists by customer: {} and date: {}", customerId, date);

        if (customerId == null || customerId <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid customer ID")
                    .build());
        }

        if (date == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Date is required")
                    .build());
        }

        Customer customer = new Customer();
        customer.setId(customerId);
        return invoiceService.existsByCustomerAndDate(customer, date)
                .onItem().transform(exists -> Response.ok(exists).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error checking invoice existence by customer: {} and date: {}", customerId, date, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error checking invoice existence: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/customer/{customerId}/count")
    public Uni<Response> countByCustomerId(@PathParam("customerId") Long customerId) {
        log.info("Counting invoices for customer id: {}", customerId);

        if (customerId == null || customerId <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid customer ID")
                    .build());
        }

        Customer customer = new Customer();
        customer.setId(customerId);
        return invoiceService.countByCustomer(customer)
                .onItem().transform(count -> Response.ok(count).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error counting invoices for customer id: {}", customerId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error counting invoices: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/date-range/count")
    public Uni<Response> countByDateRange(
            @QueryParam("start") Date startDate,
            @QueryParam("end") Date endDate) {

        log.info("Counting invoices by date range: {} to {}", startDate, endDate);

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

        return invoiceService.countByDateRange(startDate, endDate)
                .onItem().transform(count -> Response.ok(count).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error counting invoices by date range: {} to {}", startDate, endDate, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error counting invoices: " + throwable.getMessage())
                            .build();
                });
    }

    @PUT
    @Path("/{id}/total-amount")
    public Uni<Response> updateTotalAmount(
            @PathParam("id") Long id,
            @QueryParam("amount") BigDecimal newAmount) {

        log.info("Updating total amount for invoice id: {}, new amount: {}", id, newAmount);

        if (id == null || id <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid invoice ID")
                    .build());
        }

        if (newAmount == null || newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Amount must be greater than 0")
                    .build());
        }

        return invoiceService.updateTotalAmount(id, newAmount)
                .onItem().transform(updatedCount -> {
                    if (updatedCount > 0) {
                        return Response.ok().entity("Total amount updated successfully").build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating total amount for invoice id: {}", id, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error updating total amount: " + throwable.getMessage())
                            .build();
                });
    }

    @PUT
    @Path("/{id}/enterprise")
    public Uni<Response> updateEnterprise(
            @PathParam("id") Long id,
            @QueryParam("enterprise") String newEnterprise) {

        log.info("Updating enterprise for invoice id: {}, new enterprise: {}", id, newEnterprise);

        if (id == null || id <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid invoice ID")
                    .build());
        }

        if (newEnterprise == null || newEnterprise.trim().isEmpty()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Enterprise name is required")
                    .build());
        }

        return invoiceService.updateEnterprise(id, newEnterprise)
                .onItem().transform(updatedCount -> {
                    if (updatedCount > 0) {
                        return Response.ok().entity("Enterprise updated successfully").build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating enterprise for invoice id: {}", id, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error updating enterprise: " + throwable.getMessage())
                            .build();
                });
    }

    @DELETE
    @Path("/customer/{customerId}")
    public Uni<Response> deleteInvoicesByCustomerId(@PathParam("customerId") Long customerId) {
        log.info("Deleting all invoices for customer id: {}", customerId);

        if (customerId == null || customerId <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid customer ID")
                    .build());
        }

        Customer customer = new Customer();
        customer.setId(customerId);
        return invoiceService.deleteByCustomer(customer)
                .onItem().transform(count -> {
                    log.info("Deleted {} invoices for customer id: {}", count, customerId);
                    return Response.ok().entity("Deleted " + count + " invoices").build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error deleting invoices for customer id: {}", customerId, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error deleting invoices: " + throwable.getMessage())
                            .build();
                });
    }

    @DELETE
    @Path("/date-range")
    public Uni<Response> deleteInvoicesByDateRange(
            @QueryParam("start") Date startDate,
            @QueryParam("end") Date endDate) {

        log.info("Deleting invoices by date range: {} to {}", startDate, endDate);

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

        return invoiceService.deleteByDateRange(startDate, endDate)
                .onItem().transform(count -> {
                    log.info("Deleted {} invoices for date range: {} to {}", count, startDate, endDate);
                    return Response.ok().entity("Deleted " + count + " invoices").build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error deleting invoices by date range: {} to {}", startDate, endDate, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error deleting invoices: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/stats/customer/{customerId}/page-count")
    public Uni<Response> getPageCountByCustomer(
            @PathParam("customerId") Long customerId,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize) {

        log.info("Calculating page count for customer id: {}, page size: {}", customerId, pageSize);

        if (customerId == null || customerId <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid customer ID")
                    .build());
        }

        Customer customer = new Customer();
        customer.setId(customerId);
        return invoiceService.getPageCountByCustomer(customer, pageSize)
                .onItem().transform(pageCount -> Response.ok(pageCount).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error calculating page count for customer id: {}", customerId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error calculating page count: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/stats/date-range/page-count")
    public Uni<Response> getPageCountByDateRange(
            @QueryParam("start") Date startDate,
            @QueryParam("end") Date endDate,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize) {

        log.info("Calculating page count for date range: {} to {}, page size: {}", startDate, endDate, pageSize);

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

        return invoiceService.getPageCountByDateRange(startDate, endDate, pageSize)
                .onItem().transform(pageCount -> Response.ok(pageCount).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error calculating page count for date range: {} to {}", startDate, endDate, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error calculating page count: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/total/count")
    public Uni<Response> getTotalCount() {
        log.info("Getting total invoices count");

        return invoiceService.countAll()
                .onItem().transform(count -> Response.ok(count).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting total invoices count", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error counting invoices: " + throwable.getMessage())
                            .build();
                });
    }
}