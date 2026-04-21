/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import com.smartcampus.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/sensors")
public class SensorResource {

   
    // GET /api/v1/sensors
    // Returns all sensors, with optional type= filter
  
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSensors(@QueryParam("type") String type) {

        List<Sensor> sensorList = new ArrayList<>(DataStore.sensors.values());

        // If type query param is provided, filter the list
        if (type != null && !type.isEmpty()) {
            List<Sensor> filtered = new ArrayList<>();
            for (Sensor s : sensorList) {
                if (s.getType() != null && s.getType().equalsIgnoreCase(type)) {
                    filtered.add(s);
                }
            }
            return Response.ok(filtered).build();
        }

        // No filter — return everything
        return Response.ok(sensorList).build();
    }

    // GET /api/v1/sensors/{sensorId}
    // Returns a single sensor by ID
  
    @GET
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensorById(@PathParam("sensorId") String sensorId) {

        Sensor sensor = DataStore.sensors.get(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Sensor not found\"}")
                    .build();
        }

        return Response.ok(sensor).build();
    }

    // POST /api/v1/sensors
    // Registers a new sensor and Validates that the roomId actually exists
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {

        // Validate that the roomId exists in DataStore
        // If not, throw LinkedResourceNotFoundException (422)
        if (sensor.getRoomId() == null || 
            !DataStore.rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                "Room with ID '" + sensor.getRoomId() + 
                "' does not exist. Cannot register sensor."
            );
        }

        // Check if sensor ID already exists
        if (DataStore.sensors.containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"Sensor with this ID already exists\"}")
                    .build();
        }

        // Save sensor to DataStore
        DataStore.sensors.put(sensor.getId(), sensor);

        // Also add this sensor's ID to the room's sensorIds list
        // This keeps the room and sensor data in sync
        DataStore.rooms.get(sensor.getRoomId())
                       .getSensorIds()
                       .add(sensor.getId());

        // Also initialise an empty readings list for this sensor
        DataStore.readings.put(sensor.getId(), new ArrayList<>());

        return Response.status(Response.Status.CREATED)
                .entity(sensor)
                .build();
    }

    // DELETE /api/v1/sensors/{sensorId}
    // Removes a sensor and unlinks it from its room

    @DELETE
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {

        Sensor sensor = DataStore.sensors.get(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Sensor not found\"}")
                    .build();
        }

        // Remove sensor ID from the room's sensorIds list
        String roomId = sensor.getRoomId();
        if (roomId != null && DataStore.rooms.containsKey(roomId)) {
            DataStore.rooms.get(roomId).getSensorIds().remove(sensorId);
        }

        // Remove sensor and its readings from DataStore
        DataStore.sensors.remove(sensorId);
        DataStore.readings.remove(sensorId);

        return Response.noContent().build(); // 204 No Content
    }
    
  
    // Part 4 Sub-resource locator
    // GET/POST /api/v1/sensors/{sensorId}/readings
    // It returns the resource object and JAX-RS
    // figures out which method to call on it

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(
            @PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}