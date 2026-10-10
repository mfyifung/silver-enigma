# Orders API

Spring Boot 3 / Java 21 service (package `com.example.demo.order`) serving persisted
"orders" from an embedded **SQLite** database, with server-side filtering.

- Base URL (inside the box): `http://localhost:8080`
- Base URL (from peer boxes on the shared network): `http://backend:8080`
- Listens on `0.0.0.0:8080` (`server.address` / `server.port` in `application.properties`)

## SQLite setup

| Setting | Value |
|---|---|
| JDBC URL | `jdbc:sqlite:/workspace/data/app.db` (file-backed; create the dir with `mkdir -p /workspace/data`) |
| Driver | `org.xerial:sqlite-jdbc` (`org.sqlite.JDBC`) |
| Dialect | `org.hibernate.community.dialect.SQLiteDialect` (`hibernate-community-dialects`) |
| Schema | created/updated by Hibernate (`spring.jpa.hibernate.ddl-auto=update`), table `orders` |
| Seed data | `OrderSeeder` (CommandLineRunner) inserts 14 rows on startup **only when the table is empty** (idempotent across restarts) |
| Tests | use a separate throwaway file `target/test-app.db` (`src/test/resources/application.properties`) — never the live DB |

Table `orders`: `id` (INTEGER PK, auto), `order_id` (display id, unique, e.g. `00009`),
`name`, `address`, `order_date` (ISO `yyyy-MM-dd` text), `type`, `status`.

Domain taxonomy used by the seed:
- **type** ∈ Accessories, Mobile & Phone, Electronics, Home & Living, Fashion & Beauty, Services & Industry, Book & Stationary, Health & Medicine
- **status** ∈ In Transit, On Hold, Rejected, Processing, Completed

## Run

```bash
mkdir -p /workspace/data && nohup mvn -q -DskipTests spring-boot:run > /tmp/backend.log 2>&1 &
mvn test        # @DataJpaTest repository tests + @SpringBootTest MockMvc endpoint tests
```

## Order JSON

```json
{"id":9,"orderId":"00009","name":"Dollie Hines","address":"124 Lyla Forge Suite 975","date":"2026-01-09","type":"Electronics","status":"In Transit"}
```

## Endpoints

### `GET /api/orders`
Returns orders as a JSON array, sorted by `orderId`. With no params: **all rows**.

| Param | Form | Semantics |
|---|---|---|
| `status` | repeatable (`?status=A&status=B`), CSV (`?status=A,B`), or both mixed (`?status=A,B&status=C`) | multi-select: row's status is any of the values |
| `type` | repeatable, CSV, or both mixed | multi-select: row's type is any of the values |
| `date` | `YYYY-MM-DD` | exact order date; malformed → **400** |

All params are optional and combinable (AND across params, OR within one param).
Matching is exact (case-sensitive) against the values returned by `/api/orders/meta`.
Filtering is executed **in the database**: the controller passes the params to
`OrderService`, which composes Spring Data JPA `Specification`s into a single SQL
`WHERE status IN (…) AND type IN (…) AND order_date = ?` (bound parameters).
URL-encode values with spaces / `&` (e.g. `Mobile%20%26%20Phone`).

### `GET /api/orders/meta`
Filter options derived from the data (`SELECT DISTINCT`, sorted) — for backend-driven dropdowns.

### `GET /api/orders/{id}`
One order by numeric primary key `id`. Absent → **404**; non-numeric id → **400**.

## Sample curl commands + responses (captured live from the running app)

```text
$ curl -s localhost:8080/api/orders | jq length
14

$ curl -s 'localhost:8080/api/orders?status=Completed'
[{"id":1,"orderId":"00001","name":"Christine Brooks","address":"089 Kutch Green Apt. 448","date":"2026-09-04","type":"Electronics","status":"Completed"},
 {"id":4,"orderId":"00004","name":"Gilbert Johnston","address":"768 Destiny Lake Suite 600","date":"2026-02-05","type":"Accessories","status":"Completed"},
 {"id":6,"orderId":"00006","name":"Alfred Murray","address":"543 Weimann Mountain","date":"2026-08-15","type":"Fashion & Beauty","status":"Completed"},
 {"id":13,"orderId":"00013","name":"Omar Haddad","address":"221 Elm Street","date":"2026-09-04","type":"Health & Medicine","status":"Completed"}]

$ curl -s 'localhost:8080/api/orders?status=Completed,On%20Hold' | jq -c '[length,(map(.status)|unique)]'
[6,["Completed","On Hold"]]

$ curl -s 'localhost:8080/api/orders?status=Completed,On%20Hold&status=Rejected' | jq -c '[length,(map(.status)|unique)]'
[8,["Completed","On Hold","Rejected"]]

$ curl -s 'localhost:8080/api/orders?type=Electronics,Accessories&type=Mobile%20%26%20Phone' | jq -c '[length,(map(.type)|unique)]'
[6,["Accessories","Electronics","Mobile & Phone"]]

$ curl -s 'localhost:8080/api/orders?type=Electronics&type=Mobile%20%26%20Phone' | jq -c '[length,(map(.type)|unique)]'
[4,["Electronics","Mobile & Phone"]]

$ curl -s 'localhost:8080/api/orders?status=Completed&type=Health%20%26%20Medicine&date=2026-09-04'
[{"id":13,"orderId":"00013","name":"Omar Haddad","address":"221 Elm Street","date":"2026-09-04","type":"Health & Medicine","status":"Completed"}]

$ curl -s 'localhost:8080/api/orders?date=2026-09-04' | jq -r '.[].orderId'
00001
00010
00013

$ curl -s -o /dev/null -w '%{http_code}\n' 'localhost:8080/api/orders?date=2026-13-45'
400

$ curl -s localhost:8080/api/orders/meta
{"types":["Accessories","Book & Stationary","Electronics","Fashion & Beauty","Health & Medicine","Home & Living","Mobile & Phone","Services & Industry"],"statuses":["Completed","In Transit","On Hold","Processing","Rejected"]}

$ curl -s localhost:8080/api/orders/9
{"id":9,"orderId":"00009","name":"Dollie Hines","address":"124 Lyla Forge Suite 975","date":"2026-01-09","type":"Electronics","status":"In Transit"}

$ curl -s -w '\n%{http_code}\n' localhost:8080/api/orders/999
{"timestamp":"2026-10-10T13:49:49.158+00:00","status":404,"error":"Not Found","path":"/api/orders/999"}
404
```

Full raw captures: `.specs/selftest-orders-20261010/evidence/curl-proof.txt`, `curl-proof-attempt-2.txt`.

## Notes
- No CORS headers are configured; a browser page on another origin should call the API
  through its dev-server proxy (or a CORS policy can be added in a follow-up).
