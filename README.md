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