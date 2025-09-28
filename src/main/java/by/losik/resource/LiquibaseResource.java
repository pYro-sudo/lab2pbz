package by.losik.resource;

import by.losik.service.MigrationService;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jboss.resteasy.reactive.RestQuery;

import java.text.SimpleDateFormat;
import java.util.Map;

@Path("/api/liquibase")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Timeout(5000)
@Retry(maxRetries = 3, delay = 1000)
@CircuitBreaker(
        requestVolumeThreshold = 4,
        failureRatio = 0.5,
        delay = 10000,
        successThreshold = 2
)
public class LiquibaseResource {

    @Inject
    MigrationService liquibaseService;

    @POST
    @Path("/migrate")
    public Uni<Response> runMigrations(@RestQuery String contexts,
                                       @RestQuery String labels) {
        return liquibaseService.runMigration(
                        contexts != null ? contexts : "",
                        labels != null ? labels : ""
                )
                .onItem().transform(result -> Response.ok(Map.of("message", result)).build())
                .onFailure().recoverWithItem(error ->
                        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity(Map.of("error", error.getMessage()))
                                .build()
                );
    }

    @GET
    @Path("/status")
    public Uni<Response> getStatus(@RestQuery String contexts,
                                   @RestQuery String labels) {
        return liquibaseService.getMigrationStatus(
                        contexts != null ? contexts : "",
                        labels != null ? labels : ""
                )
                .onItem().transform(status -> Response.ok(status).build())
                .onFailure().recoverWithItem(error ->
                        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity(Map.of("error", error.getMessage()))
                                .build()
                );
    }

    @GET
    @Path("/applied")
    public Uni<Response> getAppliedMigrations() {
        return liquibaseService.getAppliedMigrations()
                .onItem().transform(migrations -> Response.ok(Map.of("appliedMigrations", migrations)).build())
                .onFailure().recoverWithItem(error ->
                        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity(Map.of("error", error.getMessage()))
                                .build()
                );
    }

    @POST
    @Path("/rollback/last")
    public Uni<Response> rollbackLast(@RestQuery String contexts,
                                      @RestQuery String labels) {
        return liquibaseService.rollbackLastChange(
                        contexts != null ? contexts : "",
                        labels != null ? labels : ""
                )
                .onItem().transform(result -> Response.ok(Map.of("message", result)).build())
                .onFailure().recoverWithItem(error ->
                        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity(Map.of("error", error.getMessage()))
                                .build()
                );
    }

    @POST
    @Path("/rollback/count/{count}")
    public Uni<Response> rollbackCount(@PathParam("count") int count,
                                       @RestQuery String contexts,
                                       @RestQuery String labels) {
        return liquibaseService.rollbackCount(
                        count,
                        contexts != null ? contexts : "",
                        labels != null ? labels : ""
                )
                .onItem().transform(result -> Response.ok(Map.of("message", result)).build())
                .onFailure().recoverWithItem(error ->
                        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity(Map.of("error", error.getMessage()))
                                .build()
                );
    }

    @POST
    @Path("/rollback/tag/{tag}")
    public Uni<Response> rollbackToTag(@PathParam("tag") String tag,
                                       @RestQuery String contexts,
                                       @RestQuery String labels) {
        return liquibaseService.rollbackToTag(
                        tag,
                        contexts != null ? contexts : "",
                        labels != null ? labels : ""
                )
                .onItem().transform(result -> Response.ok(Map.of("message", result)).build())
                .onFailure().recoverWithItem(error ->
                        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity(Map.of("error", error.getMessage()))
                                .build()
                );
    }

    @POST
    @Path("/rollback/date")
    public Uni<Response> rollbackToDate(@RestQuery String date,
                                        @RestQuery String contexts,
                                        @RestQuery String labels) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        return sdf.parse(date);
                    } catch (Exception e) {
                        throw new RuntimeException("Invalid date format. Use yyyy-MM-dd");
                    }
                }))
                .onItem().transformToUni(rollbackDate ->
                        liquibaseService.rollbackToDate(
                                rollbackDate,
                                contexts != null ? contexts : "",
                                labels != null ? labels : ""
                        )
                )
                .onItem().transform(result -> Response.ok(Map.of("message", result)).build())
                .onFailure().recoverWithItem(error ->
                        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity(Map.of("error", error.getMessage()))
                                .build()
                );
    }

    @POST
    @Path("/tag/{tag}")
    public Uni<Response> tagDatabase(@PathParam("tag") String tag) {
        return liquibaseService.tagDatabase(tag)
                .onItem().transform(result -> Response.ok(Map.of("message", result)).build())
                .onFailure().recoverWithItem(error ->
                        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity(Map.of("error", error.getMessage()))
                                .build()
                );
    }

    @POST
    @Path("/validate")
    public Uni<Response> validate(@RestQuery String contexts,
                                  @RestQuery String labels) {
        return liquibaseService.validateMigrations(
                        contexts != null ? contexts : "",
                        labels != null ? labels : ""
                )
                .onItem().transform(result -> Response.ok(Map.of("message", result)).build())
                .onFailure().recoverWithItem(error ->
                        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity(Map.of("error", error.getMessage()))
                                .build()
                );
    }

    @POST
    @Path("/clear-checksums")
    public Uni<Response> clearCheckSums() {
        return liquibaseService.clearCheckSums()
                .onItem().transform(result -> Response.ok(Map.of("message", result)).build())
                .onFailure().recoverWithItem(error ->
                        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity(Map.of("error", error.getMessage()))
                                .build()
                );
    }
}