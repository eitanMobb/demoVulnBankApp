# My Demo Bank App

A modern, feature-rich demo banking application built with Spring Boot for learning and demonstration purposes.

## Overview

My Demo Bank App is a comprehensive web application that showcases banking functionality in a clean, professional interface. It demonstrates modern web development practices and provides a realistic banking experience for educational and portfolio purposes.

## 🎯 Purpose

This application is designed for:
- Learning web development with Spring Boot
- Demonstrating banking application features
- Portfolio demonstration
- Educational purposes
- Understanding database interactions

## 🛠️ Features

### Core Banking Functionality
- **User Authentication** - Secure login system
- **Account Management** - Multiple account types (checking, savings, investment)
- **Money Transfers** - Transfer funds between accounts
- **Credit Card Applications** - Apply for various credit card products
- **Application Search** - Search through credit applications
- **Dashboard** - Comprehensive overview of accounts and activities

### Technical Features
- **Spring Boot** - Modern Java web framework
- **H2 Database** - In-memory database for easy setup
- **Thymeleaf** - Server-side templating
- **Responsive Design** - Works on all device sizes
- **Professional UI** - Clean, modern interface

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Running the Application

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd my-demo-bank-app
   ```

2. **Build the application**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - Open your browser and go to: `http://localhost:8080`
   - Use the demo accounts provided on the login page

### Demo Accounts

| Username | Password | Description |
|----------|----------|-------------|
| alice | password123 | Customer with multiple accounts |
| bob | pass456 | Customer with basic accounts |
| admin | admin | Administrative user |

## 💳 Credit Card Products

### DemoBank Premium
- 2% cash back on all purchases
- No annual fee
- 0% APR for 12 months
- Free fraud protection

### DemoBank Rewards
- 5% back on rotating categories
- 1% back on everything else
- $200 sign-up bonus
- Travel insurance included

### DemoBank Student
- No credit history required
- 1% cash back
- Credit education resources
- Graduation bonus

## 📁 Project Structure

```
my-demo-bank-app/
├── src/
│   ├── main/
│   │   ├── java/com/demobank/
│   │   │   ├── DemoBankApplication.java
│   │   │   ├── controller/
│   │   │   │   └── BankController.java
│   │   │   ├── entity/
│   │   │   │   ├── User.java
│   │   │   │   ├── Account.java
│   │   │   │   └── CreditApplication.java
│   │   │   └── service/
│   │   │       ├── BankService.java
│   │   │       └── DataInitService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/css/
│   │       │   └── style.css
│   │       └── templates/
│   │           ├── login.html
│   │           ├── dashboard.html
│   │           ├── accounts.html
│   │           ├── transfer.html
│   │           ├── credit-application.html
│   │           └── search.html
├── pom.xml
└── README.md
```

## 🖥️ User Interface

### Dashboard
- Account overview with balances
- Quick action buttons
- Recent transaction history
- Professional banking interface

### Account Management
- View all accounts in card format
- Detailed account information table
- Asset summary and categorization
- Account-specific actions

### Money Transfers
- User-friendly transfer form
- Account selection dropdowns
- Transfer limits and validation
- Recent transfer history

### Credit Applications
- Comprehensive application form
- Multiple credit card options
- Application requirements guide
- Professional application flow

## 🔧 Technology Stack

- **Backend**: Spring Boot 3.2.0
- **Database**: H2 Database (in-memory)
- **Template Engine**: Thymeleaf
- **Build Tool**: Maven
- **Java Version**: 17
- **CSS Framework**: Custom responsive design

## 🏗️ Architecture

The application follows a standard MVC architecture:

- **Controller Layer**: Handles HTTP requests and responses
- **Service Layer**: Contains business logic
- **Entity Layer**: Represents database entities
- **Template Layer**: Thymeleaf templates for UI rendering

## 📊 Database Schema

### Users Table
- User authentication and profile information
- Stores username, password, and personal details

### Accounts Table
- Banking account information
- Different account types with balances and account numbers

### Credit Applications Table
- Credit card application data
- Application status and user information

## 🎨 Design Features

- **Modern UI**: Clean, professional banking interface
- **Responsive Design**: Works on desktop, tablet, and mobile
- **Intuitive Navigation**: Easy-to-use navigation system
- **Visual Feedback**: Success/error messages and status indicators
- **Professional Color Scheme**: Banking-appropriate color palette

## 🚀 Getting Started for Developers

1. **Fork the repository**
2. **Set up your development environment**
3. **Install dependencies**: `mvn clean install`
4. **Run the application**: `mvn spring-boot:run`
5. **Make your changes**
6. **Test thoroughly**
7. **Submit a pull request**

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

## 📄 License

This project is provided as-is for educational and demonstration purposes.

## 📧 Contact

For questions or suggestions about this demo application, please open an issue in the repository.

---

**Note**: This is a demonstration application designed for learning purposes. It showcases modern web development practices and banking application features.