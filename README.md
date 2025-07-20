# Translation Management Service

## 📌 Overview
The *Translation Management Service* is a Spring Boot-based microservice that provides translation management. It includes *JWT authentication, **H2 database for testing, and **Swagger API documentation*.

## 🚀 Features
- 🌍 Translation Management API
- 🔐 JWT-based Authentication & Authorization
- 🗄 H2 Database for Development
- 📝 Swagger UI for API Documentation
- 🐳 Docker Support

---

## 🔧 *Tech Stack*
- *Java 21*
- *Spring Boot 3.5.3*
- *Spring Security (JWT)*
- *Spring Data JPA*
- *H2 Database (for Development)*
- *Maven*
- *Docker*

---

## 🏗 *Project Setup*
### ⿡ Clone Repository
git clone https://github.com/sanaullah-techie/translation-management-system.git
## *Navigate to Project Directory*
cd translation-management-system
### *Build Docker Image*
docker build -t tms-image .
### *Run the Docker Container*

docker run -p 8080:8080 -d --name tms-container tms-image
---
### *Accessing the Application Locally*
Visit: http://localhost:8080/swagger-ui/index.html

Since the endpoints are protected by JWT authentication, users must first register and obtain a token to access protected APIs.

## *Steps to use the Swagger UI*

1 Open the Swagger UI in your browser.
2 Register a new user by providing a username and password via the /api/auth/register endpoint.
 After registration, you will receive a JWT token in the response.
3 Click on the "Authorize" button in Swagger UI and paste the received token.
4 Once authorized, you can access the protected endpoints, such as:
  Create a new translation
  Search for translations

<img width="1011" height="467" alt="image" src="https://github.com/user-attachments/assets/a81f6b14-e982-4bd3-8237-8b5103e2b453" />


<img width="1891" height="616" alt="img2" src="https://github.com/user-attachments/assets/16495ce4-1003-44e3-9445-2eef15ab292b" />






