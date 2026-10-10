# Orders API

Spring Boot 3 / Java 21, base URL `http://<host>:8080` (listens on 0.0.0.0:8080).

## SQLite setup
Embedded SQLite file `/workspace/data/app.db` (`mkdir -p /workspace/data` first), configured in
`application.properties` (sqlite-jdbc + Hibernate community `SQLiteDialect`, `ddl-auto=update`).
`OrderSeeder` inserts 14 rows on startup if the table is empty.

Run: `mkdir -p /workspace/data && nohup mvn -q -DskipTests spring-boot:run > /tmp/backend.log 2>&1 &`

## Order object
`{ id, orderId, name, address, date (yyyy-MM-dd), type, status }`
- type ∈ Accessories, Mobile & Phone, Electronics, Home & Living, Fashion & Beauty, Services & Industry, Book & Stationary, Health & Medicine
- status ∈ In Transit, On Hold, Rejected, Processing, Completed

## Endpoints
### GET /api/orders
All orders (JSON array, ordered by id). Optional, combinable filters, applied in the DB query:
- `status` — repeatable or CSV (`status=Completed,On Hold` or `status=Completed&status=On Hold`)
- `type` — repeatable or CSV
- `date` — exact `YYYY-MM-DD`

```
$ curl -s "localhost:8080/api/orders?status=Completed,On%20Hold&type=Electronics&type=Accessories"
[{"id":1,"orderId":"00001","name":"Alice Johnson","address":"12 Oak St, Austin TX","date":"2026-09-01","type":"Accessories","status":"Completed"},
 {"id":9,"orderId":"00009","name":"Isla Martin","address":"5 Maple Way, Atlanta GA","date":"2026-09-07","type":"Electronics","status":"Completed"},
 {"id":10,"orderId":"00010","name":"Jack Davis","address":"62 Birch St, Dallas TX","date":"2026-09-08","type":"Accessories","status":"On Hold"}]

$ curl -s "localhost:8080/api/orders?status=Completed"   # 4 rows, all Completed
$ curl -s "localhost:8080/api/orders?date=2026-09-08"    # orderIds 00010, 00011
```

### GET /api/orders/meta
Filter options derived from the data.
```
$ curl -s localhost:8080/api/orders/meta
{"types":["Accessories","Book & Stationary","Electronics","Fashion & Beauty","Health & Medicine","Home & Living","Mobile & Phone","Services & Industry"],"statuses":["Completed","In Transit","On Hold","Processing","Rejected"]}
```

### GET /api/orders/{id}
```
$ curl -s localhost:8080/api/orders/9
{"id":9,"orderId":"00009","name":"Isla Martin","address":"5 Maple Way, Atlanta GA","date":"2026-09-07","type":"Electronics","status":"Completed"}
$ curl -s -o /dev/null -w "%{http_code}" localhost:8080/api/orders/999
404
```

## Tests
`mvn test` — `OrderRepositoryTest` (@DataJpaTest on SQLite): round-trip + status/type/date filters.
