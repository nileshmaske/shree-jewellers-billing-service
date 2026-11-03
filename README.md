# Shree Jewellers — Billing Service

Java 24 / Spring Boot 4 / Gradle / MySQL / Flyway / JWT resource server. Port **8083**.

## Setup
Create the database:
```sql
CREATE DATABASE IF NOT EXISTS shree_billing_db;
```
Set environment variables in IntelliJ:
- `DB_USERNAME`, `DB_PASSWORD`, optionally `DB_URL`
- `JWT_SECRET`: same HS256 secret as the Auth Service (minimum 32 UTF-8 bytes)
- `BILLING_GST_RATE`: optional, defaults to 3.00. **Verify the correct tax treatment for each sale before use.**

Run `gradle bootRun` with Java 24 and Gradle installed. The Gradle wrapper is not yet included.

## APIs (JWT required)
- `POST /api/billing/invoices` — create **draft** invoice
- `GET /api/billing/invoices` — paginated drafts/cancelled invoices
- `GET /api/billing/invoices/{id}` — detail
- `POST /api/billing/invoices/{id}/cancel` — cancel draft (ADMIN/MANAGER)

GET and draft creation: ADMIN, MANAGER, CASHIER.

Example request:
```json
{
 "customerName":"Sample Customer",
 "customerPhone":"9999999999",
 "discount":500.00,
 "items":[{"productId":1,"sku":"SJ-GOLD-0001","description":"22K Gold Necklace","quantity":1,"unitPrice":180000.00}]
}
```

## Important boundaries
This is a **draft-only billing foundation**. It calculates subtotal, discount, configurable GST, and grand total server-side using BigDecimal. It **does not** finalize sales, collect payments, validate inventory prices, reserve stock, mark products SOLD, or generate tax-compliant invoices. Item prices are supplied by the caller and are not trusted for final sale. Draft numbers are internal identifiers, not final invoice numbers. Do not issue these drafts to customers as final bills.

Before enabling final invoices, implement authoritative inventory lookup, price validation, atomic stock reservation/consumption with idempotency, payment reconciliation, finalized invoice numbering, GST-compliant tax logic, and PDF printing.

## MSG91 SMS (optional)
SMS sending is disabled by default. Configure in IntelliJ environment variables:
- `MSG91_SMS_ENABLED=true`
- `MSG91_AUTH_KEY` — keep secret, never commit
- `MSG91_FLOW_ID` — approved MSG91 Flow template
- `MSG91_SENDER_ID` — approved sender
- `MSG91_COUNTRY_CODE=91` (default)

`POST /api/billing/invoices/{id}/send-sms` with JWT and JSON `{"phone":"9876543210"}` submits a **draft estimate** SMS to MSG91. Template variables `VAR1` and `VAR2` represent the draft reference and estimated total. Configure the approved MSG91 template to clearly state **DRAFT ESTIMATE — NOT A TAX INVOICE**, and align variable names with your actual Flow configuration. MSG91 acceptance does not guarantee handset delivery. Use only for customers who consented to SMS; apply DLT/template rules and delivery logging/rate limits before production. Email is not configured.
