package com.gpa.edge.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.concurrent.CompletionStage;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;

@Path("/search")
@RegisterRestClient(configKey="coordinates-api")
@ApplicationScoped
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
public interface CoordinatesService { 
    /*
    *   recieving 302
    */ 
    
    @GET
    @Path("/q/{q}/format/{format}/addressdetails/{addressdetails}")
    @Produces("application/text")
    String getCoordinates(
        @PathParam String q,
        @PathParam String format,
        @PathParam String addressdetails) throws Exception;
}
