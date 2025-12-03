# Quick Start Guide - Ktor Microservice

## 🚀 Get Started in 5 Minutes

### Option 1: Docker Compose (Easiest)

1. **Start everything with one command:**
```bash
cd ktor-microservice
docker-compose up --build
```

2. **Test the API:**
```bash
# Check health
curl http://localhost:8080/health

# Create a user
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "demo@example.com",
    "username": "demo",
    "password": "password123"
  }'

# Get all users
curl http://localhost:8080/api/v1/users
```

3. **Access monitoring tools:**
- API: http://localhost:8080
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (login: admin/admin)

---

### Option 2: Local Development

1. **Start PostgreSQL:**
```bash
docker run -d \
  --name postgres \
  -e POSTGRES_DB=microservice_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine
```

2. **Run the application:**
```bash
./gradlew run
```

3. **Test it:**
```bash
curl http://localhost:8080/health
```

---

## 📝 Project Structure Overview

```
ktor-microservice/
├── src/main/kotlin/com/example/
│   ├── api/           → REST endpoints
│   ├── domain/        → Business logic
│   ├── data/          → Database access
│   ├── config/        → Configuration
│   ├── di/            → Dependency injection
│   └── plugins/       → Ktor features
├── src/main/resources/
│   ├── application.conf  → Settings
│   └── db/migration/     → Database schemas
└── docker-compose.yml    → One-command deployment
```

---

## 🎯 Key Features You Get

✅ **Clean Architecture** - Organized, testable code
✅ **Database Ready** - PostgreSQL with migrations
✅ **Production Monitoring** - Health checks, metrics, logs
✅ **JWT Auth** - Security built-in
✅ **Docker Ready** - Deploy anywhere
✅ **API Documentation** - See API_DOCS.md

---

## 🔧 Common Tasks

### Add a New Endpoint
1. Create route in `api/routes/`
2. Add business logic in `domain/service/`
3. Implement repository in `data/repository/`

### Add Database Table
1. Create table in `data/table/`
2. Create migration in `resources/db/migration/`

### Change Configuration
Edit `src/main/resources/application.conf` or set environment variables

---

## 📚 Next Steps

1. Read the full [README.md](README.md)
2. Check [API_DOCS.md](API_DOCS.md) for all endpoints
3. Customize for your use case
4. Deploy to production!

---

## 💡 Tips

- **Environment Variables**: Override any config with env vars
- **Hot Reload**: Use `./gradlew run` for development
- **Testing**: Run `./gradlew test`
- **Logs**: Check `logs/application.log` or console

---

## 🆘 Troubleshooting

**Port 8080 already in use?**
```bash
export PORT=8081
./gradlew run
```

**Database connection failed?**
- Check PostgreSQL is running
- Verify DATABASE_URL in application.conf

**Docker build fails?**
```bash
docker-compose down -v
docker-compose up --build
```

---

## 🎓 Learning Resources

- [Ktor Docs](https://ktor.io/docs)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-guide.html)
- [Exposed ORM](https://github.com/JetBrains/Exposed)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

---

**Ready to build something awesome? Let's go! 🚀**
