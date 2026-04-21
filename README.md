Smart Campus API

-Overview-
A RESTful API for managing university campus rooms and sensors, 
built with JAX-RS (Jersey) and Apache Tomcat.

-How to build and run-
1. Clone this repository
2. Open in NetBeans as a Maven project
3. Ensure Apache Tomcat 9 is configured in NetBeans Services
4. Right-click project → Clean and Build
5. Right-click project → Run
6. API is available at: http://localhost:8080/SmartCampusAPI/api/v1/

PART 1
-Sample curl commands-
1) Get API info
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/

2) Get all rooms
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms

3) Create a room
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}'

4) Get a specific room
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301

5) Delete a room
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301

PART 2

### Get all rooms
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms

### Get a specific room
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301

### Create a room
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"HALL-205","name":"Main Hall","capacity":200}'

### Delete a room (succeeds if no sensors assigned)
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/HALL-205

### Delete a room with sensors (returns 409 Conflict)
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301

PART 3
### Get all sensors
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors

### Get sensors filtered by type
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2"

### Get a specific sensor
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001

### Create a sensor (valid roomId)
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-002","type":"Temperature","status":"ACTIVE","currentValue":21.0,"roomId":"LAB-101"}'

### Create a sensor (invalid roomId - returns 422)
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-003","type":"Temperature","status":"ACTIVE","currentValue":21.0,"roomId":"FAKE-999"}'

### Delete a sensor
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-002

PART 4

### Get all readings for a sensor
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings

### Add a new reading
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":24.5}'

### Add reading to MAINTENANCE sensor (returns 403)
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":15.0}'