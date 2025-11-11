# 🧾 Innovate Claim Automation Portal

## Smart Insurance Claim Processing with OCR, GPT & Spring Boot

🚀 A next-generation insurance claim automation platform integrating **OCR**, **GPT-powered data extraction**, and **Spring Boot microservices**, built for efficiency and precision.

---

## 🧠 Overview

The **Innovate Claim Automation Portal** streamlines and accelerates insurance claim handling through intelligent automation.

### Key Highlights

- 🧾 **AI-based Data Extraction:** Uses GPT to parse structured claim details from emails or uploaded forms  
- 🔍 **OCR Integration:** Reads scanned documents using Tesseract OCR or Apache Tika  
- 📊 **Dynamic Analytics Dashboard:** Provides real-time visual insights into claim performance  
- 🔐 **JWT Authentication & Role Management:** Ensures secure and role-based access control  
- ⚙️ **Admin Controls:** Start or stop background email polling and view system stats  

Built with a **scalable Spring Boot backend**, a **responsive Thymeleaf frontend**, and **modern UI components** designed for usability and performance.

---

## 🧱 Tech Stack

| Layer | Technology |
|:------|:------------|
| **Backend** | Spring Boot 3, Spring Security, JPA (Hibernate), MySQL |
| **Frontend** | Thymeleaf, HTML5, CSS3, Vanilla JS (ES6), Chart.js |
| **AI & OCR** | Azure OpenAI GPT, Tesseract OCR, Apache Tika |
| **Auth** | JWT (HS512), BCrypt Password Encryption |
| **Build Tool** | Maven |

---

## 💡 Core Features

- ✅ **OCR + GPT Claim Extraction** — Automatically populates claim fields from scanned documents  
- 📄 **Manual Claim Form** — Enables manual entry of claim data  
- 📈 **Analytics Dashboard** — Real-time claim performance visualizations  
- 🧾 **Claim History Viewer** — Search, filter, paginate, and retry failed claims  
- ⚙️ **Admin Controls** — Start or stop background email polling scheduler  
- 🔐 **JWT Authentication** — Secure login and role-based access  
- 👁️ **Role-Based UI Rendering** — Hides admin-only sections for regular users  

---

## 🔐 Roles & Access

| Role | Permissions |
|:------|:-------------|
| 🧑 **User** | Can register, log in, and submit new claims |
| 👨‍💼 **Admin** | Access analytics, claim history, retry failed claims, and manage the scheduler |

---

## ⚙️ Quick Start

### 1️⃣ Clone the Repository
```bash
git clone https://github.com/your-username/innovate-claim-automation.git
cd innovate-claim-automation
```

### 2️⃣ Configure the Database (application.yml)
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/innovate_db
    username: root
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
### 3️⃣ Run the Application
```bash
mvn spring-boot:run
```
### 🌐 Open in your browser:
http://localhost:8085/login-page


## 🖼️ User Interface Overview
### 📊 Analytics | 📜 History | ➕ Report Loss
----------------------------------------
- [ Claim Statistics Bar Chart ]
- [ OCR Upload Section + Auto Field Population ]
- [ Manual Claim Submission Form ]
- [ Claim History Table with Retry Buttons ]
- Admins see all tabs (Analytics, History, Report Loss)
- Users see only Report Loss

### 🔮 Future Enhancements
- 🤖 AI-based Fraud Detection
- 👥 Admin User Management Panel
- 📨 Email Notification System
- 🐳 Docker Containerization
