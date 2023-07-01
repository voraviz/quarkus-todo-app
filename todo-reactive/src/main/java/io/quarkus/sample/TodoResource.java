package io.quarkus.sample;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;
import io.micrometer.core.instrument.Timer;
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TodoResource {

    private static final Logger LOG = Logger.getLogger(TodoResource.class);
    private final MeterRegistry registry;

     TodoResource(MeterRegistry registry) {
        this.registry = registry;
    }

    @OPTIONS
    public Response opt() {
        return Response.ok().build();
    }

    @GET
    @Operation(summary = "List All Tasks")
    @APIResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    public Uni<List<Todo>> getAll() {
       LOG.info(Thread.currentThread().getName());
       registry.counter("io.quarkus.sample.TodoResource.getAll.count").increment();
       Timer timer = registry.timer("io.quarkus.sample.TodoResource.getAll.time");
       return timer.record(() -> {
             return Panache.withTransaction(
                () -> Todo.findAll(Sort.by("order")).list()
        );
       });
    }

    @GET
    @Blocking
    @Path("/blocking")
    public List<Todo> getAllBlocking() {
        LOG.info(Thread.currentThread().getName());
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
    public Uni<Todo> getOne(@PathParam("id") Long id) {
        registry.counter("io.quarkus.sample.TodoResource.getOne.count").increment();
        Timer timer = registry.timer("io.quarkus.sample.TodoResource.getOne.time");
        return timer.record(() -> {
            return Panache.withTransaction(
                () -> Todo.<Todo>findById(id)
                    .onItem().ifNull().failWith(() ->
                        new WebApplicationException("Todo with id of " + id + " does not exist.", Status.NOT_FOUND)
                ));
        });
    }
    @POST
    @Operation(summary = "Create Tasks")
    @APIResponse(responseCode = "201",description = "Created", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    public Uni<Response> create(@Valid Todo item) {
        registry.counter("io.quarkus.sample.TodoResource.create.count").increment();
        Timer timer = registry.timer("io.quarkus.sample.TodoResource.create.time");
         return timer.record(() -> {
            return Panache.withTransaction(
                () -> item.persist()
            ).replaceWith(
                () -> Response.status(Status.CREATED).entity(item).build()
        );
        });     
    }

    @PATCH
    @Path("/{id}")
    @Operation(summary = "Update Task")
    @APIResponse(responseCode = "200",description = "Updated", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    public Uni<Todo> update(@Valid Todo todo, @PathParam("id") Long id) {
        registry.counter("io.quarkus.sample.TodoResource.update.count").increment();
        Timer timer = registry.timer("io.quarkus.sample.TodoResource.update.time");
        return timer.record(() -> {
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
        });
        
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
    public Uni<Response> deleteOne(@PathParam("id") Long id) {
        registry.counter("io.quarkus.sample.TodoResource.deleteOne.count").increment();        
        Timer timer = registry.timer("io.quarkus.sample.TodoResource.delete.time");
        return timer.record( () -> {
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

        );
        // return Panache.withTransaction(
        //         () -> Todo.findById(id)
        //                 .onItem().ifNull().failWith(() ->
        //                     new WebApplicationException("Todo with id of " + id + " does not exist.", Status.NOT_FOUND)
        //                 )
        //                 .call(entity -> entity.delete())
        // ).replaceWith(
        //         () -> Response.noContent().build()
        // );
    }

}