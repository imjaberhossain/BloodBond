# BloodBond — Blood Donor Social Network

A production-ready Spring Boot 3 application that connects blood donors and recipients.

## Tech Stack
- Java 17, Spring Boot 3.3, Spring Security, Spring WebSocket (STOMP)
- PostgreSQL + Spring Data JPA
- Thymeleaf + Tailwind CSS (glass-morphism UI)

## Features
- Dual role accounts (donor + receiver), Facebook-style public profiles with photo upload
- Email OTP verification + OTP-based Forgot/Reset password (OTP printed to the server console for demo)
- One-tap device GPS location (browser Geolocation API — no manual coordinates needed)
- Emergency blood request board with automatic expiry (scheduled task)
- Geo-search of donors by blood group only — sorted by exact distance
- Gamification: live donation counter + auto-upgrading badges
- Social feed with posts, reactions and comments
- WhatsApp-style real-time chat over WebSockets (+ call placeholders)
- Landing page with dynamic stats and smart facts

## How to run
1. Create a PostgreSQL database named `bloodbond`
2. Update `src/main/resources/application.properties` (DB user/password)
3. Run: `./mvnw spring-boot:run` or start `BloodBondApplication` from your IDE
4. Open http://localhost:8080

> No demo/seed data is included — sign up your first account, verify with the OTP
> shown in the server console, set your location via the GPS button, and start donating!
