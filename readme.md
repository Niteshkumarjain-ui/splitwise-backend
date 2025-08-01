# 🧾 Splitwise Clone - Backend

A backend system for managing shared group expenses, similar to Splitwise. Built using **Java**, **Spring Boot**, **PostgreSQL**, and **JWT**. The project supports user authentication, group and expense management, balance tracking, and simplified debt resolution. Includes interactive API docs via **Swagger**.

---

## 🚀 Features

- ✅ User Registration & Login (JWT Authentication)
- 👥 Create and manage Groups
- 👨‍👩‍👧‍👦 Add/Remove Members from Groups
- 💸 Add/Edit/Delete Expenses (Equally Split)
- 📊 User Dashboard (Overall Balances)
- 📂 Group Dashboard (Members, Expenses, Balances)
- 💰 Simplified Debt Calculations
- 🤝 Settle Up Functionality
- 🔐 Secured APIs using JWT
- 📘 Swagger UI for API Documentation

---

## 📦 Tech Stack

| Layer     | Technology           |
|----------|----------------------|
| Backend   | Java 17, Spring Boot |
| Database  | PostgreSQL           |
| Security  | Spring Security + JWT |
| ORM       | Hibernate / JPA      |
| API Docs  | Swagger (SpringDoc OpenAPI) |
| Build Tool| Maven                |

---

## 📁 Folder Structure

```
com.splitwise
├── config           # Open Api & Security config
├── controller       # API endpoints
├── service          # Interfaces + Implementations
├── model            # JPA Entity classes
├── dto              # Request/Response payloads
├── repository       # Database access layer
├── exception        # Custom & Global exception handler
├── security         # JWT, token utilites
└── SplitwiseApp     # Spring Boot main class

```

---

## ⚙️ Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/Niteshkumarjain-ui/splitwise-backend.git
cd splitwise-backend
```

### 2. Configure the Application

Update your `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/splitwise
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update
```

### 3. Run the Application

```bash
./mvnw spring-boot:run
```

---

## 🔐 Authentication Flow

- `POST /api/auth/signup` → Register
- `POST /api/auth/login` → Receive JWT
- Add JWT token in header for all protected endpoints:
  ```
  Authorization: Bearer <token>
  ```

---

## 📘 Swagger API Documentation

Once the server is running, access the **Swagger UI** at:

```
http://localhost:8080/swagger-ui/index.html
```

Or the OpenAPI spec:

```
http://localhost:8080/v3/api-docs
```

---

## 🧪 Postman Testing

1. Login using `/api/auth/login` and get the token
2. Set Header for each request:
   ```
   Key: Authorization
   Value: Bearer <your_token>
   ```
3. Use the remaining APIs to manage groups, expenses, and settle.

---

## 🧮 Simplified Debt Calculation

Group dashboard includes:
- Each member’s current balance
- Simplified debt resolution (minimized # of transactions)
- Net balances showing who owes whom

---

## 🛠️ Future Enhancements

- 📲 Push/Email notifications
- 📆 Recurring Expenses
- 📅 Monthly Report
- 🏷️ Expense Categories
- 🔎 Filters & Search
- 👫 Friend Requests and Private Groups

---

## 📄 License

Licensed under the MIT License.

---

## 👤 Author

**Nitesh Kumar Jain**  
Backend Developer – Java | Spring Boot | PostgreSQL  
[GitHub](https://github.com/Niteshkumarjain-ui)