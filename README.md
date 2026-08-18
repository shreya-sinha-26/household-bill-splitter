# Household Bill Splitter

Split household expenses equally among group members, show each person’s net balance, and suggest the fewest transfers to settle up.

The backend stores groups, members, and bills. Every bill is split among **all current members** of that group. Shares are rounded to paise, leftover paise are given out so the shares always sum to the bill amount **exactly**. The UI is a small React app for listing groups, adding bills, and reading balances.

Seed data on startup: **Room 12B** (Alice, Bob, Charlie — includes a ₹100.00 bill that does not divide evenly by 3) and **Sharma Family** (Diya, Eshan).

## Tech stack

| Layer | Choice |
|---|---|
| Backend | Java 17, Spring Boot 3.2.x, Maven |
| Persistence | Spring Data JPA, H2 in-memory |
| Validation | `jakarta.validation` on request DTOs |
| Frontend | React 18, Vite, plain JavaScript |
| HTTP | axios |
| Routing | react-router-dom v6 |
| Styling | one `index.css` (no Tailwind, no MUI) |

Money on the backend is always `BigDecimal`. Never `double` / `float`.

## Prerequisites

- JDK 17+ (`JAVA_HOME` pointing at a JDK, not only a JRE)
- Node.js 18+ and npm (for the frontend)
- Maven is **not** required: `backend/` ships the Maven Wrapper (3.9.9)

## How to run the backend

From `backend/`:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

API base URL: [http://localhost:8080](http://localhost:8080)

The first run downloads Maven 3.9.9 into `~/.m2/wrapper` (or `%USERPROFILE%\.m2\wrapper`).

## How to run the frontend

From `frontend/`:

```bash
npm install
npm run dev
```

UI: [http://localhost:5173](http://localhost:5173)

`frontend/.env` sets `VITE_API_BASE_URL=http://localhost:8080`. CORS on the backend allows that origin.

## H2 console

| Setting | Value |
|---|---|
| URL | [http://localhost:8080/h2-console](http://localhost:8080/h2-console) |
| JDBC URL | `jdbc:h2:mem:billsplitter` |
| User | `sa` |
| Password | *(blank)* |

Hibernate `ddl-auto: update` creates tables from the entities. `data.sql` then inserts the two seed groups.

The group table is named `household_group` because `GROUP` is a reserved SQL keyword.

## API endpoints

| Method | Path | Status | Body |
|---|---|---|---|
| POST | `/api/groups` | 201 | `GroupResponse` |
| GET | `/api/groups?page=0&size=10&search=` | 200 | `PageResponse<GroupSummaryResponse>` |
| GET | `/api/groups/{id}` | 200 | `GroupResponse` |
| PUT | `/api/groups/{id}` | 200 | `GroupResponse` |
| DELETE | `/api/groups/{id}` | 204 | empty |
| POST | `/api/groups/{groupId}/bills` | 201 | `BillResponse` |
| GET | `/api/groups/{groupId}/bills?page=0&size=10&paidById=&from=&to=&sort=date,desc` | 200 | `PageResponse<BillResponse>` |
| GET | `/api/bills/{id}` | 200 | `BillResponse` |
| PUT | `/api/bills/{id}` | 200 | `BillResponse` |
| DELETE | `/api/bills/{id}` | 204 | empty |
| GET | `/api/groups/{groupId}/balances` | 200 | `List<MemberBalanceResponse>` |
| GET | `/api/groups/{groupId}/settlements` | 200 | `List<SettlementResponse>` |

Errors all use the same JSON shape:

```json
{
  "timestamp": "2026-08-18T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/groups/1/bills",
  "fieldErrors": { "amount": "must be greater than 0" }
}
```

| Status | When |
|---|---|
| 400 | Malformed JSON, bean validation, bad date, bad query type |
| 404 | Group, bill, or member id does not exist |
| 409 | Domain rule (payer not in the group, duplicate member name, removing a member who has paid bills) |
| 500 | Anything else (stack trace logged, not returned) |

## Sample curl commands

Create a group:

```bash
curl -s -X POST http://localhost:8080/api/groups \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Room 12B\",\"members\":[\"Alice\",\"Bob\",\"Charlie\"]}"
```

List groups:

```bash
curl -s "http://localhost:8080/api/groups?page=0&size=10&search=Room"
```

Add a bill (use a real `paidById` from the group):

```bash
curl -s -X POST http://localhost:8080/api/groups/1/bills \
  -H "Content-Type: application/json" \
  -d "{\"description\":\"Weekly groceries\",\"amount\":100.00,\"paidById\":1,\"date\":\"2026-08-05\"}"
```

List bills with filters:

```bash
curl -s "http://localhost:8080/api/groups/1/bills?page=0&size=10&paidById=1&from=2026-08-01&to=2026-08-31&sort=date,desc"
```

Balances and settlements (seed group 1):

```bash
curl -s http://localhost:8080/api/groups/1/balances
curl -s http://localhost:8080/api/groups/1/settlements
```

## How to run tests

From `backend/`:

```bash
./mvnw test
```

Windows:

```powershell
.\mvnw.cmd test
```

`SplitCalculatorTest` checks leftover-paise shares sum to the bill amount, net balances sum to zero, zero members, and zero bills. `BillEntryServiceTest` checks 404 vs 409 for `paidById`.

## Design decisions

### Rounding strategy

Each bill is split equally among the **current** member count.

1. `share = amount / memberCount`, scale 2, `RoundingMode.DOWN`.
2. `share * memberCount` is often less than `amount` (₹100 / 3 → 33.33 × 3 = 99.99).
3. Leftover paise = `amount - (share * memberCount)` in paise.
4. Add **₹0.01** to the first N members ordered by `member.id` ascending.

Example: ₹100.00 among ids 1, 2, 3 → **33.34, 33.33, 33.33**. The sum is exactly ₹100.00. Unit tests assert that.

Per member across the group:

- `totalPaid` = sum of bills they paid
- `totalShare` = sum of their share of every bill
- `netBalance` = `totalPaid - totalShare` (positive = owed, negative = owes)

Because every bill’s shares sum to that bill, **the sum of all net balances is zero**.

Settlements use a greedy min-transfer: largest debtor with largest creditor, transfer the min of the two, drop whoever hits zero.

### Why `paidBy` is a foreign key

`BillEntry.paidBy` is a `ManyToOne` to `Member`, not a free-text name. A name can be typo’d or left behind after a rename. An FK means:

- the payer is a real member row
- the service can reject a `paidById` that belongs to another group (**409**)
- you cannot remove a member who has paid bills without a deliberate **409**

The API still accepts `paidById` on the request DTO; the service loads the `Member` and checks `member.group.id`.

### Retroactive members (deliberate)

There is **no per-bill membership snapshot**. Balances always split every stored bill among whoever is in the group **now**.

If a member is added after bills exist, they inherit a share of those old bills, and everyone else’s share drops. That is intentional: it keeps the model to three tables and matches the rule “split equally among all current members.” The alternative (historical membership) would need a join table per bill and was out of scope.

Edge cases handled without crashing:

- **Zero members** → empty balance list (no divide-by-zero)
- **Zero bills** → every member’s paid / share / net is 0.00
