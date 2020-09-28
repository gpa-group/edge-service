package com.gpa.edge.client;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.lang.StringBuffer;
import java.net.URL;
import java.io.InputStream;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import org.jboss.resteasy.annotations.jaxrs.PathParam;

import io.smallrye.mutiny.Uni;

import com.gpa.edge.client.SensorService;
import com.gpa.edge.datahub.DataHubClientService;
import com.gpa.edge.client.CoordinatesService;
import com.gpa.edge.datahub.DataHubServiceImpl;
import com.gpa.edge.client.CoordinatesBean;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;


import javax.enterprise.context.ApplicationScoped;

import javax.transaction.Transactional;

import io.quarkus.scheduler.Scheduled;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Path("/")
@ApplicationScoped
public class SensorResource {
    @Inject
    DataHubServiceImpl dataHubServiceImpl;
    int id;
    String deviceName = ConfigProvider.getConfig().getValue("device.name", String.class);

    int i = 0;
    
    void onStart(@Observes StartupEvent ev) 
        throws Exception {    

        System.out.println("device name: " + deviceName);           
        String serialId = getSerial();

        HashMap<String,Double>  coordinates = getCoordinates();
        
        double latitude = coordinates.get("latitude");
        double longitude = coordinates.get("longitude");

        // CoordinatesBean coordinates = getCoordinates("padova");

        // double latitude = coordinates.latitude;
        // double longitude = coordinates.longitude;


        String stringId = "";
        try{
            stringId = dataHub.register(serialId, deviceName, longitude, latitude);
            id = Integer.valueOf(stringId);
            System.out.println("Registred. Id: " + id);
        } catch(Exception e){
            System.out.println("Registration failed: " + e.getMessage());
        }
        
    }

    void onStop(@Observes ShutdownEvent ev) {               

        try{
            dataHub.unregister(id);
            System.out.println("Device Id unregistred: " + id);
        } catch(Exception e){
            System.out.println("Error unregistering: " + e.getMessage());
        }
    }

    @Scheduled(every = "10s") 
    void schedule() {
        String measure = null;
        String completedGasMeasure = null;
        String completedPollutionMeasure = null;

        try{
            measure = sensorService.getGas();
            completedGasMeasure = getCompletedMeasure(measure);
        //    dataHubServiceImpl.sendGas(completedGasMeasure);
        }catch(Exception e){
            System.out.println("Error getting gas measure");
        }

        try{
            measure = sensorService.getPollution();
            completedPollutionMeasure = getCompletedMeasure(measure);
        //    dataHubServiceImpl.sendPollution(completedPollutionMeasure);
        }catch(Exception e){
            System.out.println("Error getting pollution measure");
        }
    }

    @Inject
    @RestClient
    SensorService sensorService;

    @Inject
    @RestClient
    DataHubClientService dataHub;

    @Inject
    @RestClient
    CoordinatesService coordinatesService;

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
            return sensorService.getSerial();
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

    private String getCompletedMeasure(String measure) {
        JsonObject jsonObject = new JsonParser()
            .parse(measure).getAsJsonObject();
        jsonObject.addProperty("stationId", id);
        jsonObject.addProperty("instant", OffsetDateTime.now(ZoneOffset.UTC).toInstant().toString());
        return jsonObject.toString();
    }

    /* public CoordinatesBean getCoordinates(String address)
            throws Exception {
        CoordinatesBean coordinates = null;
        /* StringBuffer query = null;
        String[] split = null;
        split = address.split(" ");
        query = new StringBuffer();
        query.append("https://nominatim.openstreetmap.org/search?q=");
        if (split.length == 0) {
            return null;
        }
        for (int i = 0; i < split.length; i++) {
            query.append(split[i]);
            if (i < (split.length - 1)) {
                query.append("+");
            }
        }
        query.append("&format=json&addressdetails=1");
        System.out.println("Query:" + query); */
        // URL url = new URL(query.toString());
     /*   String coords = coordinatesService.getCoordinates(address, "json", "1");
        System.out.println(coords);
        JsonObject jsonObject = new JsonParser()
            .parse(coords).getAsJsonArray().get(0).getAsJsonObject();
        System.out.println(jsonObject.toString());
        coordinates = new CoordinatesBean();
        coordinates.longitude = Double
            .parseDouble(jsonObject.get("lon").toString());
        coordinates.latitude = Double
            .parseDouble(jsonObject.get("lat").toString());

        return coordinates;
    }
 */
}
