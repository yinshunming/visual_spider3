# Visual Spider 可视化爬虫系统

**运营优先的可视化爬虫配置系统**，支持通过内嵌浏览器点击操作自动生成爬虫规则。

## 技术栈

| 组件 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.2.5 |
| Java 版本 | Java | 17+ |
| 数据库 | PostgreSQL | 15+ |
| HTML 解析 | Jsoup | 1.17.2 |
| Lombok | Lombok | 1.18.38 |
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
CREATE DATABASE visual_spider3_new;
```

### 2. 配置数据库连接

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/visual_spider3_new
    username: postgres
    password: 123456
```

**注意**: Hibernate `ddl-auto: update` 会自动创建表，无需手动建表。

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
|------|------|
| GET | /api/contents | 分页查询内容列表 |
| GET | /api/contents/{id} | 获取内容详情 |
| PUT | /api/contents/{id} | 更新内容 |
| DELETE | /api/contents/{id} | 删除内容 |
| GET | /api/contents/export | 导出内容 |

### 执行历史

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/executions | 分页查询执行历史（支持 taskId 过滤） |
| GET | /api/executions/{id} | 执行详情（含耗时、条数、错误信息） |

### Playwright 浏览器自动化

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/playwright/sessions | 创建浏览器 Session |
| DELETE | /api/playwright/sessions/{sessionId} | 关闭 Session |
| POST | /api/playwright/sessions/{sessionId}/ping | 心跳保活 |
| POST | /api/playwright/sessions/{sessionId}/navigate | 导航到新页面 |
| POST | /api/playwright/sessions/{sessionId}/screenshot | 获取截图 |
| POST | /api/playwright/sessions/{sessionId}/element | 获取坐标处元素信息 |
| POST | /api/playwright/sessions/{sessionId}/test-selector | 测试选择器唯一性 |

**限制**: 最多 5 个并发 Session，Session 超时 3 分钟自动清理。

## 开发指南

### 运行测试

```bash
cd backend
mvn test
```

### IDEA 启动后端报 `TypeTag :: UNKNOWN`

项目编译目标是 Java 17，但本机可以用 JDK 24 编译运行。若 IDEA 使用 JDK 24，必须使用 Lombok `1.18.38+`；旧版本 Lombok 在 JDK 24 的 `javac` annotation processor 初始化阶段会报：

```text
java: java.lang.ExceptionInInitializerError
com.sun.tools.javac.code.TypeTag :: UNKNOWN
```

处理步骤：

1. Reload Maven Projects，确认 `backend/pom.xml` 中 Lombok 为 `1.18.38+`
2. Build -> Rebuild Project
3. 仍失败时执行 Invalidate Caches / Restart
4. 更稳妥的配置是让 IDEA Project SDK / Maven Runner JRE 使用 JDK 17

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
| M4 | 可视化配置 | ✅ 已完成（Playwright 升级：E2E 11/11 通过） |
| M5 | 内容管理 | ✅ 已完成（API 测试：21/21 通过，E2E：60/67 通过） |
| M6 | 调度与发布 | ✅ 已完成（m6a ✅, m6b ✅, E2E 主流程测试 ✅） |

详见 [docs/roadmap.md](docs/roadmap.md)

## 规范参考

- 全局规范：`C:\Users\yin-s\.config\opencode\AGENTS.md`
- 项目规范：`AGENTS.md`
- 使用 Spring Boot 开发时推荐加载：`/springboot-patterns`、`/java-coding-standards`
