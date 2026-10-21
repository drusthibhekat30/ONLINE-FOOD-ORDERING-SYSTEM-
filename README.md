# Saffron & Stone | Online Food Ordering

A responsive food ordering system built with Spring Boot, Thymeleaf, JDBC, and MySQL-compatible SQL. It follows MVC with a DAO layer. H2 is used for a zero-setup local demo; the MySQL profile is ready for a local database.

## Run locally

Requirements: Java 17+ and Maven 3.9+.

```bash
mvn spring-boot:run
```

Open http://localhost:8080. The seeded demo administrator is `admin@saffron.test` / `Admin123!`.

To use MySQL, create a database named `food_ordering`, set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`, then run:

```bash
SPRING_PROFILES_ACTIVE=mysql mvn spring-boot:run
```

The schema is initialized on startup. Change the seeded admin password before deployment. Configure `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, and `MAIL_PASSWORD` to enable order confirmation email. UPI is a mock payment choice; no payment is collected.

## Features

- Registration/login, BCrypt password hashing, session-backed identity/cart/favourites
- Category, dish-name, diet, price, and rating filters
- Quantity controls, live bag subtotal, checkout, delivery address, COD/UPI mock, order history and status
- Admin menu CRUD, order status management, customer listing
- Ratings/reviews, favourites, coupon codes (`WELCOME10`, `FEAST15`), and contact form
- Responsive Bootstrap-based UI

## Architecture and report

`controller` handles HTTP/MVC routes, `dao` owns prepared JDBC queries, and Thymeleaf templates render views. `schema.sql` creates the requested tables plus reviews, contacts, and coupons. See [the project report](docs/PROJECT_REPORT.md) for ER, DFD, use-case diagrams, screenshot checklist, and future scope.

```bash
mvn test
```
