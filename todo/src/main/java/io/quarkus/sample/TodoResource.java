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

    /**
     * This method is responsible for fetching all Todo items in the database.
     * It logs access info, increments a counter for monitoring purposes,
     * and records the time taken to execute the query. The items are returned
     * sorted by their "order" field.
     *
     * @return A list of Todo items sorted by their order.
     */
    @GET
    @Operation(summary = "List All Tasks")
    @APIResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    public List<Todo> getAll() {
        // Log the method access
        LOG.info("getAll");
        
        // Increment the counter to monitor method access frequency
        registry.counter("io.quarkus.sample.TodoResource.getAll.count").increment();
        
        // Create and start a timer to measure the execution time of fetching all Todos
        Timer timer = registry.timer("io.quarkus.sample.TodoResource.getAll.time");
        return timer.record(() -> {
            return Todo.listAll(Sort.by("order"));
        });
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
        Timer timer = registry.timer("io.quarkus.sample.TodoResource.create.time");
        return timer.record(() -> {
            return Response.status(Status.CREATED).entity(item).build();
        });
        
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
        registry.counter("io.quarkus.sample.TodoResource.deleteOne.count").increment();
        Timer timer = registry.timer("io.quarkus.sample.TodoResource.delete.time");
        return timer.record( () -> {
            Todo entity = Todo.findById(id);
        
        if (entity == null) {
            throw new WebApplicationException("Todo with id of " + id + " does not exist.", Status.NOT_FOUND);
        }
        entity.delete();
        return Response.noContent().build();
        });
       }
}