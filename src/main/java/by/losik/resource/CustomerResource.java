package by.losik.resource;

import by.losik.entity.Customer;
import by.losik.service.CustomerService;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Path("/api/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
@Authenticated
public class CustomerResource {

    @Inject
    CustomerService customerService;

    @GET
    public Uni<Response> getAllCustomers(
            @QueryParam("sort") String sortField,
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        log.info("Getting all customers, page: {}, size: {}, sort: {}", page, size, sortField);

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

                    return customerService.findPaginatedSorted(page, size, sort)
                            .onItem().transform(customers -> Response.ok(customers).build());
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting all customers", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving customers: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getCustomerById(@PathParam("id") Long id) {
        log.info("Getting customer by id: {}", id);

        return Uni.createFrom().item(id)
                .onItem().transform(i -> i == null || i <= 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid customer ID")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        customerService.findById(id)
                                .onItem().transform(customer -> {
                                    if (customer == null) {
                                        log.warn("Customer with id {} not found", id);
                                        return Response.status(Response.Status.NOT_FOUND).build();
                                    }
                                    return Response.ok(customer).build();
                                })
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting customer by id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving customer: " + throwable.getMessage())
                            .build();
                });
    }

    @POST
    public Uni<Response> createCustomer(Customer customer) {
        log.info("Creating new customer: {}", customer != null ? customer.getName() : "null");

        return Uni.createFrom().item(customer)
                .onItem().transform(c -> c == null || c.getName() == null || c.getName().trim().isEmpty())
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Customer name is required")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        {
                            assert customer != null;
                            return Uni.createFrom().item(customer.getName().length() > 200)
                                    .onItem().transform(isInvalid -> {
                                        if (isInvalid) {
                                            return Response.status(Response.Status.BAD_REQUEST)
                                                    .entity("Customer name must not exceed 200 characters")
                                                    .build();
                                        }
                                        return null;
                                    });
                        }
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        {
                            assert customer != null;
                            return customerService.save(customer)
                                    .onItem().transform(savedCustomer ->
                                            Response.status(Response.Status.CREATED)
                                                    .entity(savedCustomer)
                                                    .build());
                        }
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error creating customer: {}", customer != null ? customer.getName() : "null", throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error creating customer: " + throwable.getMessage())
                            .build();
                });
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> updateCustomer(@PathParam("id") Long id, Customer customer) {
        log.info("Updating customer with id: {}", id);

        return Uni.createFrom().item(id)
                .onItem().transform(i -> i == null || i <= 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid customer ID")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        Uni.createFrom().item(customer)
                                .onItem().transform(c -> c == null || c.getName() == null || c.getName().trim().isEmpty())
                                .onItem().transform(isInvalid -> {
                                    if (isInvalid) {
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Customer name is required")
                                                .build();
                                    }
                                    return null;
                                })
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        Uni.createFrom().item(customer.getName().length() > 200)
                                .onItem().transform(isInvalid -> {
                                    if (isInvalid) {
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Customer name must not exceed 200 characters")
                                                .build();
                                    }
                                    return null;
                                })
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() -> {
                    customer.setId(id);
                    return customerService.update(customer)
                            .onItem().transform(updatedCustomer -> Response.ok(updatedCustomer).build());
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating customer with id: {}", id, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error updating customer: " + throwable.getMessage())
                            .build();
                });
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> deleteCustomer(@PathParam("id") Long id) {
        log.info("Deleting customer with id: {}", id);

        return Uni.createFrom().item(id)
                .onItem().transform(i -> i == null || i <= 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid customer ID")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        customerService.deleteById(id)
                                .onItem().transform(deleted -> {
                                    if (deleted) {
                                        return Response.noContent().build();
                                    } else {
                                        return Response.status(Response.Status.NOT_FOUND).build();
                                    }
                                })
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error deleting customer with id: {}", id, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error deleting customer: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/legal-status/{isLegalEntity}")
    public Uni<Response> getCustomersByLegalStatus(
            @PathParam("isLegalEntity") Boolean isLegalEntity,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        log.info("Getting customers by legal status: {}, page: {}, size: {}", isLegalEntity, page, size);

        return Uni.createFrom().item(isLegalEntity)
                .onItem().transform(Objects::isNull)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Legal status is required")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        customerService.findByLegalEntityPaginated(isLegalEntity, page, size)
                                .onItem().transform(customers -> Response.ok(customers).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting customers by legal status: {}", isLegalEntity, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving customers: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/bank/{bankName}")
    public Uni<Response> getCustomersByBankName(@PathParam("bankName") String bankName) {
        log.info("Getting customers by bank name: {}", bankName);

        return Uni.createFrom().item(bankName)
                .onItem().transform(name -> name == null || name.trim().isEmpty())
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Bank name is required")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        customerService.findByBankName(bankName)
                                .onItem().transform(customers -> Response.ok(customers).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting customers by bank name: {}", bankName, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving customers: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/document")
    public Uni<Response> getCustomersByDocument(
            @QueryParam("series") String series,
            @QueryParam("number") String number) {

        log.info("Getting customers by document: series={}, number={}", series, number);

        return Uni.createFrom().item(series)
                .onItem().transform(s -> s == null || s.trim().isEmpty())
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Document series is required")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        Uni.createFrom().item(number)
                                .onItem().transform(n -> n == null || n.trim().isEmpty())
                                .onItem().transform(isInvalid -> {
                                    if (isInvalid) {
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Document number is required")
                                                .build();
                                    }
                                    return null;
                                })
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        customerService.findByDocument(series, number)
                                .onItem().transform(customers -> Response.ok(customers).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting customers by document: series={}, number={}", series, number, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving customers: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/legal-entities/with-bank")
    public Uni<Response> getLegalEntitiesWithBankAccounts() {
        log.info("Getting legal entities with bank accounts");

        return customerService.findLegalEntitiesWithBankAccounts()
                .onItem().transform(customers -> Response.ok(customers).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting legal entities with bank accounts", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving legal entities: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/individuals/with-docs")
    public Uni<Response> getIndividualsWithDocuments() {
        log.info("Getting individuals with documents");

        return customerService.findIndividualsWithDocuments()
                .onItem().transform(customers -> Response.ok(customers).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting individuals with documents", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving individuals: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/count/legal-status/{isLegalEntity}")
    public Uni<Response> countByLegalStatus(@PathParam("isLegalEntity") Boolean isLegalEntity) {
        log.info("Counting customers by legal status: {}", isLegalEntity);

        return Uni.createFrom().item(isLegalEntity)
                .onItem().transform(Objects::isNull)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Legal status is required")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        customerService.countByLegalEntityStatus(isLegalEntity)
                                .onItem().transform(count -> Response.ok(count).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error counting customers by legal status: {}", isLegalEntity, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error counting customers: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/exists/document")
    public Uni<Response> existsByDocument(
            @QueryParam("series") String series,
            @QueryParam("number") String number) {

        log.info("Checking if customer exists by document: series={}, number={}", series, number);

        return Uni.createFrom().item(series)
                .onItem().transform(s -> s == null || s.trim().isEmpty())
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Document series is required")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        Uni.createFrom().item(number)
                                .onItem().transform(n -> n == null || n.trim().isEmpty())
                                .onItem().transform(isInvalid -> {
                                    if (isInvalid) {
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Document number is required")
                                                .build();
                                    }
                                    return null;
                                })
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        customerService.existsByDocument(series, number)
                                .onItem().transform(exists -> Response.ok(exists).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error checking customer existence by document: series={}, number={}", series, number, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error checking customer existence: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/exists/name/{name}")
    public Uni<Response> existsByName(@PathParam("name") String name) {
        log.info("Checking if customer exists by name: {}", name);

        return Uni.createFrom().item(name)
                .onItem().transform(n -> n == null || n.trim().isEmpty())
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Customer name is required")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        customerService.existsByName(name)
                                .onItem().transform(exists -> Response.ok(exists).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error checking customer existence by name: {}", name, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error checking customer existence: " + throwable.getMessage())
                            .build();
                });
    }

    @PUT
    @Path("/{id}/address")
    public Uni<Response> updateAddress(@PathParam("id") Long id, String newAddress) {
        log.info("Updating address for customer id: {}, new address: {}", id, newAddress);

        return Uni.createFrom().item(id)
                .onItem().transform(i -> i == null || i <= 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid customer ID")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        Uni.createFrom().item(newAddress)
                                .onItem().transform(addr -> addr == null || addr.trim().isEmpty())
                                .onItem().transform(isInvalid -> {
                                    if (isInvalid) {
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("New address is required")
                                                .build();
                                    }
                                    return null;
                                })
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        customerService.updateCustomerAddress(id, newAddress)
                                .onItem().transform(updatedCount -> {
                                    if (updatedCount > 0) {
                                        return Response.ok().entity("Address updated successfully").build();
                                    } else {
                                        return Response.status(Response.Status.NOT_FOUND).build();
                                    }
                                })
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating address for customer id: {}", id, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error updating address: " + throwable.getMessage())
                            .build();
                });
    }

    @PUT
    @Path("/{id}/bank-details")
    public Uni<Response> updateBankDetails(
            @PathParam("id") Long id,
            @QueryParam("bankName") String bankName,
            @QueryParam("bankAccount") String bankAccount) {

        log.info("Updating bank details for customer id: {}, bank: {}, account: {}", id, bankName, bankAccount);

        return Uni.createFrom().item(id)
                .onItem().transform(i -> i == null || i <= 0)
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid customer ID")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        Uni.createFrom().item(bankName)
                                .onItem().transform(name -> name == null || name.trim().isEmpty())
                                .onItem().transform(isInvalid -> {
                                    if (isInvalid) {
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Bank name is required")
                                                .build();
                                    }
                                    return null;
                                })
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        Uni.createFrom().item(bankAccount)
                                .onItem().transform(account -> account == null || account.trim().isEmpty())
                                .onItem().transform(isInvalid -> {
                                    if (isInvalid) {
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Bank account is required")
                                                .build();
                                    }
                                    return null;
                                })
                )
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        customerService.updateBankDetails(id, bankName, bankAccount)
                                .onItem().transform(updatedCount -> {
                                    if (updatedCount > 0) {
                                        return Response.ok().entity("Bank details updated successfully").build();
                                    } else {
                                        return Response.status(Response.Status.NOT_FOUND).build();
                                    }
                                })
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating bank details for customer id: {}", id, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error updating bank details: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/search")
    public Uni<Response> searchCustomers(
            @QueryParam("q") String searchTerm,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        log.info("Searching customers with term: {}, page: {}, size: {}", searchTerm, page, size);

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
                        customerService.findByNamePaginated("%" + searchTerm + "%", page, size)
                                .onItem().transform(customers -> Response.ok(customers).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error searching customers with term: {}", searchTerm, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error searching customers: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/stats/legal-entity")
    public Uni<Response> getCustomerStatsByLegalEntity() {
        log.info("Getting customer statistics by legal entity");

        return customerService.findCustomerStatsByLegalEntity()
                .onItem().transform(stats -> Response.ok(stats).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting customer statistics by legal entity", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving statistics: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/total/count")
    public Uni<Response> getTotalCount() {
        log.info("Getting total customers count");

        return customerService.countAll()
                .onItem().transform(count -> Response.ok(count).build())
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting total customers count", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error counting customers: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/search/page-count")
    public Uni<Response> getSearchPageCount(
            @QueryParam("q") String searchTerm,
            @QueryParam("size") @DefaultValue("20") int size) {

        log.info("Calculating page count for search term: {}, page size: {}", searchTerm, size);

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
                        customerService.getPageCountByName("%" + searchTerm + "%", size)
                                .onItem().transform(pageCount -> Response.ok(pageCount).build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error calculating page count for search term: {}", searchTerm, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error calculating page count: " + throwable.getMessage())
                            .build();
                });
    }

    @POST
    @Path("/bulk/mark-legal-entities")
    public Uni<Response> markLegalEntitiesWithBank(@QueryParam("bankName") String bankName) {
        log.info("Marking all legal entities with bank name: {}", bankName);

        return Uni.createFrom().item(bankName)
                .onItem().transform(name -> name == null || name.trim().isEmpty())
                .onItem().transform(isInvalid -> {
                    if (isInvalid) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Bank name is required")
                                .build();
                    }
                    return null;
                })
                .onItem().ifNotNull().transform(response -> response)
                .onItem().ifNull().switchTo(() ->
                        customerService.markAllLegalEntitiesWithBank(bankName)
                                .onItem().transform(updatedCount ->
                                        Response.ok()
                                                .entity("Successfully marked " + updatedCount + " legal entities with bank: " + bankName)
                                                .build())
                )
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error marking legal entities with bank name: {}", bankName, throwable);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Error marking legal entities: " + throwable.getMessage())
                            .build();
                });
    }
}