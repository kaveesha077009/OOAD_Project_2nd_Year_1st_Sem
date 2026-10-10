# AgroShop - Product and Inventory module

Spring Boot 4 + MySQL + plain HTML staff panel.

## Run
1. Run `Agroshop.sql` in MySQL (creates `Agro_Shop_db` with sample data).
2. Set your DB password: `DB_PASSWORD=yourpassword` (environment variable; optional `DB_USERNAME`, `DB_URL`).
3. `./mvnw spring-boot:run` then open http://localhost:8081

## API
| Method | URL | Purpose |
|---|---|---|
| GET/POST | /api/categories | list / add |
| PUT/DELETE | /api/categories/{id} | edit / remove |
| GET/POST | /api/products | list / add (also creates stock record) |
| PUT/DELETE | /api/products/{id} | edit / remove |
| PUT | /api/products/{id}/stock?quantityChange=-3 | add or take out stock |
| GET | /api/products/low-stock | products at or below alert level |
