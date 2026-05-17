# Spring Boot 项目规范

## 技术栈
- Java 17 编译目标 / Spring Boot 3.x
- 本机可用 JDK 17+；若 IDEA 使用 JDK 24，Lombok 必须保持 `1.18.38+`
- MyBatis 或 JPA / Maven 或 Gradle
- PostgreSQL / Redis / MQ

## 架构原则
- 业务逻辑放在 Service / Domain 层
- DTO、Entity、VO 分层清晰，避免直接混用
- 关注事务边界、幂等、并发安全、缓存一致性

## 命令
- 构建：`cd backend && mvn clean package -DskipTests`
- 测试：`cd backend && mvn test`
- 前端开发：`cd frontend && npm run dev`（端口 3000，代理 /api 到 8080）
- 前端构建：`cd frontend && npm run build`
- 前端测试：`cd frontend && npm run test`（Vitest）
- 前端 E2E：`cd frontend && npm run test:e2e`（Playwright）

## 测试规范

### 测试基础设施
- **测试数据库**：Repository 测试使用 H2 内存数据库（`application-test.yml` 配置）；`ContentControllerTest` 当前显式连接本地 PostgreSQL
- **测试框架**：Spring Boot Test + JUnit 5
- **Mock 框架**：Spring Boot 内置 MockMvc

### 测试分层
| 层级 | 位置 | 说明 |
|------|------|------|
| Controller 测试 | `src/test/java/.../controller/` | REST API 端到端测试 |
| Service 测试 | `src/test/java/.../service/` | 业务逻辑单元测试 |
| Repository 测试 | `src/test/java/.../repository/` | 数据访问层测试 |

### 运行测试
```bash
cd backend
mvn test
```

### 注意事项
- 测试配置：`src/test/resources/application-test.yml`（H2 内存数据库）
- 完整运行 `mvn test` 前需确保本地 PostgreSQL `localhost:5432/postgres` 可用，账号密码与 `ContentControllerTest` 一致
- 提交前必须通过所有测试
- 新功能需要同步编写对应的单元测试

## 参考
- 全局规范：`C:\Users\yin-s\.config\opencode\AGENTS.md`
- 项目 skill：使用 `/springboot-patterns`、`/java-coding-standards` 等

---

# Visual Spider 可视化爬虫系统

## 项目概述
运营优先的可视化爬虫配置系统，支持通过内嵌浏览器点击操作自动生成爬虫规则。

## 系统架构
- **前端**：Vue3 + Element Plus + Playwright（内嵌浏览器）
- **后端**：Spring Boot + Playwright Java Client + Jsoup + HttpClient
- **数据库**：PostgreSQL（JSONB 存储动态字段）
- **调度**：Spring @Scheduler

## 核心实体
| 实体 | 说明 |
|-----|------|
| SpiderTask | 爬虫任务（支持列表页/直接URL双模式） |
| SpiderField | 自定义字段（text/image/link/richText） |
| ContentItem | 爬取内容（JSONB 存储动态字段值） |
| ExecutionLog | 爬虫执行日志（记录开始/结束时间、状态、耗时、条数） |

## 目录结构
```
visual_spider3/
├── backend/                              # Spring Boot 后端
│   └── src/main/java/com/example/visualspider/
│       ├── controller/                   # REST API
│       ├── service/                      # 业务逻辑 + 爬虫引擎
│       ├── entity/                       # JPA Entity
│       ├── repository/                   # 数据访问
│       ├── dto/                          # Request/Response DTO
│       ├── config/                       # 配置类
│       └── exception/                    # 异常定义
├── frontend/                             # Vue3 前端（已完成）
│   ├── src/
│   │   ├── views/                       # 页面组件
│   │   ├── components/                  # 公共组件
│   │   ├── api/                         # API 调用
│   │   └── router/                      # 路由配置
│   └── tests/
│       └── e2e/                         # Playwright E2E 测试
```

## 可视化配置流程
1. 输入URL → Playwright加载页面 → 前端展示
2. 点击页面元素 → 自动生成 CSS/XPath 选择器
3. 逐字段配置 → 保存任务规则
4. 支持手动触发 / Cron定时爬取
