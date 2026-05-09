# Visual Spider 可视化爬虫系统

**运营优先的可视化爬虫配置系统**，支持通过内嵌浏览器点击操作自动生成爬虫规则。

## 技术栈

| 组件 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.2.5 |
| Java 版本 | Java | 17+ |
| 数据库 | PostgreSQL | 15+ |
| HTML 解析 | Jsoup | 1.17.2 |
| 构建工具 | Maven | 3.x |

## 项目结构

```
visual_spider3/
├── backend/                    # Spring Boot 后端
│   └── src/main/java/com/example/visualspider/
│       ├── controller/         # REST API (SpiderTaskController, ContentController)
│       ├── service/            # 业务逻辑 + 爬虫引擎
│       ├── entity/             # JPA Entity
│       ├── repository/         # 数据访问
│       ├── dto/                # Request/Response DTO
│       ├── config/             # 配置类
│       └── exception/           # 异常定义
├── frontend/                   # Vue3 前端（已完成）
├── docs/                       # 项目文档
│   └── roadmap.md             # 开发路线图
└── openspec/                   # OpenSpec 变更追踪
```

## 快速开始

### 前置条件

- JDK 17+
- PostgreSQL 15+
- Maven 3.x

### 1. 创建数据库

```sql
CREATE DATABASE postgres;
```

### 2. 配置数据库连接

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: 123456
```

### 3. 构建项目

```bash
cd backend
mvn clean package -DskipTests
```

### 4. 运行

```bash
java -jar target/visual-spider-0.0.1-SNAPSHOT.jar
```

服务启动于 `http://localhost:8080`

### 5. 前端开发（可选）

前端默认集成在后端JAR中（`backend/src/main/resources/static/`），也可独立开发：

```bash
cd frontend
npm install
npm run dev      # 开发服务器 http://localhost:3000
npm run build    # 构建到 backend/src/main/resources/static/
```

## API 文档

### 任务管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/tasks | 分页查询任务列表 |
| GET | /api/tasks/{id} | 获取任务详情 |
| POST | /api/tasks | 创建任务 |
| PUT | /api/tasks/{id} | 更新任务 |
| DELETE | /api/tasks/{id} | 删除任务 |
| POST | /api/tasks/{id}/enable | 启用任务 |
| POST | /api/tasks/{id}/disable | 停用任务 |
| POST | /api/tasks/{id}/run | 手动执行任务 |

### 内容管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/contents | 分页查询内容列表 |
| GET | /api/contents/{id} | 获取内容详情 |
| PUT | /api/contents/{id} | 更新内容 |
| DELETE | /api/contents/{id} | 删除内容 |
| GET | /api/contents/export | 导出内容 |

## 开发指南

### 运行测试

```bash
cd backend
mvn test
```

### 添加新依赖

编辑 `backend/pom.xml`，然后执行：

```bash
mvn clean package -DskipTests
```

## 里程碑进度

| 里程碑 | 名称 | 状态 |
|--------|------|------|
| M1 | 基础设施 | ✅ 已完成 |
| M2 | 任务管理 | ✅ 已完成 |
| M3 | 爬虫核心 | ✅ 已完成 |
| M4 | 可视化配置 | ✅ 已完成（E2E 测试：26/26 通过） |
| M5 | 内容管理 | ⬜ 未开始 |
| M6 | 调度与发布 | ⬜ 未开始 |

详见 [docs/roadmap.md](docs/roadmap.md)

## 规范参考

- 全局规范：`C:\Users\yin-s\.config\opencode\AGENTS.md`
- 项目规范：`AGENTS.md`
- 使用 Spring Boot 开发时推荐加载：`/springboot-patterns`、`/java-coding-standards`
