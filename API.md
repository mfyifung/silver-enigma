# Order API

Spring Boot 3 / Java 21 REST API for the "Order Lists" admin feature, backed by
an embedded SQLite database at `/workspace/data/app.db`. The server listens on
`0.0.0.0:8080` so peer boxes can reach it at `http://backend:8080` (via an
Angular `/api` proxy).

Domain taxonomy:

- `type` ∈ `Accessories`, `Mobile & Phone`, `Electronics`, `Home & Living`,
  `Fashion & Beauty`, `Services & Industry`, `Book & Stationary`,
  `Health & Medicine`
- `status` ∈ `In Transit`, `On Hold`, `Rejected`, `Processing`, `Completed`

On first boot, a `CommandLineRunner` seeds 14 rows spanning every `type` and
every `status` across 8 distinct dates. The seeder is idempotent (only inserts
when the table is empty), so restarts never duplicate rows.

All filtering (`status`, `type`, `date`) is executed as a SQL `WHERE` clause via
a Spring Data JPA `Specification` — never filtered in-memory in the controller.

## `GET /api/orders`

Lists orders. Optional, combinable filters — combining different filter
dimensions ANDs them together; multiple values for the *same* dimension are
OR-ed (`IN (...)`), passed either as repeated params (`?status=A&status=B`) or
CSV (`?status=A,B`):

- `status` — repeatable / CSV
- `type` — repeatable / CSV
- `date` — `YYYY-MM-DD`, single value

### `curl http://localhost:8080/api/orders`

```json
[{"id":1,"orderId":"00001","name":"Amara Chen","address":"12 Riverside Ave, Austin, TX","date":"2026-06-30","type":"Accessories","status":"Completed"},{"id":2,"orderId":"00002","name":"Liam O'Brien","address":"88 Maple St, Portland, OR","date":"2026-07-01","type":"Mobile & Phone","status":"In Transit"},{"id":3,"orderId":"00003","name":"Priya Nair","address":"45 Elm Court, Seattle, WA","date":"2026-07-01","type":"Electronics","status":"Processing"},{"id":4,"orderId":"00004","name":"Diego Alvarez","address":"7 Birch Lane, Denver, CO","date":"2026-07-02","type":"Home & Living","status":"On Hold"},{"id":5,"orderId":"00005","name":"Sofia Rossi","address":"231 Pine Rd, Miami, FL","date":"2026-07-02","type":"Fashion & Beauty","status":"Completed"},{"id":6,"orderId":"00006","name":"Kenji Sato","address":"19 Cedar Blvd, San Jose, CA","date":"2026-07-03","type":"Services & Industry","status":"Rejected"},{"id":7,"orderId":"00007","name":"Grace Mensah","address":"5 Willow Way, Chicago, IL","date":"2026-07-03","type":"Book & Stationary","status":"In Transit"},{"id":8,"orderId":"00008","name":"Noah Kim","address":"310 Oak St, Boston, MA","date":"2026-07-04","type":"Health & Medicine","status":"Processing"},{"id":9,"orderId":"00009","name":"Ana Silva","address":"77 Spruce Ave, Phoenix, AZ","date":"2026-07-04","type":"Accessories","status":"Completed"},{"id":10,"orderId":"00010","name":"Ethan Brooks","address":"42 Aspen Dr, Raleigh, NC","date":"2026-07-05","type":"Mobile & Phone","status":"On Hold"},{"id":11,"orderId":"00011","name":"Mei Lin","address":"16 Poplar St, San Diego, CA","date":"2026-07-05","type":"Electronics","status":"Completed"},{"id":12,"orderId":"00012","name":"Omar Farouk","address":"58 Cherry Ln, Dallas, TX","date":"2026-07-06","type":"Home & Living","status":"Rejected"},{"id":13,"orderId":"00013","name":"Isabella Costa","address":"9 Magnolia Ct, Orlando, FL","date":"2026-07-06","type":"Fashion & Beauty","status":"In Transit"},{"id":14,"orderId":"00014","name":"Ravi Patel","address":"124 Sycamore Ave, Atlanta, GA","date":"2026-07-07","type":"Services & Industry","status":"Processing"}]
```

### `curl "http://localhost:8080/api/orders?status=Completed"`

```json
[{"id":1,"orderId":"00001","name":"Amara Chen","address":"12 Riverside Ave, Austin, TX","date":"2026-06-30","type":"Accessories","status":"Completed"},{"id":5,"orderId":"00005","name":"Sofia Rossi","address":"231 Pine Rd, Miami, FL","date":"2026-07-02","type":"Fashion & Beauty","status":"Completed"},{"id":9,"orderId":"00009","name":"Ana Silva","address":"77 Spruce Ave, Phoenix, AZ","date":"2026-07-04","type":"Accessories","status":"Completed"},{"id":11,"orderId":"00011","name":"Mei Lin","address":"16 Poplar St, San Diego, CA","date":"2026-07-05","type":"Electronics","status":"Completed"}]
```

### `curl "http://localhost:8080/api/orders?status=Completed,On%20Hold"` (CSV multi-value, OR within dimension)

```json
[{"id":1,"orderId":"00001","name":"Amara Chen","address":"12 Riverside Ave, Austin, TX","date":"2026-06-30","type":"Accessories","status":"Completed"},{"id":4,"orderId":"00004","name":"Diego Alvarez","address":"7 Birch Lane, Denver, CO","date":"2026-07-02","type":"Home & Living","status":"On Hold"},{"id":5,"orderId":"00005","name":"Sofia Rossi","address":"231 Pine Rd, Miami, FL","date":"2026-07-02","type":"Fashion & Beauty","status":"Completed"},{"id":9,"orderId":"00009","name":"Ana Silva","address":"77 Spruce Ave, Phoenix, AZ","date":"2026-07-04","type":"Accessories","status":"Completed"},{"id":10,"orderId":"00010","name":"Ethan Brooks","address":"42 Aspen Dr, Raleigh, NC","date":"2026-07-05","type":"Mobile & Phone","status":"On Hold"},{"id":11,"orderId":"00011","name":"Mei Lin","address":"16 Poplar St, San Diego, CA","date":"2026-07-05","type":"Electronics","status":"Completed"}]
```

### `curl "http://localhost:8080/api/orders?status=Completed&type=Electronics"` (AND across dimensions)

```json
[{"id":11,"orderId":"00011","name":"Mei Lin","address":"16 Poplar St, San Diego, CA","date":"2026-07-05","type":"Electronics","status":"Completed"}]
```

### `curl "http://localhost:8080/api/orders?date=2026-07-04"`

```json
[{"id":8,"orderId":"00008","name":"Noah Kim","address":"310 Oak St, Boston, MA","date":"2026-07-04","type":"Health & Medicine","status":"Processing"},{"id":9,"orderId":"00009","name":"Ana Silva","address":"77 Spruce Ave, Phoenix, AZ","date":"2026-07-04","type":"Accessories","status":"Completed"}]
```

## `GET /api/orders/meta`

Distinct `type`/`status` values actually present in the data — drives the UI's
filter dropdowns without hardcoding the taxonomy on the client.

### `curl http://localhost:8080/api/orders/meta`

```json
{"types":["Accessories","Book & Stationary","Electronics","Fashion & Beauty","Health & Medicine","Home & Living","Mobile & Phone","Services & Industry"],"statuses":["Completed","In Transit","On Hold","Processing","Rejected"]}
```

## `GET /api/orders/{id}`

### `curl http://localhost:8080/api/orders/1`

```json
{"id":1,"orderId":"00001","name":"Amara Chen","address":"12 Riverside Ave, Austin, TX","date":"2026-06-30","type":"Accessories","status":"Completed"}
```

### `curl -i http://localhost:8080/api/orders/99999` (absent id → 404)

```
HTTP/1.1 404
Content-Length: 0
Date: Sat, 11 Jul 2026 08:30:59 GMT
```

## Running it

```bash
mvn -DskipTests package
java -jar target/demo-0.0.1-SNAPSHOT.jar --spring.datasource.url=jdbc:sqlite:/workspace/data/app.db
```

The server binds `0.0.0.0:8080` (see `server.address` in
`application.properties`) so a peer box on the shared network reaches it at
`http://backend:8080`.

## Tests

`src/test/java/com/example/demo/order/OrderRepositoryTest.java` (`@DataJpaTest`,
pointed at a separate test-only SQLite file via
`src/test/resources/application.properties` so it never contends with the live
server's file lock or mutates seeded data):

```
mvn test
...
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

1. `roundTripsAnOrderThroughTheRepository` — saves an `Order`, reads it back by
   id, asserts every field round-trips.
2. `filterQueryReturnsOnlyTheExpectedSubset` — seeds three orders (two
   `Completed`, one `On Hold`), queries via
   `OrderSpecifications.withStatuses(List.of("Completed"))`, and asserts the
   result contains only the `Completed` rows.
