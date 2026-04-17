# Scala HTTP Account Service

A functional backend service built with **Scala 3**, demonstrating clean architecture, functional programming, and real-world backend patterns.

---

## Overview

This project implements a simple **account management system** with the ability to:

* Create accounts
* Credit accounts
* Debit accounts
* Prevent invalid operations (e.g. overdraft, duplicates)

The goal of the project is to showcase:

* Functional programming in Scala
* Clean architecture (domain → service → repository → HTTP)
* Type-safe design
* Testable and maintainable code

---

## Architecture

```
Client
   ↓
HTTP (http4s)
   ↓
Routes
   ↓
Service (EitherT + IO)
   ↓
Repository (Ref)
   ↓
Domain (pure logic)
```

---

## Key Concepts Used

### Functional Effects

* `IO` for managing side effects

### Error Handling

* `Either[DomainError, A]`
* `EitherT[IO, DomainError, A]` for composition

### State Management

* `Ref[IO, Map[AccountId, Account]]` as an in-memory database

### Type Safety

* Opaque types:
    * `AccountId`
    * `Money`
    * `Balance`

### Separation of Concerns

* Domain (pure logic)
* Service (business rules)
* Repository (state)
* HTTP (transport layer)

---

## 📌 API Endpoints

### Create Account

```http
POST /accounts
```

**Request:**

```json
{
  "id": "acc1",
  "initialBalance": 100
}
```

---

### Debit Account

```http
POST /accounts/{id}/debit
```

**Request:**

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

**Request:**

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

## Testing

The project includes tests for multiple layers:

* **Domain tests** – validation and business rules
* **Service tests** – orchestration logic
* **HTTP tests** – endpoint behavior

Run tests with:

```bash
sbt test
```

---

## Running the Application

```bash
sbt run
```

Then access:

```
http://localhost:{PORT}/health
```

---

## 📌 Example (curl)

```bash
curl -X POST http://localhost:{PORT}/accounts \
  -H "Content-Type: application/json" \
  -d '{"id":"acc1","initialBalance":100}'
```

```bash
curl -X POST http://localhost:8010/accounts/{account_id}/debit \
  -H "Content-Type: application/json" \
  -d '{"amount": 10}'
```

---