<<<<<<< HEAD
# AgroShop - Hardware and farm supplies shop

Spring Boot 4 + MySQL + plain HTML. Covers Product and Inventory, login with roles, and Sales and Payment.

## Run
1. Run `Agroshop.sql` in MySQL (creates `Agro_Shop_db` with sample data).
2. Set your DB password as an environment variable: `DB_PASSWORD=yourpassword` (optional: `DB_USERNAME`, `DB_URL`).
3. `./mvnw spring-boot:run`, then open http://localhost:8081

## Sample logins
On first start the placeholder passwords in the SQL are converted to BCrypt (the `hash_` prefix is dropped).

| Role | Username | Password |
|---|---|---|
| Admin | admin_user | admin123 |
| Employee | saman_emp | emp123 |
| Customer | kaveesha_p, nimal_s, sunil_f | cust1, cust2, cust3 |

## Who can do what
| | Admin | Employee | Customer |
|---|---|---|---|
| Browse catalog | yes | yes | yes (even signed out) |
| Add, edit, remove products and categories | yes | no | no |
| Change stock, see low stock | yes | yes | no |
| Counter sale (cash or card) | yes | yes | no |
| See and update all orders, confirm bank transfers | yes | yes | no |
| Cart, place order, see own orders | no | no | yes |

## Order flow
- Customer: cart (saved in the browser) -> checkout with cash on delivery or bank transfer -> order PENDING, payment PENDING.
- Staff confirm a bank transfer -> payment COMPLETED, order PROCESSING. Cash on delivery turns COMPLETED when the order is marked DELIVERED.
- Cancelling an order returns the stock and marks the payment FAILED (or REFUNDED if it was already paid).
- Counter sale: order DELIVERED and payment COMPLETED immediately, recorded under the "Walk-in Customer".
- Prices and stock are always checked on the server. Stock is locked while an order is being placed, so two buyers cannot take the same last item.

## API
| Method | URL | Who |
|---|---|---|
| POST | /api/auth/login, /api/auth/logout; GET /api/auth/me | everyone |
| GET | /api/products, /api/categories | everyone |
| POST/PUT/DELETE | /api/products, /api/categories | admin |
| PUT | /api/products/{id}/stock?quantityChange=-3 ; GET /api/products/low-stock | admin, employee |
| POST | /api/orders ; GET /api/orders/my | customer |
| POST | /api/orders/sale ; GET /api/orders ; PUT /api/orders/{id}/status?status=SHIPPED ; PUT /api/orders/{id}/payment/confirm | admin, employee |
=======
# ooad
>>>>>>> origin/feature/supplier-purchasing-integration
