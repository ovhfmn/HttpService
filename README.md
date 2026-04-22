# Scala HTTP Account Service

A functional backend service built with **Scala 3**, demonstrating clean architecture, functional programming, and real-world backend patterns.

---

## 🚀 Overview

This project implements a simple **account management system** with:

* Account creation
* Credit / debit operations
* Validation of business rules (no overdrafts, no duplicates)
* HTTP API with JSON support
* Pluggable persistence (In-Memory + PostgreSQL)

---

## 🧠 Goals

* Demonstrate **functional programming in Scala**
* Apply **clean architecture principles**
* Model a **type-safe domain**
* Build a **testable and maintainable backend service**

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
Repository (InMemory / Postgres)
   ↓
Domain (pure logic)
```

---

## 🧩 Key Concepts

### Functional Effects

* `IO` for managing side effects

### Error Handling

* `Either[DomainError, A]`
* `EitherT[IO, DomainError, A]`

### State Management

* `Ref[IO, Map[AccountId, Account]]` (in-memory)
* PostgreSQL via Doobie

### Type Safety

* Opaque types:

  * `AccountId`
  * `Money`
  * `Balance`

### Separation of Concerns

* Domain (pure logic)
* Service (business orchestration)
* Repository (state / persistence)
* HTTP (transport layer)

---

## 📌 API Endpoints

### Create Account

```http
POST /accounts
```

```json
{
  "id": "acc1",
  "balance": 100
}
```

---

### Debit Account

```http
POST /accounts/{id}/debit
```

```json
{
  "amount": 10
}
```

---

### Credit Account

```http
POST /accounts/{id}/credit
```

```json
{
  "amount": 10
}
```

---

### Health Check

```http
GET /health
```

---

## 🧪 Testing

Covers multiple layers:

* **Domain tests** → pure business rules
* **Service tests** → orchestration logic
* **HTTP tests** → endpoint behavior

Run:

```bash
sbt test
```

---

## ▶️ Running the Application

```bash
sbt run
```

Then access:

```
http://localhost:8010/health
```

---

## 🗄 Database

* PostgreSQL supported via Doobie
* Default connection:

  * `jdbc:postgresql://localhost:5432/postgres`
  * user: `postgres`
  * password: `postgres`

---

## 🛠 Example

```bash
curl -X POST http://localhost:8010/accounts \
  -H "Content-Type: application/json" \
  -d '{"id":"acc1","balance":100}'
```

```bash
curl -X POST http://localhost:8010/accounts/acc1/debit \
  -H "Content-Type: application/json" \
  -d '{"amount": 10}'
```