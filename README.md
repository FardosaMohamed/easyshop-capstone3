# 🛍️ EasyShop E-commerce API

This project provides a **RESTful API** for an e-commerce platform, managing products, user authentication, shopping carts, user profiles, and the ordering process. It's built with **Spring Boot** and **Spring JDBC** using a **MySQL** database.

---

## 1. 📦 Application Overview & Ordering Process

The EasyShop API serves as the robust backend for an online store. It handles all core e-commerce interactions, from user management and product browsing to order finalization.

### 🛒 The Ordering & User Process via API

#### 🔐 User Registration

```http
POST /register
```
Submit new user credentials and role.

#### 🔐 User Login

```http
POST /login
```
Authenticate and receive a JWT token for future authenticated requests.

#### 🛍️ Product Browsing

```http
GET /products
GET /categories/{categoryId}/products
```

#### 👤 User Profile 

```http
GET /profile
PUT /profile
```
- Retrieve and update the logged-in user's profile information.

#### 🛒 Shopping Cart 

```http
GET /cart
POST /cart/products/{productId}
PUT /cart/products/{productId}
DELETE /cart
```

#### ✅ Checkout & Orders

```http
POST /orders
```
- Finalize the cart and place a new order. The cart is cleared after checkout.

#### 🔧 Admin Functions

```http
POST /products, PUT /products/{id}, DELETE /products/{id}
POST /categories, PUT /categories/{id}, DELETE /categories/{id}
```
- Requires `ROLE_ADMIN` authentication.

---

## 2. 🔧 API Interaction and Testing with Postman

### ✅ Prerequisites

- Java 17+  
- Maven  
- MySQL with `easyshop.sql` loaded (including `orders` and `order_line_items`)  
- Run with:  
  ```bash
  mvn spring-boot:run
  ```

### 🧪 Postman Setup

1. Create a Collection and Environment.
2. Set environment variables:
   - `baseUrl`: `http://localhost:8080`
   - `adminToken`, `userToken`, `createdProductId`

---

## 🧪 Example API Interactions & Postman Tests

### A. 🔐 Admin Login

```http
POST {{baseUrl}}/login
```
**Body:**
```json
{
  "username": "admin",
  "password": "password"
}
```
**Tests:**
```js
pm.test("Status 200 OK", () => pm.response.to.have.status(200));
pm.environment.set("adminToken", pm.response.json().token);
```

---

### B. ➕ Create a Product (Admin)

```http
POST {{baseUrl}}/products
```

**Headers:**  
Authorization: Bearer {{adminToken}}  
Content-Type: application/json

**Body:**
```json
{
  "name": "Wireless Mouse",
  "description": "Ergonomic mouse with 2.4GHz wireless",
  "price": 29.99,
  "categoryId": 2,
  "color": "Black"
}
```

---

### C. ✏️ Update a Product (Admin)

```http
PUT {{baseUrl}}/products/{{createdProductId}}
```

**Body:**
```json
{
  "name": "Wireless Mouse Pro",
  "description": "Updated version with USB-C support",
  "price": 34.99,
  "categoryId": 2,
  "color": "Black"
}
```

---

### D. 🔍 Product Search

```http
GET {{baseUrl}}/products?cat=1&minPrice=80&maxPrice=90
```

---

### E. 👤 User Profile

**GET:**
```http
GET {{baseUrl}}/profile
```

**PUT:**
```http
PUT {{baseUrl}}/profile
```
**Body:**
```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane.doe@example.com",
  "address": "456 Oak Ave",
  "city": "Sampleville",
  "state": "TX",
  "zip": "75001",
  "phone": "555-987-6543"
}
```

---

### F. 🛒 Shopping Cart

**Add to Cart:**
```http
POST {{baseUrl}}/cart/products/{{someProductId}}
```

**Update Quantity:**
```http
PUT {{baseUrl}}/cart/products/{{productIdInCart}}
```
**Body:**
```json
{ "quantity": 2 }
```

**Clear Cart:**
```http
DELETE {{baseUrl}}/cart
```

---

### G. ✅ Checkout

```http
POST {{baseUrl}}/orders
```

---

## 3. 🧠 Interesting Code: Dynamic Product Search

The `search` method in `MySqlProductDao.java` dynamically builds SQL queries based on optional search filters.

### 📌 Why It's Interesting:

- **Flexible Query Construction**: Automatically builds `WHERE` clauses only for provided filters.  
- **SQL Injection Safe**: Uses `PreparedStatement` with parameter binding.  
- **Optimized Filtering**: Reduces unnecessary conditions, improving query efficiency.

### 🔍 Code Snippet:

```java
@Override
public List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String color)
{
    List<Product> products = new ArrayList<>();
    StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM products WHERE 1=1 ");
    List<Object> params = new ArrayList<>();

    if (categoryId != null && categoryId > 0) {
        sqlBuilder.append(" AND category_id = ? ");
        params.add(categoryId);
    }
    if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) >= 0) {
        sqlBuilder.append(" AND price >= ? ");
        params.add(minPrice);
    }
    if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) >= 0) {
        sqlBuilder.append(" AND price <= ? ");
        params.add(maxPrice);
    }
    if (color != null && !color.trim().isEmpty()) {
        sqlBuilder.append(" AND color = ? ");
        params.add(color.trim());
    }

    try (Connection connection = getConnection())
    {
        PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString());
        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            if (param instanceof Integer) statement.setInt(i + 1, (Integer) param);
            else if (param instanceof BigDecimal) statement.setBigDecimal(i + 1, (BigDecimal) param);
            else if (param instanceof String) statement.setString(i + 1, (String) param);
        }

        ResultSet row = statement.executeQuery();
        while (row.next()) products.add(mapRow(row));
    }
    catch (SQLException e)
    {
        System.err.println("ERROR: SQL Exception during product search: " + e.getMessage());
        throw new RuntimeException("Error searching products: " + e.getMessage(), e);
    }
    return products;
}
```

---
