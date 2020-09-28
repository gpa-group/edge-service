package com.gpa.edge.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.concurrent.CompletionStage;

import io.smallrye.mutiny.Uni;


@RegisterRestClient(configKey="sensor-api")
public interface SensorService {

    @GET
    @Path("/gas")
    @Produces("application/text")
    String getGas();

    @GET
    @Path("/pollution")
    @Produces("application/text")
    String getPollution();

    @GET
    @Path("/serial")
    @Produces("application/text")
    String getSerial() throws Exception;

}
