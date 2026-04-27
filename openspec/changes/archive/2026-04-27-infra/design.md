## Context

Visual Spider 可视化爬虫系统的基础设施阶段。本阶段需要搭建 Spring Boot 项目骨架、数据库实体、Repository 层。

**技术栈约束**：
- Java 17+ / Spring Boot 3.x
- PostgreSQL（JSONB 存储动态字段）
- Maven 构建工具
- JPA/Hibernate ORM

## Goals / Non-Goals

**Goals:**
- 创建完整的 Spring Boot 项目结构
- 定义 SpiderTask、SpiderField、ContentItem 三个核心实体
- 实现基础的 Repository CRUD
- 配置 PostgreSQL 连接
- 提供 REST API 骨架

**Non-Goals:**
- 不实现具体业务逻辑（爬虫执行、可视化配置）
- 不实现前端
- 不实现调度功能

## Decisions

### Decision 1: Maven over Gradle
**选择**: Maven
**理由**: Spring Boot 官方默认，构建配置更通用

### Decision 2: JPA over MyBatis
**选择**: JPA (Spring Data JPA)
**理由**: 实体关系更清晰，JSONB 字段支持好，减少 SQL 编写

### Decision 3: 包结构
```
com.example.visualspider
├── controller/    # REST API
├── service/       # 业务逻辑
├── entity/        # JPA Entity
├── repository/    # Data Access
├── dto/           # 请求响应对象
└── config/        # 配置类
```

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| JSONB 字段查询性能 | 必要时添加 GIN 索引 |
| 后续需求变更导致结构调整 | 实体设计保持简洁，避免过度设计 |

## Open Questions

- 是否需要单独建立 config 包用于配置类？
