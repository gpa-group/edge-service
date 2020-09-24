package com.gpa.edge.client;

import java.util.HashMap;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.config.ConfigProvider;

import org.jboss.resteasy.annotations.jaxrs.PathParam;

import io.smallrye.mutiny.Uni;

import com.gpa.edge.client.SensorService;
import com.gpa.edge.datahub.DataHubClientService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;


import javax.enterprise.context.ApplicationScoped;

import javax.transaction.Transactional;

import io.quarkus.scheduler.Scheduled;

@Path("/")
@ApplicationScoped
public class SensorResource {

    int id;
    String deviceName = ConfigProvider.getConfig().getValue("device.name", String.class);

    int i = 0;
    
    void onStart(@Observes StartupEvent ev) {    

        System.out.println("device name: " + deviceName);           
        String serialId = getSerial();


        HashMap<String,Double> coordinates = getCoordinates();

        double latitude = coordinates.get("latitude");
        double longitude = coordinates.get("longitude");


        String stringId = "";
        try{
            stringId = dataHub.register(serialId, "edGpa2", latitude, longitude);
        } catch(Exception e){
            System.out.println("Registration failed: " + e.getMessage());
        }
        if(stringId != null){
            id = Integer.valueOf(stringId);
            System.out.println("stringId: " + id);
        }else{
            System.out.println("stringId nullo");
        }
    }

    void onStop(@Observes ShutdownEvent ev) {               

        try{
            dataHub.unregister(id);
        } catch(Exception e){
            System.out.println("errore: " + e.getMessage());
        }
    }

    @Scheduled(every = "10s") 
    void schedule() {
        i++;
        System.out.println("nuovo valore: " + i);
    }



    @Inject
    @RestClient
    SensorService sensorService;

    @Inject
    @RestClient
    DataHubClientService dataHub;

    @GET
    @Path("/gas")
    @Produces(MediaType.TEXT_PLAIN)
    public String gas() {
        return sensorService.getGas();
    }

    @GET
    @Path("/pollution")
    @Produces(MediaType.TEXT_PLAIN)
    public String pollution() {
        return sensorService.getPollution();
    }

    @GET
    @Path("/getSerial")
    @Produces(MediaType.TEXT_PLAIN)
    public String serial() throws Exception{
        try{
            return sensorService.getSerial() + dataHub.a;
        }catch(Exception e){
            return "eccezione: " + e.getMessage();
        }
    }

    @GET
    @Path("/delete")
    @Produces(MediaType.TEXT_PLAIN)
    public String unregister() throws Exception{

        System.out.println("Id: " + id);
        try{
            dataHub.unregister(id);
        } catch(Exception e){
            System.out.println("errore: " + e.getMessage());
        }
        return "fatto?" + id;
    }


    /*
    *
    *   PRIVATE METHODS
    *
    */

    private String getSerial(){
        //get SerialId method
        try{
            return sensorService.getSerial();
        }catch(Exception e){
            System.out.println("Error getting serialId: " + e.getMessage());
            return "ERROROOOOOOOOOOO";
        }
    }

    private HashMap<String,Double> getCoordinates(){
        //get coordinates methof
        HashMap<String,Double> coordinates = new HashMap<String,Double>();
        coordinates.put("latitude", 45.395402);
        coordinates.put("longitude", 11.945819);
        return coordinates;
    }
}
