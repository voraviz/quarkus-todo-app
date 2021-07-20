package io.quarkus.sample;

import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.ConcurrentGauge;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.media.Content;
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TodoResource {

    @OPTIONS
    public Response opt() {
        return Response.ok().build();
    }

    @GET
    @Operation(summary = "List All Tasks")
    @APIResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @Counted(
        name = "countGetAll", 
        description = "Counts how many times the getAll method has been invoked"
        )
    @Timed(
        name = "timeGetAll", 
        description = "Times how long it takes to invoke the getAll method in second", 
        unit = MetricUnits.SECONDS
        )
    @ConcurrentGauge(
            name = "concurrentGetAll",
            description = "Concurrent connection to GetAll method"
            )    public Uni<List<Todo>> getAll() {
        System.out.println(Thread.currentThread().getName());
        return Panache.withTransaction(
                () -> Todo.findAll(Sort.by("order")).list()
        );
    }

    @GET
    @Blocking
    @Path("/blocking")
    public List<Todo> getAllBlocking() {
        System.out.println(Thread.currentThread().getName());
        return getAll()
                .await().indefinitely();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "getOne Tasks")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "200", description = "Found", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
            @APIResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = MediaType.APPLICATION_JSON))
        }
    )
    @Counted(
        name = "countGetOne", 
        description = "Counts how many times the getOne method has been invoked"
        )
    @Timed(
        name = "timeGetOne", 
        description = "Times how long it takes to invoke the getOne method in second", 
        unit = MetricUnits.SECONDS
        )
    @ConcurrentGauge(
            name = "concurrentGetOne",
            description = "Concurrent connection to GetOne method"
        )
    public Uni<Todo> getOne(@PathParam("id") Long id) {
        return Panache.withTransaction(
                () -> Todo.<Todo>findById(id)
                    .onItem().ifNull().failWith(() ->
                        new WebApplicationException("Todo with id of " + id + " does not exist.", Status.NOT_FOUND)
                ));
    }

    @POST
    @Operation(summary = "Create Tasks")
    @APIResponse(responseCode = "201",description = "Created", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @Counted(
        name = "countCreate", 
        description = "Counts how many times the create method has been invoked"
        )
    @Timed(
        name = "timeCreate", 
        description = "Times how long it takes to create the getAll method in second", 
        unit = MetricUnits.SECONDS
        )
    @ConcurrentGauge(
            name = "concurrentCreate",
            description = "Concurrent connection to create method"
        )
    public Uni<Response> create(@Valid Todo item) {
        return Panache.withTransaction(
                () -> item.persist()
            ).replaceWith(
                () -> Response.status(Status.CREATED).entity(item).build()
        );
    }

    @PATCH
    @Path("/{id}")
    @Operation(summary = "Update Task")
    @APIResponse(responseCode = "200",description = "Updated", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @Counted(
        name = "countUpdate", 
        description = "Counts how many times the update method has been invoked"
        )
    @Timed(
        name = "timeUpdate", 
        description = "Times how long it takes to invoke the update method in second", 
        unit = MetricUnits.SECONDS
        )
    @ConcurrentGauge(
            name = "concurrentUpdate",
            description = "Concurrent connection to update method"
        )
    public Uni<Todo> update(@Valid Todo todo, @PathParam("id") Long id) {
        return Panache.withTransaction(
                () -> Todo.<Todo>findById(id)
                .onItem().transform(entity -> {
                    entity.id = id;
                    entity.completed = todo.completed;
                    entity.order = todo.order;
                    entity.title = todo.title;
                    entity.url = todo.url;
                    return entity;
                })
        );
    }

    @DELETE
    public Uni<Response> deleteCompleted() {
        return Panache.withTransaction(
                () -> Todo.deleteCompleted()
        ).replaceWith(
                () -> Response.noContent().build()
        );
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete Tasks")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "204", description ="deleted", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
            @APIResponse(responseCode = "404", description ="Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON))
        }
    )
    @Counted(
        name = "countDeleteOne", 
        description = "Counts how many times the deleteOne method has been invoked"
        )
    @Timed(
        name = "timeDeleteOne", 
        description = "Times how long it takes to deleteOne the getAll method in second", 
        unit = MetricUnits.SECONDS
        )
    @ConcurrentGauge(
            name = "concurrentDeleteOne",
            description = "Concurrent connection to deleteOne method"
        )
    public Uni<Response> deleteOne(@PathParam("id") Long id) {
        return Panache.withTransaction(
                () -> Todo.findById(id)
                        .onItem().ifNull().failWith(() ->
                            new WebApplicationException("Todo with id of " + id + " does not exist.", Status.NOT_FOUND)
                        )
                        .call(entity -> entity.delete())
        ).replaceWith(
                () -> Response.noContent().build()
        );
    }

}