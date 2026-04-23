# Scala HTTP Account Service

A functional backend service built with **Scala 3**, demonstrating clean architecture, functional programming, and real-world backend patterns.

---

## 🚀 Overview

This project implements a simple **account management system** with:

* Account creation
* Credit / debit operations
* Validation of business rules (no overdrafts, no duplicates)
* HTTP API with JSON support
* PostgreSQL persistence with optimistic locking

---

## 🧠 Goals

* Demonstrate **functional programming in Scala**
* Apply **clean architecture principles**
* Model a **type-safe domain**
* Build a **testable and maintainable backend service**
* Showcase **transactional correctness and concurrency handling**

---

## 🏗 Architecture

```
Client
   ↓
HTTP (http4s)
   ↓
Routes
   ↓
Service (EitherT[IO, DomainError, A])
   ↓
Repository (Doobie / PostgreSQL)
   ↓
Domain (pure logic)
```

---

## 🧩 Key Concepts

### Functional Effects

* `IO` for managing side effects
* `ConnectionIO` for database programs

### Error Handling

* `Either[DomainError, A]`
* `EitherT[IO, DomainError, A]`
* Mapping to HTTP responses

### Persistence

* PostgreSQL via Doobie
* Transactions via `transact`
* Optimistic locking using `version`

### Type Safety

* Opaque types:

  * `AccountId`
  * `Money`
  * `Balance`

---

## 🧠 Key Design Decisions

### Why Cats Effect instead of Future?

* Referential transparency
* Composable effects
* Structured concurrency

### Why http4s?

* Pure functional ecosystem
* Seamless integration with Cats Effect
* Minimal abstraction overhead

### Why Doobie?

* Type-safe SQL
* Full control over queries
* Better transparency vs ORMs

### Why optimistic locking?

* Prevents lost updates
* Detects concurrent modifications
* Ensures consistency in multi-request scenarios

---

## 🐘 PostgreSQL Setup & Usage

### 🔐 Connect

```bash
psql -U postgres -W
```

---

### 🧱 Create Database

```sql
CREATE DATABASE account_service;
```

```bash
\c account_service
```

---

### 📦 Create Table

```sql
CREATE TABLE accounts (
  id TEXT PRIMARY KEY,
  balance NUMERIC NOT NULL,
  version BIGINT NOT NULL
);
```

---

### ➕ Insert Data

```sql
INSERT INTO accounts (id, balance, version)
VALUES ('acc1', 100, 0);
```

---

### 🔍 Query Data

```sql
SELECT * FROM accounts;
```

---

### 📋 Show Tables

```bash
\dt
```

---

### 🧠 Inspect Schema

```bash
\d accounts
```

---

### 🧹 Reset Table

```sql
DROP TABLE IF EXISTS accounts;

CREATE TABLE accounts (
  id TEXT PRIMARY KEY,
  balance NUMERIC NOT NULL,
  version BIGINT NOT NULL
);
```

---

### ❌ Exit

```bash
\q
```

---

## ⚙️ PostgreSQL Service (Linux)

```bash
sudo systemctl status postgresql
sudo systemctl start postgresql
sudo systemctl restart postgresql
```

---

## ▶️ Running the Application

```bash
sbt run
```

Health check:

```
http://localhost:8010/health
```

---

## 🧪 Example API Usage

### Create Account

```bash
curl -X POST http://localhost:8010/accounts \
  -H "Content-Type: application/json" \
  -d '{"id":"acc1","balance":100}'
```

---

### Debit

```bash
curl -X POST http://localhost:8010/accounts/acc1/debit \
  -H "Content-Type: application/json" \
  -d '{"amount":10}'
```

---

### Credit

```bash
curl -X POST http://localhost:8010/accounts/acc1/credit \
  -H "Content-Type: application/json" \
  -d '{"amount":10}'
```

---

## ❗ Example Errors

### Duplicate Account

```json
{
  "error": "AccountAlreadyExists",
  "message": "Account acc1 already exists"
}
```

---

### Concurrent Modification

```json
{
  "error": "ConcurrentModification",
  "message": "Account acc1 was modified concurrently"
}
```

---

### Invalid Input

```json
{
  "error": "InvalidAmount",
  "message": "Amount must be positive"
}
```

---

## 🪵 Logging

* Uses **SLF4J + Logback**
* Logs written to:

```
logs/app.log
```

* Includes:

  * request actions
  * errors
  * state transitions

---

## 🧪 Testing Strategy

### Domain Tests

~~* Pure logic~~
~~* No side effects
* Fast and deterministic
~~
### Integration Tests (recommended)

~~* Use real PostgreSQL (Testcontainers)
* Validate SQL + transactions
~~
---

## ⚠️ Limitations

* No authentication / authorization
* No request validation at HTTP boundary
* No pagination or advanced querying
* Single-node DB assumption
* No metrics or tracing yet

---

## 🚀 Future Improvements

* Structured logging (JSON)
* Request correlation IDs
* Testcontainers integration
* AppError vs DomainError separation
* Config via environment (PureConfig or alternative)
* Observability (metrics, tracing)

---

## 📌 Summary

This project demonstrates:

* Functional Scala backend development
* Real-world database interaction with Doobie
* Transactional correctness
* Optimistic locking for concurrency safety
* Clean architecture and separation of concerns

---
