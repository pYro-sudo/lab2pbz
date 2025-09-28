package by.losik.resource;

import by.losik.entity.Region;
import by.losik.service.RegionService;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

@Resource
@Path("/api/regions")
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
public class RegionResource {

    @Inject
    RegionService regionService;

    @GET
    public Uni<Response> getAllRegions(
            @QueryParam("sort") @DefaultValue("name") String sortField,
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @QueryParam("page") @DefaultValue("0") int pageIndex,
            @QueryParam("size") @DefaultValue("20") int pageSize) {

        log.info("Getting all regions sorted by {} {}, page: {}, size: {}",
                sortField, direction, pageIndex, pageSize);

        return regionService.findPaginatedSorted(pageIndex, pageSize,
                        io.quarkus.panache.common.Sort.by(sortField,
                                io.quarkus.panache.common.Sort.Direction.valueOf(direction.toUpperCase())))
                .onItem().transform(regions -> {
                    log.debug("Retrieved {} regions", regions.size());
                    return Response.ok(regions).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting all regions", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving regions: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getRegionById(@PathParam("id") Long id) {
        log.info("Getting region by id: {}", id);

        return regionService.findById(id)
                .onItem().ifNotNull().transform(region -> {
                    log.debug("Found region with id: {}", id);
                    return Response.ok(region).build();
                })
                .onItem().ifNull().continueWith(() -> {
                    log.warn("Region with id {} not found", id);
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("Region not found with id: " + id)
                            .build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting region by id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving region: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/country/{country}")
    public Uni<Response> getRegionsByCountry(@PathParam("country") String country) {
        log.info("Getting regions by country: {}", country);

        return regionService.findByCountry(country)
                .onItem().transform(regions -> {
                    log.debug("Found {} regions for country: {}", regions.size(), country);
                    return Response.ok(regions).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting regions by country: {}", country, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving regions: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/search")
    public Uni<Response> searchRegions(
            @QueryParam("q") String searchTerm,
            @QueryParam("page") @DefaultValue("0") int pageIndex,
            @QueryParam("size") @DefaultValue("20") int pageSize) {

        log.info("Searching regions with term: {}, page: {}, size: {}", searchTerm, pageIndex, pageSize);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Search term cannot be empty")
                    .build());
        }

        Uni<Response> searchUni;

        if (pageIndex >= 0 && pageSize > 0) {
            searchUni = regionService.searchRegionsPaginated(searchTerm, pageIndex, pageSize)
                    .onItem().transform(regions -> {
                        log.debug("Found {} regions matching search term: {}", regions.size(), searchTerm);
                        return Response.ok(regions).build();
                    });
        } else {
            searchUni = regionService.searchRegions(searchTerm)
                    .onItem().transform(regions -> {
                        log.debug("Found {} regions matching search term: {}", regions.size(), searchTerm);
                        return Response.ok(regions).build();
                    });
        }

        return searchUni.onFailure().recoverWithItem(throwable -> {
            log.error("Error searching regions with term: {}", searchTerm, throwable);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error searching regions: " + throwable.getMessage())
                    .build();
        });
    }

    @GET
    @Path("/countries")
    public Uni<Response> getDistinctCountries() {
        log.info("Getting distinct countries");

        return regionService.findDistinctCountries()
                .onItem().transform(countries -> {
                    log.debug("Found {} distinct countries", countries.size());
                    return Response.ok(countries).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting distinct countries", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving countries: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/country-contains/{countryPart}")
    public Uni<Response> getRegionsByCountryContaining(@PathParam("countryPart") String countryPart) {
        log.info("Getting regions by country containing: {}", countryPart);

        return regionService.findByCountryContaining(countryPart)
                .onItem().transform(regions -> {
                    log.debug("Found {} regions with country containing: {}", regions.size(), countryPart);
                    return Response.ok(regions).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting regions by country containing: {}", countryPart, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving regions: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/name-contains/{namePart}")
    public Uni<Response> getRegionsByNameContaining(@PathParam("namePart") String namePart) {
        log.info("Getting regions by name containing: {}", namePart);

        return regionService.findByNameContaining(namePart)
                .onItem().transform(regions -> {
                    log.debug("Found {} regions with name containing: {}", regions.size(), namePart);
                    return Response.ok(regions).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting regions by name containing: {}", namePart, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving regions: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/country/{country}/name-contains/{namePart}")
    public Uni<Response> getRegionsByCountryAndName(
            @PathParam("country") String country,
            @PathParam("namePart") String namePart) {

        log.info("Getting regions by country: {} and name containing: {}", country, namePart);

        return regionService.findByCountryAndName(country, namePart)
                .onItem().transform(regions -> {
                    log.debug("Found {} regions for country and name query", regions.size());
                    return Response.ok(regions).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting regions by country and name", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving regions: " + throwable.getMessage())
                            .build();
                });
    }

    @POST
    public Uni<Response> createRegion(Region region) {
        log.info("Creating new region: {}, country: {}", region.getName(), region.getCountry());

        if (region.getName() == null || region.getName().trim().isEmpty()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Region name is required")
                    .build());
        }

        if (region.getCountry() == null || region.getCountry().trim().isEmpty()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Country is required")
                    .build());
        }

        return regionService.existsByNameAndCountry(region.getName(), region.getCountry())
                .onItem().transformToUni(exists -> {
                    if (exists) {
                        log.warn("Region with name {} and country {} already exists", region.getName(), region.getCountry());
                        return Uni.createFrom().item(Response.status(Response.Status.CONFLICT)
                                .entity("Region with name " + region.getName() + " and country " + region.getCountry() + " already exists")
                                .build());
                    } else {
                        return regionService.save(region)
                                .onItem().transform(savedRegion -> {
                                    log.info("Successfully created region with id: {}", savedRegion.getId());
                                    return Response.status(Response.Status.CREATED)
                                            .entity(savedRegion)
                                            .build();
                                });
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error creating region", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error creating region: " + throwable.getMessage())
                            .build();
                });
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> updateRegion(@PathParam("id") Long id, Region region) {
        log.info("Updating region with id: {}", id);

        if (!id.equals(region.getId())) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("ID in path does not match ID in request body")
                    .build());
        }

        return regionService.update(region)
                .onItem().transform(updatedRegion -> {
                    log.info("Successfully updated region with id: {}", id);
                    return Response.ok(updatedRegion).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating region with id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error updating region: " + throwable.getMessage())
                            .build();
                });
    }

    @PATCH
    @Path("/{id}/name")
    public Uni<Response> updateRegionName(@PathParam("id") Long id, String newName) {
        log.info("Updating region name for id: {} to {}", id, newName);

        if (newName == null || newName.trim().isEmpty()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Region name cannot be empty")
                    .build());
        }

        return regionService.updateRegionName(id, newName)
                .onItem().transform(updatedCount -> {
                    if (updatedCount > 0) {
                        log.info("Successfully updated region name for id: {}", id);
                        return Response.ok().entity("Region name updated successfully").build();
                    } else {
                        log.warn("No region found with id: {}", id);
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("Region not found with id: " + id)
                                .build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating region name for id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error updating region name: " + throwable.getMessage())
                            .build();
                });
    }

    @PATCH
    @Path("/{id}/country")
    public Uni<Response> updateRegionCountry(@PathParam("id") Long id, String newCountry) {
        log.info("Updating region country for id: {} to {}", id, newCountry);

        if (newCountry == null || newCountry.trim().isEmpty()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Country cannot be empty")
                    .build());
        }

        return regionService.updateRegionCountry(id, newCountry)
                .onItem().transform(updatedCount -> {
                    if (updatedCount > 0) {
                        log.info("Successfully updated region country for id: {}", id);
                        return Response.ok().entity("Region country updated successfully").build();
                    } else {
                        log.warn("No region found with id: {}", id);
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("Region not found with id: " + id)
                                .build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating region country for id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error updating region country: " + throwable.getMessage())
                            .build();
                });
    }

    @PUT
    @Path("/country/update")
    public Uni<Response> updateCountryForRegions(
            @QueryParam("oldCountry") String oldCountry,
            @QueryParam("newCountry") String newCountry) {

        log.info("Updating country for regions from: {} to: {}", oldCountry, newCountry);

        if (oldCountry == null || newCountry == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Both oldCountry and newCountry parameters are required")
                    .build());
        }

        return regionService.updateCountryForRegions(oldCountry, newCountry)
                .onItem().transform(updatedCount -> {
                    log.info("Successfully updated country for {} regions", updatedCount);
                    return Response.ok().entity("Updated country for " + updatedCount + " regions").build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error updating country for regions", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error updating country for regions: " + throwable.getMessage())
                            .build();
                });
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> deleteRegion(@PathParam("id") Long id) {
        log.info("Deleting region with id: {}", id);

        return regionService.deleteById(id)
                .onItem().transform(deleted -> {
                    if (deleted) {
                        log.info("Successfully deleted region with id: {}", id);
                        return Response.noContent().build();
                    } else {
                        log.warn("Region with id {} not found for deletion", id);
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("Region not found with id: " + id)
                                .build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error deleting region with id: {}", id, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error deleting region: " + throwable.getMessage())
                            .build();
                });
    }

    @DELETE
    @Path("/country/{country}")
    public Uni<Response> deleteRegionsByCountry(@PathParam("country") String country) {
        log.info("Deleting regions by country: {}", country);

        return regionService.deleteByCountry(country)
                .onItem().transform(deletedCount -> {
                    log.info("Successfully deleted {} regions for country: {}", deletedCount, country);
                    return Response.ok().entity("Deleted " + deletedCount + " regions").build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error deleting regions by country: {}", country, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error deleting regions: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/stats/country/{country}")
    public Uni<Response> getRegionCountByCountry(@PathParam("country") String country) {
        log.info("Getting region count by country: {}", country);

        return regionService.countByCountry(country)
                .onItem().transform(count -> {
                    log.debug("Found {} regions for country: {}", count, country);
                    return Response.ok().entity(count).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting region count by country: {}", country, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving region count: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/stats/no-settlements")
    public Uni<Response> getRegionsWithoutSettlements() {
        log.info("Getting regions without settlements");

        return regionService.findRegionsWithoutSettlements()
                .onItem().transform(regions -> {
                    log.debug("Found {} regions without settlements", regions.size());
                    return Response.ok(regions).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting regions without settlements", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving regions without settlements: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/stats/settlement-count")
    public Uni<Response> getSettlementCountByRegion() {
        log.info("Getting settlement count by region");

        return regionService.getSettlementCountByRegion()
                .onItem().transform(results -> {
                    log.debug("Found settlement count for {} regions", results.size());
                    return Response.ok(results).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error getting settlement count by region", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error retrieving settlement count: " + throwable.getMessage())
                            .build();
                });
    }

    @GET
    @Path("/exists/name-country")
    public Uni<Response> checkRegionExistsByNameAndCountry(
            @QueryParam("name") String name,
            @QueryParam("country") String country) {

        log.info("Checking if region exists by name: {} and country: {}", name, country);

        if (name == null || country == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Both name and country parameters are required")
                    .build());
        }

        return regionService.existsByNameAndCountry(name, country)
                .onItem().transform(exists -> {
                    log.debug("Region exists by name {} and country {}: {}", name, country, exists);
                    return Response.ok().entity(exists).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    log.error("Error checking if region exists by name and country", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error checking region existence: " + throwable.getMessage())
                            .build();
                });
    }
}