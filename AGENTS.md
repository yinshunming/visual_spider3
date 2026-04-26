# Spring Boot 项目规范

## 技术栈
- Java 17+ / Spring Boot 3.x
- MyBatis 或 JPA / Maven 或 Gradle
- PostgreSQL / Redis / MQ

## 架构原则
- 业务逻辑放在 Service / Domain 层
- DTO、Entity、VO 分层清晰，避免直接混用
- 关注事务边界、幂等、并发安全、缓存一致性

## 命令
- 构建：`./mvnw clean package` 或 `gradle build`
- 运行：`java -jar target/*.jar` 或 `gradle bootRun`
- 测试：`./mvnw test` 或 `gradle test`

## 参考
- 全局规范：`C:\Users\yin-s\.config\opencode\AGENTS.md`
- 项目 skill：使用 `/springboot-patterns`、`/java-coding-standards` 等

---

# Visual Spider 可视化爬虫系统

## 项目概述
运营优先的可视化爬虫配置系统，支持通过内嵌浏览器点击操作自动生成爬虫规则。

## 系统架构
- **前端**：Vue3 + Element Plus + Chrome DevTools Protocol（内嵌浏览器）
- **后端**：Spring Boot + Jsoup + HttpClient
- **数据库**：PostgreSQL（JSONB 存储动态字段）
- **调度**：Spring @Scheduler

## 核心实体
| 实体 | 说明 |
|-----|------|
| SpiderTask | 爬虫任务（支持列表页/直接URL双模式） |
| SpiderField | 自定义字段（text/image/link/richText） |
| ContentItem | 爬取内容（JSONB 存储动态字段值） |

## 目录结构
```
visual-spider/
├── backend/           # Spring Boot 后端
│   └── src/main/java/com/example/visualspider/
│       ├── controller/    # REST API
│       ├── service/        # 业务逻辑 + 爬虫引擎
│       ├── entity/         # JPA Entity
│       └── repository/     # 数据访问
└── frontend/           # Vue3 前端（独立部署）
    └── src/
        ├── views/          # 页面
        │   ├── TaskList.vue       # 任务管理
        │   ├── TaskConfig.vue     # 可视化配置
        │   └── ContentManage.vue  # 内容管理
        └── components/
            └── EmbeddedBrowser.vue # 内嵌Chromium
```

## 可视化配置流程
1. 输入URL → Playwright加载页面 → 前端展示
2. 点击页面元素 → 自动生成 CSS/XPath 选择器
3. 逐字段配置 → 保存任务规则
4. 支持手动触发 / Cron定时爬取
