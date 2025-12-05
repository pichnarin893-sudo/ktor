# Architecture Diagram

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Client Layer                         │
│  (Mobile Apps, Web Browsers, API Consumers)                 │
└────────────────────────────┬────────────────────────────────┘
                             │ HTTP/HTTPS
                             │
┌────────────────────────────▼────────────────────────────────┐
│                    Load Balancer / API Gateway              │
│              (Nginx, AWS ALB, Kong, etc.)                   │
└────────────────────────────┬────────────────────────────────┘
                             │
                             │
┌────────────────────────────▼────────────────────────────────┐
│                   Ktor Microservice                         │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              API Layer (Routes)                       │  │
│  │  • User Routes                                        │  │
│  │  • Authentication                                     │  │
│  │  • Request Validation                                 │  │
│  └──────────────┬───────────────────────────────────────┘  │
│                 │                                            │
│  ┌──────────────▼───────────────────────────────────────┐  │
│  │           Domain Layer (Services)                     │  │
│  │  • Business Logic                                     │  │
│  │  • Validation Rules                                   │  │
│  │  • Repository Interfaces                              │  │
│  └──────────────┬───────────────────────────────────────┘  │
│                 │                                            │
│  ┌──────────────▼───────────────────────────────────────┐  │
│  │         Data Layer (Repositories)                     │  │
│  │  • Database Access                                    │  │
│  │  • Data Mapping                                       │  │
│  │  • Connection Pool (HikariCP)                        │  │
│  └──────────────┬───────────────────────────────────────┘  │
│                 │                                            │
│  ┌──────────────┼───────────────────────────────────────┐  │
│  │          Cross-Cutting Concerns                       │  │
│  │  • Logging (Logback)                                  │  │
│  │  • Metrics (Prometheus)                               │  │
│  │  • Error Handling                                     │  │
│  │  • Configuration                                      │  │
│  │  • Dependency Injection (Koin)                       │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
┌───────────────┐    ┌──────────────┐    ┌──────────────┐
│  PostgreSQL   │    │  Prometheus  │    │   Grafana    │
│   Database    │    │   Metrics    │    │  Dashboard   │
│               │    │   Scraper    │    │              │
└───────────────┘    └──────────────┘    └──────────────┘
```

## Request Flow

```
1. Client Request
   │
   ▼
2. API Layer (Route Handler)
   │ - Receive HTTP request
   │ - Parse & validate input
   │ - Extract auth token
   │
   ▼
3. Domain Layer (Service)
   │ - Execute business logic
   │ - Apply validation rules
   │ - Call repository methods
   │
   ▼
4. Data Layer (Repository)
   │ - Build SQL query (Exposed DSL)
   │ - Execute via connection pool
   │ - Map database results
   │
   ▼
5. Database (PostgreSQL)
   │ - Execute query
   │ - Return results
   │
   ▼
6. Response Flow (back up the chain)
   │ - Repository → Service → Route
   │ - Serialize to JSON
   │ - Add appropriate headers
   │ - Return HTTP response
   │
   ▼
7. Client receives response
```

## Component Dependencies

```
┌─────────────────────────────────────────────────────┐
│                  Application.kt                      │
│              (Main Entry Point)                      │
└──────────────────────┬──────────────────────────────┘
                       │
                       │ Initializes
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
┌─────────────┐  ┌──────────┐  ┌──────────┐
│   Plugins   │  │  Koin DI │  │ Database │
│             │  │          │  │  Factory │
└─────────────┘  └────┬─────┘  └──────────┘
                      │
                      │ Provides
                      │
        ┌─────────────┼─────────────┐
        │             │             │
        ▼             ▼             ▼
┌──────────┐   ┌───────────┐  ┌──────────┐
│  Routes  │   │ Services  │  │Repository│
└──────────┘   └───────────┘  └──────────┘
```

## Technology Stack

```
┌─────────────────────────────────────────┐
│          Application Layer              │
│  • Ktor 2.3.7 (Web Framework)          │
│  • Kotlin 1.9.22                        │
│  • Kotlin Coroutines                    │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│         Data Access Layer               │
│  • Exposed ORM 0.46.0                   │
│  • HikariCP (Connection Pool)           │
│  • Flyway (Migrations)                  │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│           Database Layer                │
│  • PostgreSQL 16                        │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│      Observability & Monitoring         │
│  • Prometheus (Metrics)                 │
│  • Grafana (Visualization)              │
│  • Logback (Structured Logging)         │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│         Security & Auth                 │
│  • JWT (JSON Web Tokens)                │
│  • CORS Configuration                   │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│      Dependency Injection               │
│  • Koin 3.5.3                           │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│         Serialization                   │
│  • Kotlinx Serialization (JSON)         │
└─────────────────────────────────────────┘
```

## Deployment Architecture

```
┌─────────────────────────────────────────────────────┐
│                 Production Environment               │
│                                                      │
│  ┌────────────────────────────────────────────┐    │
│  │         Kubernetes Cluster                  │    │
│  │                                             │    │
│  │  ┌─────────────────────────────────────┐   │    │
│  │  │      Ktor Pod (Replica 1)           │   │    │
│  │  └─────────────────────────────────────┘   │    │
│  │  ┌─────────────────────────────────────┐   │    │
│  │  │      Ktor Pod (Replica 2)           │   │    │
│  │  └─────────────────────────────────────┘   │    │
│  │  ┌─────────────────────────────────────┐   │    │
│  │  │      Ktor Pod (Replica 3)           │   │    │
│  │  └─────────────────────────────────────┘   │    │
│  │                                             │    │
│  │  ┌─────────────────────────────────────┐   │    │
│  │  │       Service (Load Balancer)       │   │    │
│  │  └─────────────────────────────────────┘   │    │
│  │                                             │    │
│  │  ┌─────────────────────────────────────┐   │    │
│  │  │         ConfigMap / Secrets         │   │    │
│  │  └─────────────────────────────────────┘   │    │
│  └────────────────────────────────────────────┘    │
│                                                      │
│  ┌────────────────────────────────────────────┐    │
│  │      Managed PostgreSQL Database           │    │
│  │  • AWS RDS / Google Cloud SQL              │    │
│  │  • High Availability                       │    │
│  │  • Automated Backups                       │    │
│  └────────────────────────────────────────────┘    │
│                                                      │
│  ┌────────────────────────────────────────────┐    │
│  │         Monitoring Stack                   │    │
│  │  • Prometheus (Metrics)                    │    │
│  │  • Grafana (Dashboards)                    │    │
│  │  • ELK/Loki (Log Aggregation)             │    │
│  └────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────┘
```

## Clean Architecture Layers

```
┌─────────────────────────────────────────────────────┐
│                  External Interfaces                 │
│         (HTTP, Database, External APIs)              │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│              API / Interface Layer                   │
│  • REST Controllers                                  │
│  • Request/Response DTOs                             │
│  • Input Validation                                  │
└────────────────────┬────────────────────────────────┘
                     │ depends on
┌────────────────────▼────────────────────────────────┐
│               Domain / Business Layer                │
│  • Services (Business Logic)                         │
│  • Domain Models                                     │
│  • Repository Interfaces                             │
│  • Business Rules & Validation                       │
│                                                       │
│  ⚠️  NO FRAMEWORK DEPENDENCIES                       │
└────────────────────┬────────────────────────────────┘
                     │ depends on
┌────────────────────▼────────────────────────────────┐
│              Data / Infrastructure Layer             │
│  • Repository Implementations                        │
│  • Database Tables                                   │
│  • External Service Clients                          │
│  • Framework-specific code                           │
└─────────────────────────────────────────────────────┘

Key Principle: Dependencies point INWARD
• Outer layers depend on inner layers
• Inner layers know nothing about outer layers
• Domain layer is framework-agnostic
```

## Security Flow

```
Client Request
     │
     ▼
┌─────────────────────┐
│  CORS Validation    │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  JWT Verification   │ ← Validates token signature
│  • Check signature  │   and expiration
│  • Verify issuer    │
│  • Check expiration │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Extract User Info  │ ← Gets user ID from token
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Route Handler      │ ← Processes authorized request
└─────────────────────┘
```

---

## Key Design Decisions

1. **Clean Architecture**: Separation of concerns for maintainability
2. **Repository Pattern**: Abstraction over data access
3. **Dependency Injection**: Loose coupling between components
4. **Connection Pooling**: Efficient database resource usage
5. **Structured Logging**: Easy log parsing and analysis
6. **Health Checks**: Kubernetes-ready liveness/readiness probes
7. **Metrics Export**: Prometheus-compatible monitoring
8. **Database Migrations**: Version-controlled schema changes
9. **Environment-based Config**: Easy deployment across environments
10. **Stateless Design**: Horizontal scaling capability
