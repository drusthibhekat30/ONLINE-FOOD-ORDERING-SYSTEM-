# Project Report: Saffron & Stone

## Overview

An online restaurant ordering application with Spring MVC controllers, Thymeleaf views, JDBC DAO access, and an H2/MySQL database. Customers browse a menu, manage a session cart, order, and follow progress. Administrators manage the menu and order queue.

## ER diagram

```mermaid
erDiagram
  USERS ||--o| ADMIN : has
  USERS ||--o{ CART : owns
  FOOD_ITEMS ||--o{ CART : contains
  USERS ||--o{ ORDERS : places
  ORDERS ||--|{ ORDER_ITEMS : includes
  FOOD_ITEMS ||--o{ ORDER_ITEMS : snapshots
  USERS ||--o{ REVIEWS : writes
  FOOD_ITEMS ||--o{ REVIEWS : receives
  COUPONS { string code PK; int discount_percent; boolean active }
  CONTACTS { bigint id PK; string name; string email; string message }
```

## Data-flow diagram

```mermaid
flowchart LR
  Customer -->|browse, account, cart, checkout| WebApp[Spring MVC application]
  Admin -->|menu and order actions| WebApp
  WebApp -->|prepared JDBC queries| DB[(MySQL / H2 database)]
  DB -->|menu, users, orders| WebApp
  WebApp -->|confirmation when SMTP is configured| Email[Email service]
  WebApp -->|HTML views| Customer
  WebApp -->|admin dashboard| Admin
```

## Use-case diagram

```mermaid
flowchart TB
  Customer([Customer])
  Administrator([Administrator])
  Browse((Browse and filter menu))
  Account((Register / sign in))
  Cart((Manage cart and favourites))
  Checkout((Checkout and place order))
  Track((View order history and status))
  Review((Rate and review dishes))
  Contact((Send feedback))
  Menu((Manage menu items))
  Queue((Update order status))
  Users((View customers))
  Customer --- Browse
  Customer --- Account
  Customer --- Cart
  Customer --- Checkout
  Customer --- Track
  Customer --- Review
  Customer --- Contact
  Administrator --- Menu
  Administrator --- Queue
  Administrator --- Users
```

## Screenshots

Start the app and capture these pages into `docs/screenshots/`: menu and bag (`/`), sign-in/register (`/account`), checkout (`/checkout`), order history (`/orders`), admin studio (`/admin`), and contact (`/contact`). The admin page requires the demo administrator. Do not include real customer data.

## Demo and coupons

- Admin: `admin@saffron.test` / `Admin123!`
- Coupon codes: `WELCOME10` and `FEAST15`
- COD and UPI are demo selections; no payment is collected.

## Future scope

- Live delivery tracking with driver location and status notifications
- Payment gateway integration with verified callbacks and refunds
- Mobile app with push notifications and saved addresses
- Persistent cross-device carts and personalized recommendations
- Email delivery events, account recovery, and rate limiting