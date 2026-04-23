/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStore {

    public static final Map<String, Room> rooms = new HashMap<>();
    public static final Map<String, Sensor> sensors = new HashMap<>();
    public static final Map<String, List<SensorReading>> readings = new HashMap<>();

    static {
        // Sample rooms
        Room r1 = new Room("ENG-401", "Engineering Lab", 40);
        Room r2 = new Room("CHEM-202", "Chemistry Lab", 25);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);

        // Sample sensors
        Sensor s1 = new Sensor("HUM-001", "Humidity", "ACTIVE", 55.0, "ENG-401");
        Sensor s2 = new Sensor("MOT-001", "Motion", "ACTIVE", 1.0, "ENG-401");
        Sensor s3 = new Sensor("NOISE-001", "Noise", "MAINTENANCE", 0.0, "CHEM-202");

        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);

        r1.getSensorIds().add(s1.getId());
        r1.getSensorIds().add(s2.getId());
        r2.getSensorIds().add(s3.getId());

        readings.put(s1.getId(), new ArrayList<>());
        readings.put(s2.getId(), new ArrayList<>());
        readings.put(s3.getId(), new ArrayList<>());
    }
}
