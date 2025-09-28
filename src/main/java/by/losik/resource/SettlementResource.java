package by.losik.resource;

import by.losik.entity.Settlement;
import by.losik.service.SettlementService;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Resource
@Path("/api/settlements")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
@Authenticated
public class SettlementResource {

    @Inject
    SettlementService settlementService;

    @GET
    public Uni<Response> getAllSettlements(
            @QueryParam("sort") @DefaultValue("name") String sortField,
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @QueryParam("page") @DefaultValue("0") int pageIndex,
            @QueryParam("size") @DefaultValue("20") int pageSize) {

        log.info("Getting all settlements sorted by {} {}, page: {}, size: {}",
                sortField, direction, pageIndex, pageSize);

        return settlementService.findPaginatedSorted(pageIndex, pageSize,
                        io.quarkus.panache.common.Sort.by(sortField,
                                io.quarkus.panache.common.Sort.Direction.valueOf(direction.toUpperCase())))
                .onItem().transform(settlements -> {
                    log.debug("Retrieved {} settlements", settlements.size());
                    return Response.ok(settlements).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting all settlements", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving settlements: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getSettlementById(@PathParam("id") Long id) {
        log.info("Getting settlement by id: {}", id);

        return settlementService.findById(id)
                .onItem().ifNotNull().transform(settlement -> {
                    log.debug("Found settlement with id: {}", id);
                    return Response.ok(settlement).build();
                })
                .onItem().ifNull().continueWith(() -> {
                    log.warn("Settlement with id {} not found", id);
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("Settlement not found with id: " + id)
                            .build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting settlement by id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving settlement: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/region/{regionId}")
    public Uni<Response> getSettlementsByRegion(@PathParam("regionId") Long regionId) {
        log.info("Getting settlements by region id: {}", regionId);

        return settlementService.findByRegionId(regionId)
                .onItem().transform(settlements -> {
                    log.debug("Found {} settlements for region id: {}", settlements.size(), regionId);
                    return Response.ok(settlements).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting settlements by region id: {}", regionId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving settlements: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/search")
    public Uni<Response> searchSettlements(
            @QueryParam("q") String searchTerm,
            @QueryParam("page") @DefaultValue("0") int pageIndex,
            @QueryParam("size") @DefaultValue("20") int pageSize) {

        log.info("Searching settlements with term: {}, page: {}, size: {}", searchTerm, pageIndex, pageSize);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Search term cannot be empty")
                    .build());
        }

        Uni<Response> searchUni;

        if (pageIndex >= 0 && pageSize > 0) {
            searchUni = settlementService.searchSettlementsPaginated(searchTerm, pageIndex, pageSize)
                    .onItem().transform(settlements -> {
                        log.debug("Found {} settlements matching search term: {}", settlements.size(), searchTerm);
                        return Response.ok(settlements).build();
                    });
        } else {
            searchUni = settlementService.searchSettlements(searchTerm)
                    .onItem().transform(settlements -> {
                        log.debug("Found {} settlements matching search term: {}", settlements.size(), searchTerm);
                        return Response.ok(settlements).build();
                    });
        }

        return searchUni.onFailure().recoverWithItem(throwable -> {
            log.error("Error searching settlements with term: {}", searchTerm, throwable);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error searching settlements: " + throwable.getMessage())
                    .build();
        });
    }

    @GET
    @Path("/name-contains/{namePart}")
    public Uni<Response> getSettlementsByNameContaining(@PathParam("namePart") String namePart) {
        log.info("Getting settlements by name containing: {}", namePart);

        return settlementService.findByNameContaining(namePart)
                .onItem().transform(settlements -> {
                    log.debug("Found {} settlements with name containing: {}", settlements.size(), namePart);
                    return Response.ok(settlements).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting settlements by name containing: {}", namePart, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving settlements: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/region/{regionId}/name-contains/{namePart}")
    public Uni<Response> getSettlementsByRegionAndName(
            @PathParam("regionId") Long regionId,
            @PathParam("namePart") String namePart) {

        log.info("Getting settlements by region id: {} and name containing: {}", regionId, namePart);

        return settlementService.findByRegionIdAndName(regionId, namePart)
                .onItem().transform(settlements -> {
                    log.debug("Found {} settlements for region id and name query", settlements.size());
                    return Response.ok(settlements).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting settlements by region id and name", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving settlements: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/without-invoices")
    public Uni<Response> getSettlementsWithoutInvoices() {
        log.info("Getting settlements without invoices");

        return settlementService.findSettlementsWithoutInvoices()
                .onItem().transform(settlements -> {
                    log.debug("Found {} settlements without invoices", settlements.size());
                    return Response.ok(settlements).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting settlements without invoices", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving settlements without invoices: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/with-invoices")
    public Uni<Response> getSettlementsWithInvoices() {
        log.info("Getting settlements with invoices");

        return settlementService.findSettlementsWithInvoices()
                .onItem().transform(settlements -> {
                    log.debug("Found {} settlements with invoices", settlements.size());
                    return Response.ok(settlements).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting settlements with invoices", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving settlements with invoices: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/most-active")
    public Uni<Response> getMostActiveSettlements(@QueryParam("limit") @DefaultValue("10") int limit) {
        log.info("Getting most active settlements, limit: {}", limit);

        return settlementService.findMostActiveSettlements(limit)
                .onItem().transform(settlements -> {
                    log.debug("Found {} most active settlements", settlements.size());
                    return Response.ok(settlements).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting most active settlements", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving most active settlements: " + throwable.getMessage())
                            .build();
                });
    }

    @POST
    public Uni<Response> createSettlement(Settlement settlement) {
        log.info("Creating new settlement: {}, region id: {}",
                settlement.name(),
                settlement.regionId() != null ? settlement.regionId().id() : "null");

        // Валидация обязательных полей
        if (settlement.name() == null || settlement.name().trim().isEmpty()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Settlement name is required")
                    .build());
        }

        if (settlement.regionId() == null || settlement.regionId().id() == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Region ID is required")
                    .build());
        }

        return settlementService.existsByNameAndRegionId(settlement.name(), settlement.regionId().id())
                .onItem().transformToUni(exists -> {
                    if (exists) {
                        log.warn("Settlement with name {} and region id {} already exists",
                                settlement.name(), settlement.regionId().id());
                        return Uni.createFrom().item(Response.status(Response.Status.CONFLICT)
                                .entity("Settlement with name " + settlement.name() + " and region id " +
                                        settlement.regionId().id() + " already exists")
                                .build());
                    } else {
                        return settlementService.save(settlement)
                                .onItem().transform(savedSettlement -> {
                                    log.info("Successfully created settlement with id: {}", savedSettlement.id());
                                    return Response.status(Response.Status.CREATED)
                                            .entity(savedSettlement)
                                            .build();
                                });
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error creating settlement", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error creating settlement: " + throwable.getMessage())
                            .build();
                });
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> updateSettlement(@PathParam("id") Long id, Settlement settlement) {
        log.info("Updating settlement with id: {}", id);

        // Убеждаемся, что ID в пути совпадает с ID в теле
        if (!id.equals(settlement.id())) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("ID in path does not match ID in request body")
                    .build());
        }

        return settlementService.update(settlement)
                .onItem().transform(updatedSettlement -> {
                    log.info("Successfully updated settlement with id: {}", id);
                    return Response.ok(updatedSettlement).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating settlement with id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error updating settlement: " + throwable.getMessage())
                            .build();
                });
    }

    @PATCH
    @Path("/{id}/name")
    public Uni<Response> updateSettlementName(@PathParam("id") Long id, String newName) {
        log.info("Updating settlement name for id: {} to {}", id, newName);

        if (newName == null || newName.trim().isEmpty()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Settlement name cannot be empty")
                    .build());
        }

        return settlementService.updateSettlementName(id, newName)
                .onItem().transform(updatedCount -> {
                    if (updatedCount > 0) {
                        log.info("Successfully updated settlement name for id: {}", id);
                        return Response.ok().entity("Settlement name updated successfully").build();
                    } else {
                        log.warn("No settlement found with id: {}", id);
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("Settlement not found with id: " + id)
                                .build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating settlement name for id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error updating settlement name: " + throwable.getMessage())
                            .build();
                });
    }

    @PATCH
    @Path("/{id}/region")
    public Uni<Response> updateSettlementRegion(@PathParam("id") Long id, Long newRegionId) {
        log.info("Updating settlement region for id: {} to {}", id, newRegionId);

        if (newRegionId == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Region ID cannot be null")
                    .build());
        }

        return settlementService.updateSettlementRegion(id, newRegionId)
                .onItem().transform(updatedCount -> {
                    if (updatedCount > 0) {
                        log.info("Successfully updated settlement region for id: {}", id);
                        return Response.ok().entity("Settlement region updated successfully").build();
                    } else {
                        log.warn("No settlement found with id: {}", id);
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("Settlement not found with id: " + id)
                                .build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating settlement region for id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error updating settlement region: " + throwable.getMessage())
                            .build();
                });
    }

    @PUT
    @Path("/transfer-region")
    public Uni<Response> transferSettlementsToRegion(
            @QueryParam("fromRegionId") Long fromRegionId,
            @QueryParam("toRegionId") Long toRegionId) {

        log.info("Transferring settlements from region: {} to region: {}", fromRegionId, toRegionId);

        if (fromRegionId == null || toRegionId == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Both fromRegionId and toRegionId parameters are required")
                    .build());
        }

        if (fromRegionId.equals(toRegionId)) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Source and target region IDs cannot be the same")
                    .build());
        }

        return settlementService.transferSettlementsToRegion(fromRegionId, toRegionId)
                .onItem().transform(updatedCount -> {
                    log.info("Successfully transferred {} settlements to new region", updatedCount);
                    return Response.ok().entity("Transferred " + updatedCount + " settlements to new region").build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error transferring settlements between regions", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error transferring settlements: " + throwable.getMessage())
                            .build();
                });
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> deleteSettlement(@PathParam("id") Long id) {
        log.info("Deleting settlement with id: {}", id);

        return settlementService.deleteById(id)
                .onItem().transform(deleted -> {
                    if (deleted) {
                        log.info("Successfully deleted settlement with id: {}", id);
                        return Response.noContent().build();
                    } else {
                        log.warn("Settlement with id {} not found for deletion", id);
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("Settlement not found with id: " + id)
                                .build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error deleting settlement with id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error deleting settlement: " + throwable.getMessage())
                            .build();
                });
    }

    @DELETE
    @Path("/region/{regionId}")
    public Uni<Response> deleteSettlementsByRegion(@PathParam("regionId") Long regionId) {
        log.info("Deleting settlements by region id: {}", regionId);

        return settlementService.deleteByRegionId(regionId)
                .onItem().transform(deletedCount -> {
                    log.info("Successfully deleted {} settlements for region id: {}", deletedCount, regionId);
                    return Response.ok().entity("Deleted " + deletedCount + " settlements").build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error deleting settlements by region id: {}", regionId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error deleting settlements: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/stats/region/{regionId}")
    public Uni<Response> getSettlementCountByRegion(@PathParam("regionId") Long regionId) {
        log.info("Getting settlement count by region id: {}", regionId);

        return settlementService.countByRegionId(regionId)
                .onItem().transform(count -> {
                    log.debug("Found {} settlements for region id: {}", count, regionId);
                    return Response.ok().entity(count).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting settlement count by region id: {}", regionId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving settlement count: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/stats/invoice-stats")
    public Uni<Response> getSettlementInvoiceStats() {
        log.info("Getting settlement invoice statistics");

        return settlementService.getSettlementInvoiceStats()
                .onItem().transform(results -> {
                    log.debug("Found invoice stats for {} settlements", results.size());
                    return Response.ok(results).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting settlement invoice statistics", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving settlement invoice statistics: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/exists/name-region")
    public Uni<Response> checkSettlementExistsByNameAndRegion(
            @QueryParam("name") String name,
            @QueryParam("regionId") Long regionId) {

        log.info("Checking if settlement exists by name: {} and region id: {}", name, regionId);

        if (name == null || regionId == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Both name and regionId parameters are required")
                    .build());
        }

        return settlementService.existsByNameAndRegionId(name, regionId)
                .onItem().transform(exists -> {
                    log.debug("Settlement exists by name {} and region id {}: {}", name, regionId, exists);
                    return Response.ok().entity(exists).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error checking if settlement exists by name and region", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error checking settlement existence: " + throwable.getMessage())
                            .build();
                });
    }
}