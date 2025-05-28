package io.quarkus.sample;

import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.jboss.logging.Logger;
import io.micrometer.core.instrument.MeterRegistry;
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
    public List<Todo> getAll() {
        LOG.info("getAll");
        registry.counter("io.quarkus.sample.TodoResource.getAll.count").increment();
        if (ApplicationConfig.IS_READY.get()){
            Timer timer = registry.timer("io.quarkus.sample.TodoResource.getAll.time");
            return timer.record(() -> {
                return Todo.listAll(Sort.by("order"));
            });
        }else{
            throw new WebApplicationException();
        }       
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
    public Todo getOne(@PathParam("id") Long id) {
        Todo entity = Todo.findById(id);
        registry.counter("io.quarkus.sample.TodoResource.getOne.count").increment();
        if (entity == null) {
            throw new WebApplicationException("Todo with id of " + id + " does not exist.", Status.NOT_FOUND);
        }
        Timer timer = registry.timer("io.quarkus.sample.TodoResource.getOne.time");
        return timer.record(() -> {
            return entity;
        });
        
    }
    @POST
    @Transactional
    @Operation(summary = "Create Tasks")
    @APIResponse(responseCode = "201",description = "Created", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    public Response create(@Valid Todo item) {
        item.persist();
        registry.counter("io.quarkus.sample.TodoResource.create.count").increment();
        return Response.status(Status.CREATED).entity(item).build();
    }
    @PATCH
    @Path("/{id}")
    @Operation(summary = "Update Task")
    @APIResponse(responseCode = "200",description = "Updated", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @Transactional
    public Response update(@Valid Todo todo, @PathParam("id") Long id) {
        Todo entity = Todo.findById(id);
        entity.id = id;
        entity.completed = todo.completed;
        entity.order = todo.order;
        entity.title = todo.title;
        entity.url = todo.url;
        registry.counter("io.quarkus.sample.TodoResource.update.count").increment();
        Timer timer = registry.timer("io.quarkus.sample.TodoResource.update.time");
        return timer.record(() -> {
            return Response.ok(entity).build();
        });
        
    }

    @DELETE
    @Transactional
    public Response deleteCompleted() {
        Timer timer = registry.timer("io.quarkus.sample.TodoResource.delete");
        return timer.record(() -> {
            Todo.deleteCompleted();
            return Response.noContent().build();
        });
       
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Operation(summary = "Delete Tasks")
    @APIResponses(
        value = {
            @APIResponse(responseCode = "204", description ="deleted", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
            @APIResponse(responseCode = "404", description ="Not found", content = @Content(mediaType = MediaType.APPLICATION_JSON))
        }
    )
    public Response deleteOne(@PathParam("id") Long id) {
        Todo entity = Todo.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Todo with id of " + id + " does not exist.", Status.NOT_FOUND);
        }
        entity.delete();
        registry.counter("io.quarkus.sample.TodoResource.deleteOne.count").increment();
        return Response.noContent().build();
    }


    @GET
    @Path("/not_ready")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Set Readiness to false")
    @APIResponse(responseCode = "200", content = @Content(mediaType = MediaType.TEXT_PLAIN))
    public Response notReadyApp() {
        LOG.info("Set Readiness to false");
        ApplicationConfig.IS_READY.set(false);
        return Response.ok().encoding("text/plain")
        .entity("Readiness: false")
            // .expires(Date.from(Instant.now().plus(Duration.ofMillis(0))))
            .build();
    }

    @GET
    @Path("/ready")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Set Readiness to true")
    @APIResponse(responseCode = "200", content = @Content(mediaType = MediaType.TEXT_PLAIN))
    public Response readyApp() {
        LOG.info("Set Readiness to true");
        ApplicationConfig.IS_READY.set(true);
        return Response.ok().encoding("text/plain")
        .entity("Readiness: true")
            // .expires(Date.from(Instant.now().plus(Duration.ofMillis(0))))
            .build();
    }

}