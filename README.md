# Smart Campus API

## Overview
A RESTful API for managing university campus rooms and sensors, 
built using JAX-RS (Jersey) deployed on Apache Tomcat 9. 
The API follows REST architectural principles including resource-based 
interactions, statelessness, and appropriate HTTP status codes. 
Data is stored in-memory using HashMaps and ArrayLists.

All data is stored in-memory(no database was used).

## How to Build and Run

1. Clone this repository
2. Open NetBeans IDE
3. Go to File → Open Project and select the cloned folder
4. Make sure Apache Tomcat 9 is configured under the Services tab
5. Right-click the project → Clean and Build
6. Right-click the project → Run
7. The API will be available at:
   http://localhost:8080/SmartCampusAPI/api/v1/

## Sample curl Commands


# 1. Get API discovery info (Part 1)
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/

# 2. Create a new room (Part 2)
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"CONF-101","name":"Conference Room","capacity":15}'

# 3. Get sensors filtered by type (Part 3)
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=Humidity"

# 4. Post a new sensor reading (Part 4)
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/HUM-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":62.3}'

# 5. Attempt to delete a room with active sensors (Part 5 - shows 409 error handling)
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/ENG-401


## Report

### Part 1.1 — JAX-RS Resource Lifecycle

JAX-RS follows a request-scoped model by default, meaning the runtime 
spins up a fresh instance of each resource class every time a request 
arrives. So for something like RoomResource, a new object is created 
per request and thrown away once the response is sent.

The consequence of this for data management is significant. If I had 
stored the room or sensor data as regular fields inside RoomResource, 
that data would vanish the moment the request finished — the next 
request would get a completely empty object. To get around this I 
built a centralised DataStore class where all the maps are declared 
as static. Static fields live on the class itself in memory, so every 
request instance shares the exact same underlying data regardless of 
how many objects get created.

The downside of sharing mutable static state is that concurrent 
requests can interfere with each other. If two POST requests arrive 
simultaneously and both try to insert into the same HashMap, you can 
end up with unpredictable results or corrupted data — this is known 
as a race condition. A real production system would replace HashMap 
with ConcurrentHashMap or wrap writes in a synchronised block. For 
this coursework the static HashMap approach works correctly under 
normal usage.

### Part 1.2 — HATEOAS

HATEOAS is short for Hypermedia as the Engine of Application State. 
At its core the principle says that a REST response should do more 
than just return data — it should also tell the client where to go 
next by embedding navigational links directly in the response body.

In my implementation the GET /api/v1/ endpoint returns a JSON object 
that includes a resources field containing the paths to /api/v1/rooms 
and /api/v1/sensors. A brand new client hitting the API for the first 
time can discover the entire surface of the API from that one call 
without ever opening a documentation page.

The practical advantage over static documentation is that docs go 
stale — if I rename an endpoint, any client hardcoding the old URL 
breaks. But a client that reads the links from the discovery response 
and follows them will always end up at the right place because the 
server is the single source of truth for its own URLs.

### Part 2.1 — Returning IDs vs Full Objects

There are two approaches to returning a collection — send back just 
the IDs and let the client fetch details separately, or include the 
full objects straight away. I went with full objects.

The argument for IDs only is smaller initial payloads. But in 
practice a client displaying a room list needs the name, capacity 
and sensor count at minimum. If I returned only IDs, the client 
would need to fire off a separate GET request for each room — 
for a campus with 50 rooms that is 51 HTTP requests instead of one. 
The extra bandwidth from including full objects is far less costly 
than the latency of all those round trips, so returning full objects 
is the right call here.

### Part 2.2 — Idempotency of DELETE

DELETE is idempotent in my implementation. Idempotency means that 
repeating the same request multiple times produces the same outcome 
as sending it once.

When DELETE /rooms/CONF-101 is called the first time, the room gets 
removed and a 204 No Content comes back. If the client calls the 
exact same endpoint again, the room is already gone so it gets a 
404 Not Found. The key point is that the state of the server is 
identical in both cases — CONF-101 does not exist. The second call 
does not create any new change or side effect, it just reports that 
the target is already absent. That is what makes it idempotent.

### Part 3.1 — @Consumes and Content-Type Mismatches

Putting @Consumes(MediaType.APPLICATION_JSON) on the POST method 
declares a contract — this endpoint will only process requests that 
arrive with a Content-Type of application/json.

If something sends the request with text/plain or application/xml 
instead, JAX-RS catches the mismatch at the framework level before 
my code even runs and responds with 415 Unsupported Media Type. I 
do not have to write any validation logic for this myself. It also 
means I can be confident that by the time execution reaches my method 
body, Jersey has already confirmed the incoming data is in a format 
it can deserialise into a Sensor object. Without this annotation the 
server might try to parse a plain text string as JSON and throw an 
unhelpful internal error.


### Part 3.2 — @QueryParam vs Path Parameter for Filtering

I used @QueryParam("type") rather than embedding the type in the path 
like /sensors/type/Humidity because the two serve fundamentally 
different purposes in REST.

A path parameter points to a specific resource — /sensors/HUM-001 
means the one sensor with that exact ID. A query parameter modifies 
the way a collection is returned — /sensors?type=Humidity means the 
whole sensors collection, but narrowed down. Using a path segment for 
a filter value implies that Humidity is itself a resource that lives 
inside sensors, which is not semantically true.

Query parameters are also composable in a way path segments are not. 
If I later want to filter by both type and status, I can simply write 
?type=Humidity&status=ACTIVE. Doing the same with path parameters 
would require defining a new route like /sensors/type/Humidity/status/
ACTIVE which becomes unmanageable quickly.

### Part 4.1 — Sub-Resource Locator Pattern

The sub-resource locator is a JAX-RS pattern where a resource method 
carries only a @Path annotation with no HTTP verb annotation. Instead 
of handling the request itself it returns a reference to another 
resource class and hands off the rest of the work.

In my code SensorResource has a method mapped to 
@Path("/{sensorId}/readings"). It has no @GET or @POST on it — it 
just creates a SensorReadingResource with the sensor ID and returns 
it. JAX-RS then inspects the remaining part of the URL and the HTTP 
verb and calls the correct method on that object.

The architectural payoff is separation of concerns. Everything to do 
with reading history lives inside SensorReadingResource and nothing 
else touches it. If SensorResource also handled readings directly, 
the class would grow to cover sensor registration, sensor retrieval, 
sensor deletion and the full readings history for every sensor — it 
would be unmaintainable. Splitting by responsibility keeps each class 
focused and easier to test and modify independently.


### Part 5.1 — Why 422 Instead of 404

When a client sends a POST request to create a sensor with a roomId 
that does not exist, the correct response is 422 Unprocessable Entity 
rather than 404 Not Found.

404 means the URL endpoint itself could not be found on the server. 
But in this case the URL /api/v1/sensors is perfectly valid and the 
server found it without any problem. The issue is not with the URL — 
it is with the data inside the request body. The JSON payload is 
syntactically correct but it references a room that does not exist 
in the system, which means it fails a business logic validation rule.

422 is more accurate because it specifically means the server 
understood the request and the format was correct, but the content 
was semantically invalid. It tells the client the problem is in their 
data, not their URL. This makes it much easier for the client 
developer to diagnose and fix the issue.

### Part 5.2 — Security Risks of Exposing Stack Traces

Returning a raw Java stack trace in an API response is a serious 
information disclosure vulnerability. The trace contains things an 
attacker can directly act on.

First it reveals the full package hierarchy of the application — 
com.smartcampus.resource.RoomResource for example — which tells the 
attacker exactly how the codebase is structured. Second it lists 
every framework and library involved along with version numbers. An 
attacker can cross-reference those versions against public CVE 
databases to find known exploits. Third the exact line numbers and 
method call chain show the internal execution path, which helps an 
attacker reverse engineer the business logic and find edge cases to 
exploit.

Essentially a stack trace saves an attacker hours of reconnaissance 
work. My global ExceptionMapper<Throwable> ensures that none of this 
ever leaves the server. Any unhandled exception gets caught, logged 
internally, and replaced with a plain 500 response that says nothing 
useful to an outsider.

### Part 5.3 — Why Use Filters for Logging

Adding Logger.info() calls manually inside every resource method 
works but it is fragile and repetitive. Every time a new endpoint 
gets added someone has to remember to add logging to it. If the log 
format needs updating the change has to be made in every method 
across every resource class.

JAX-RS filters solve this cleanly. The ContainerRequestFilter runs 
before every single request regardless of which endpoint it targets, 
and the ContainerResponseFilter runs after every response on the way 
out. I write the logging logic once inside LoggingFilter and it 
automatically covers RoomResource, SensorResource, 
SensorReadingResource and DiscoveryResource without any of those 
classes knowing it exists.

This is an application of the separation of concerns principle — 
logging has nothing to do with room management or sensor registration, 
so it should not live inside those classes. Filters are the correct 
JAX-RS mechanism for behaviour that cuts across the whole API, which 
is exactly what logging is.