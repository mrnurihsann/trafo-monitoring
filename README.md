
# 🚀 PLN Transformer Monitoring System - Setup Guide

Sistem monitoring transformator PLN berbasis web, dengan backend Quarkus dan frontend Vue.js, mendukung notifikasi real-time, manajemen work order, dan dashboard analitik.

---

## 📋 Prerequisites

- Java 17+
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL 10 (via Docker)

---

## 🗄️ Database Setup (PostgreSQL 10)

### 1. Start PostgreSQL dengan Docker

```bash
cd pln-trafo-backend
docker-compose up -d
```

Database akan berjalan di:

- **Host:** localhost:5432  
- **Database:** `trafo_monitoring`  
- **Username:** `sesuaikan`  
- **Password:** `sesuaikan`

### 2. Connect via DBeaver (opsional)

- **Driver:** PostgreSQL  
- **Server Host:** localhost  
- **Port:** 5432  
- **Database:** `trafo_monitoring`  
- **Username:** `sesuaikan`  
- **Password:** `sesuaikan`

---

## 🔧 Backend Setup (Quarkus)

### 1. Masuk ke direktori backend

```bash
cd pln-trafo-backend
```

### 2. Install dependencies & run

```bash
# Development mode (hot reload)
./mvnw quarkus:dev

# Atau production mode
./mvnw compile quarkus:dev
```

- Backend akan berjalan di: `http://localhost:8080`

### 3. API Documentation

- Swagger UI: [http://localhost:8080/q/swagger-ui](http://localhost:8080/q/swagger-ui)  
- OpenAPI Spec: [http://localhost:8080/q/openapi](http://localhost:8080/q/openapi)

### 4. Test API Endpoints

```bash
# Login untuk dapatkan JWT token
curl -X POST http://localhost:8080/auth/login   -H "Content-Type: application/json"   -d '{"username":"admin","password":"admin123"}'

# Get transformers (dengan Authorization header)
curl -X GET http://localhost:8080/api/transformers   -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 💻 Frontend Setup (Vue.js)

### 1. Masuk ke direktori frontend

```bash
cd pln-transformer-monitor
```

### 2. Install dependencies

```bash
npm install
```

### 3. Development mode

```bash
npm run dev
```

- Frontend berjalan di: `http://localhost:5173`

### 4. Production build

```bash
npm run build
npm run preview
```

---

## 🔗 Integration Frontend-Backend

### 1. Update API Base URL

Edit file `pln-transformer-monitor/src/services/api.ts`:

```ts
const API_BASE_URL = 'http://localhost:8080';
```

### 2. Enable CORS di Backend

Sudah dikonfigurasi di `application.properties`:

```properties
quarkus.http.cors=true
quarkus.http.cors.origins=http://localhost:5173,http://localhost:3000
```

---

## 👥 User Accounts untuk Testing

### Default Admin User
- **Username:** `admin`  
- **Password:** `admin123`  
- **Role:** `ADMIN`

### Default Engineer User
- **Username:** `engineer1`  
- **Password:** `engineer123`  
- **Role:** `ENGINEER`

### Default Operator User
- **Username:** `operator1`  
- **Password:** `operator123`  
- **Role:** `OPERATOR`

---

## 🌐 Real-time WebSocket

### Connect ke WebSocket

```js
const ws = new WebSocket('ws://localhost:8080/ws/notifications/USER_ID');
ws.onmessage = (event) => {
  console.log('Notification:', JSON.parse(event.data));
};
```

---

## 📊 Database Schema

**Tables Created:**

1. `users` - User management dengan roles  
2. `transformers` - Data transformator PLN  
3. `status_history` - History perubahan status  
4. `work_orders` - Work orders untuk maintenance  
5. `notifications` - Sistem notifikasi  

**Sample Data:**

- 5 transformers dengan berbagai status  
- 3 users dengan roles berbeda  
- 10+ work orders  
- Status history tracking

---

## 🔧 Development Tools

### Backend Tools

- Quarkus Dev Mode: Hot reload  
- Database Migration: Flyway  
- API Testing: REST Assured  
- Monitoring: Health checks & metrics  

### Frontend Tools

- Vite: Fast development  
- Vue DevTools: Debugging  
- TypeScript: Type safety  
- Responsive Design: Mobile-first  

---

## 🚀 Production Deployment

### Backend Deployment

```bash
# Build Docker image
docker build -t pln-trafo-backend .

# Run dengan environment variables
docker run -p 8080:8080   -e QUARKUS_PROFILE=prod   -e DB_HOST=your-postgres-host   pln-trafo-backend
```

### Frontend Deployment

```bash
# Build untuk production
npm run build

# Deploy dist/ folder ke web server
# Atau gunakan platform seperti Vercel / Netlify
```

---

## 🎯 Features Ready untuk Testing

### ✅ Sudah Berfungsi:

- Authentication & Authorization  
- CRUD transformers  
- Status tracking & history  
- Real-time notifications (WebSocket)  
- Dashboard analytics  
- User management  
- Work order management  
- Reports & export CSV  
- Mobile responsive UI  

---

## 🧪 Testing Scenarios

1. Login dengan berbagai roles  
2. Create/edit/delete transformers  
3. Update status transformers  
4. Create work orders  
5. Real-time notifications  
6. Export reports  
7. Mobile UI testing  

---

## 📞 Support

Jika terjadi masalah, periksa hal-hal berikut:

1. Cek koneksi database  
2. Verifikasi API via Swagger  
3. Periksa error di console browser  
4. Cek konfigurasi CORS  
5. Periksa koneksi WebSocket  

---

Aplikasi monitoring trafo PLN siap digunakan! 🎉
