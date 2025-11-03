# 📧 Innovaite Email Claim Automation

An AI-powered Spring Boot application that automates **insurance claim creation** by reading incoming emails, extracting claim-related details using an **Azure AI Foundry agent**, and integrating with backend claim APIs.

---

## 🚀 Overview

The **Innovaite Email Claim Automation** service is designed to:
1. Receive and process incoming emails from a configured mailbox (e.g., `insurancecompany.claim@company.com`)
2. Use an **Azure AI Foundry** agent to intelligently extract key claim fields such as:
   - Policy number  
   - Policyholder name  
   - Contact details  
   - Claimed amount  
   - Incident date  
   - Description
3. Forward the structured data to a **Claim Service API** for automated claim registration.

This project combines **Spring Boot**, **Microsoft Graph API**, and **Azure AI Foundry** to deliver an intelligent workflow automation system for insurance claims.

---

## 🧠 System Architecture

+-------------------------+
| User sends email |
| (Outlook / Gmail) |
+------------+------------+
|
v
+------------+------------+
| Email Fetcher Service |
| (Microsoft Graph API) |
+------------+------------+
|
v
+------------+------------+
| AI Claim Extractor |
| (Azure AI Foundry Agent)|
+------------+------------+
|
v
+------------+------------+
| Claim API Client |
| (Spring REST integration)|
+--------------------------+

## 🧩 Technology Stack

| Component | Technology |
|------------|-------------|
| **Backend Framework** | Spring Boot 3.5.x |
| **AI Integration** | Azure AI Foundry (custom deployed chat agent) |
| **Email Fetching** | Microsoft Graph API |
| **Language** | Java 17 |
| **HTTP Client** | Spring WebFlux (`WebClient`) |
| **Serialization** | Jackson Databind |
| **Build Tool** | Maven Wrapper |
| **Configuration** | `application.properties` |
| **Logging** | SLF4J / Spring Boot Logging |

---
