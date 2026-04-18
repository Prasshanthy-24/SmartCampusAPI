/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import com.smartcampus.DataStore;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/rooms")
public class RoomResource {

    // GET /api/v1/rooms
    // Returns all rooms as a JSON array
   
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRooms() {
        List<Room> roomList = new ArrayList<>(DataStore.rooms.values());
        return Response.ok(roomList).build();
    }

    // POST /api/v1/rooms
    // Creates a new room
   
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {

        // Check if a room with this ID already exists
        if (DataStore.rooms.containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"Room with this ID already exists\"}")
                    .build();
        }

        // Save the room into HashMap
        DataStore.rooms.put(room.getId(), room);

        // Return 201 Created with the new room as the body
        return Response.status(Response.Status.CREATED)
                .entity(room)
                .build();
    }

    // GET /api/v1/rooms/{roomId}
    // Returns a single room by its ID
  
    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoomById(@PathParam("roomId") String roomId) {

        Room room = DataStore.rooms.get(roomId);

        // If room not found, return 404
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Room not found\"}")
                    .build();
        }

        return Response.ok(room).build();
    }

    // DELETE /api/v1/rooms/{roomId}
    // Deletes a room - but ONLY if it has no sensors
  
    @DELETE
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRoom(@PathParam("roomId") String roomId) {

        Room room = DataStore.rooms.get(roomId);

        // If room not found, return 404
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Room not found\"}")
                    .build();
        }

        // Business logic: block deletion if room still has sensors
        // This throws RoomNotEmptyException 
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                "Cannot delete room '" + roomId + 
                "' — it still has " + room.getSensorIds().size() + 
                " sensor(s) assigned to it."
            );
        }

        // Safe to delete
        DataStore.rooms.remove(roomId);

        return Response.noContent().build(); // 204 No Content
    }
}