package com.gpa.edge.datahub;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

@Path("/v1/register")
@RegisterRestClient(configKey = "datahub-api")
@ApplicationScoped
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
public interface DataHubClientService {

	@PUT
	@Path("/serial/{serial}/name/{name}/longitude/{longitude}/latitude/{latitude}")
	String register(
		@PathParam("serial") String serial,
		@PathParam("name") String name,
		@PathParam("longitude") double longitude,
		@PathParam("latitude") double latitude) throws Exception;

	@DELETE
	@Path("/id/{id}")
	public void unregister(
		@PathParam("id") int id) throws Exception;

	
}