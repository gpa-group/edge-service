package com.gpa.edge.client;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.lang.StringBuffer;
import java.net.URL;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

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
public class EdgeResource {

    @Inject
    DataHubServiceImpl dataHubServiceImpl;

    @Inject
    @RestClient
    SensorService sensorService;

    @Inject
    @RestClient
    DataHubClientService dataHub;

    int id;
    String deviceName = ConfigProvider.getConfig().getValue("device.name", String.class);

    int i = 0;
    
    void onStart(@Observes StartupEvent ev) 
        throws Exception {    
        
        System.out.println("here to serve!");   

        System.out.println("device name: " + deviceName);           
        String serialId = getSerial();

        CoordinatesBean coordinates = getCoordinates("via lisbona padova");

        double latitude = coordinates.latitude;
        double longitude = coordinates.longitude;

        String stringId = null;
        try{
            stringId = dataHub.register(serialId, deviceName, longitude, latitude);
            id = Integer.valueOf(stringId);
            System.out.println("Registred. Id: " + id);
        } catch(Exception e){
            System.out.println("Registration failed: " + e.getMessage());
            stringId = "ERRORID";
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

    @Scheduled(every = "5s") 
    void scheduled() throws Exception {
        String measure = null;
        String decoratedGasMeasure = null;
        String decoratedPollutionMeasure = null;

        try{
            measure = sensorService.getGas();
            decoratedGasMeasure = decorateMeasure(measure);
            dataHubServiceImpl.sendGas(decoratedGasMeasure);
        }catch(Exception e){
            System.out.println("Error getting gas measure" + e.getMessage());
        }

        try{
            measure = sensorService.getPollution();
            decoratedPollutionMeasure = decorateMeasure(measure);
            dataHubServiceImpl.sendPollution(decoratedPollutionMeasure);
        }catch(Exception e){
            System.out.println("Error getting pollution measure" + e.getMessage());
        }
    }

    @GET
    @Path("/gas")
    @Produces(MediaType.TEXT_PLAIN)
    public String gas() throws Exception {
        return sensorService.getGas();
    }

    @GET
    @Path("/pollution")
    @Produces(MediaType.TEXT_PLAIN)
    public String pollution() throws Exception {
        return sensorService.getPollution();
    }

    @GET
    @Path("/getSerial")
    @Produces(MediaType.TEXT_PLAIN)
    public String serial() throws Exception{
        try{
            return sensorService.getSerial();
        }catch(Exception e){
            return "Error gettin serial: " + e.getMessage();
        }
    }

    @GET
    @Path("/delete")
    @Produces(MediaType.TEXT_PLAIN)
    public String unregister() throws Exception{
        //delete resource
        try{
            dataHub.unregister(id);
        } catch(Exception e){
            System.out.println("Error unregistrering: " + e.getMessage());
            return "Error unregistrering: " + e.getMessage();
        }
        return "Unregistred id: " + id;
    }


    /*
    *
    *   PRIVATE METHODS
    *
    */

    private String getSerial(){
        //get SerialId method
        try{

            String jsonStringSerial = sensorService.getSerial();
            JsonObject jsonObject = new JsonParser()
                .parse(jsonStringSerial).getAsJsonObject();

            return jsonObject.get("serial_number").getAsString();

        }catch(Exception e){
            System.out.println("Error getting serialId: " + e.getMessage());
            return "ERROROOOOOOOOOOO";
        }
    }

    private String decorateMeasure(String measure) {

        JsonObject jsonObject = new JsonParser()
            .parse(measure).getAsJsonObject();
        jsonObject.addProperty("stationId", id);
        jsonObject.addProperty("instant", OffsetDateTime.now(ZoneOffset.UTC).toInstant().toString());

        return jsonObject.toString();

    }

    public CoordinatesBean getCoordinates(String address)
            throws Exception {

        //get coordinates method

        CoordinatesBean coordinates = null;
        StringBuffer query = null;
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
        URL url = new URL(query.toString());
        InputStream is = url.openStream();
        String result = IOUtils.toString(is);
    //    String result = coordinatesService.getCoordinates(address, "json", "1");  //not used as recieving 302
        JsonObject jsonObject = new JsonParser()
            .parse(result).getAsJsonArray().get(0).getAsJsonObject();
        coordinates = new CoordinatesBean();
        coordinates.longitude = Double
            .parseDouble(jsonObject.get("lon").getAsString());
        coordinates.latitude = Double
            .parseDouble(jsonObject.get("lat").getAsString());
        
        return coordinates;
    }
 
}
