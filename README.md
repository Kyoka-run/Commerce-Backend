# E-Commerce Web Application

## 🎯 Project Overview
This is a full-stack e-commerce platform built with Spring Boot and React, featuring secure user authentication, product management, shopping cart functionality, and payment processing integration.

The project consists of two main repositories:
- Backend Repository: [`Backend`][backend]
- Frontend Repository: [`Frontend`][frontend]

Try application here:
[`Commerce`][demo]

[backend]: https://github.com/Kyoka-run/Commerce-Backend
[frontend]: https://github.com/Kyoka-run/Commerce-Frontend
[demo]: http://kyoka-ecommerce.s3-website-eu-west-1.amazonaws.com/

## ⚙️ Technology Stack

### Back-end
- **Framework:** Spring Boot 3.4.1
- **Security:** Spring Security with JWT
- **Database:** MySQL with JPA/Hibernate, RDS for development
- **Payment Processing:** Stripe API
- **Testing:** JUnit, Mockito
- **Build Tool:** Maven

### Front-end
- **Framework:** React 18.3.1 with Vite 5.3.1
- **State Management:** Redux Toolkit
- **UI Styling:** Tailwind CSS 3.4.17, Material UI 6.3.1
- **Form Handling:** React Hook Form 7.54.2
- **HTTP Client:** Axios 1.7.9
- **Testing:** Vitest 3.0.9, React Testing Library 16.2.0
- **UI Notifications:** React Hot Toast 2.5.1

## ✨ Key Features

### User Authentication & Authorization
- **JWT-based authentication**
- **Secure registration and login system**

### Product Management
- **Product catalog** with categories
- **Product search and filtering** capabilities
- **Image upload for products**

### Shopping Experience
- **Intuitive product browsing**
- **Real-time cart management**
- **Responsive design**

### Order Processing
- **Order history and tracking**


## 📊 Testing and Code Quality

- Extensive test coverage for both backend and frontend
- Unit tests with JUnit and Mockito for backend services
- Component testing with Vitest and React Testing Library
- Integration tests for API endpoints

## 🛠️ Installation & Setup

### Prerequisites
- Java 17+
- Node.js 18+
- MySQL 8.0+
- Stripe API key (for payment processing)

### Backend Setup
1. Clone the repository:
```bash
git clone https://github.com/yourusername/e-commerce.git
cd e-commerce
```
2. Set up the MySQL database:
```bash
CREATE DATABASE commerce;
```
3. Configure application.properties in the src/main/resources directory:
```bash
# Database configuration
spring.datasource.url=jdbc:mysql://localhost:3306/commerce
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT configuration
spring.app.jwtSecret=your_jwt_secret_key
spring.app.jwtExpirationMs=36000000
spring.app.jwtCookieName=SpringBootCommerce

# Image storage
project.image=images/
image.base.url=http://localhost:8080/images

# Stripe configuration
stripe.secret.key=your_stripe_secret_key
```
4. Build and run the Spring Boot application:
```bash
./mvnw spring-boot:run
```

### Frontend Setup
1. Navigate to the frontend directory:
```bash
cd frontend
```
2. Install dependencies:
```bash
npm install
```
3. Configure environment variables:
   Create a .env.local file with:
```bash
VITE_BACK_END_URL=http://localhost:8080
VITE_STRIPE_PUBLISHABLE_KEY=your_stripe_publishable_key
```
4. Start the development server:
```bash
npm run dev
```
5. Build for production:
```bash
npm run build
```

## 🖼️ Application Screenshots

### Product Browsing
![Image](https://github.com/user-attachments/assets/37f08717-18d1-4799-8e81-7a3ad3a93023)
### Home Page
![Image](https://github.com/user-attachments/assets/844ca3a5-9cbe-4f21-ae88-f41eea781893)
### About Page
![Image](https://github.com/user-attachments/assets/405fbb02-cc80-4434-bf6d-d0fd8245b38b)
### Order View
![Image](https://github.com/user-attachments/assets/e40aadaa-da5b-455b-9dac-a7cd4a1929d7)
### Login Page
![Image](https://github.com/user-attachments/assets/5657dff1-4acf-4552-aac8-9d8393b6343a)
### Cart View
![Image](https://github.com/user-attachments/assets/5583fe96-723b-493a-b0d7-c3dc117356df)
### Checkout Flow
![Image](https://github.com/user-attachments/assets/3fae4245-376c-4d2c-963b-13663ca0ee32)
