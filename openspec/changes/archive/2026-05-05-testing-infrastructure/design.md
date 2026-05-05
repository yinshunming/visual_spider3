## Context

Visual Spider 项目使用 Spring Boot 3.2.5 + Java 17 + PostgreSQL，已完成 M1-M3 里程碑（基础设施、任务管理、爬虫核心），但完全缺乏自动化测试。pom.xml 已配置 `spring-boot-starter-test`（含 JUnit 5、Mockito、Spring Test），但 `src/test` 目录不存在。

项目待测试组件：
- **Controller**: SpiderTaskController、ContentController
- **Service**: SpiderTaskService、CrawlerEngine、ContentService、ListPageParser、DirectUrlParser、ContentPageExtractor、PaginationRule
- **Repository**: SpiderTaskRepository、SpiderFieldRepository、ContentItemRepository
- **Entity**: SpiderTask、SpiderField、ContentItem

## Goals / Non-Goals

**Goals:**
- 建立完整测试金字塔（Repository 集成测试 → Service 单元测试 → Controller 测试）
- 使用 H2 内存数据库进行 Repository 集成测试
- 使用 Mockito 进行 Service 层单元测试（隔离外部依赖）
- 使用 @WebMvcTest + MockMvc 进行 Controller 层测试
- 创建可复用的 Fixtures 工具类
- 达到覆盖率目标：Service/Repository 80%+，Controller 70%+

**Non-Goals:**
- 不修复 ContentController 中未实现的 TODO 方法
- 不引入 Testcontainers（后续有需要再添加）
- 不包含前端 E2E 测试
- 不进行性能测试或安全测试

## Decisions

### 1. H2 内存数据库 vs Testcontainers

**选择**: H2 内存数据库

**理由**: 启动快（秒级）、无外部依赖（Docker）、CI 友好、维护成本低。

**替代方案**: Testcontainers（真实 PostgreSQL），但启动慢（10-30秒），需要 Docker 环境，CI 配置复杂。

**风险**: H2 与 PostgreSQL 语法可能存在细微差异（如某些函数、索引语法）。如遇差异，通过自定义 H2 方言或调整 SQL 解决。

### 2. @DataJpaTest vs @SpringBootTest

**选择**: @DataJpaTest 用于 Repository 测试

**理由**: @DataJpaTest 只加载 JPA 相关组件，启动快（2-3秒），自动配置内存数据库，适合 Repository CRUD 测试。

**替代方案**: @SpringBootTest 加载完整上下文，但启动慢（10+秒）。

### 3. 构造器注入 vs Field 注入

**现状**: Service/Controller 使用 @Autowired 字段注入

**决策**: 测试中不强制重构注入方式，保持与现有代码一致。使用反射或 Spring 测试工具注入 Mock 对象。

### 4. Fixtures 方案

**选择**: 静态工厂方法（TaskFixtures、FieldFixtures）

**理由**: 简单直接、无框架依赖、调用方一目了然。避免引入 Test Data Builder 或 Fluent Builder 增加复杂度。

## Risks / Trade-offs

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| H2 与 PostgreSQL 语法差异 | 集成测试通过但生产失败 | 使用 PostgreSQL 兼容模式，发现差异时调整 SQL |
| 现有代码使用字段注入 | Mock 注入较困难 | 使用 ReflectionTestUtils 或 @MockBean |
| Controller 测试需要完整上下文 | 测试启动慢 | 按需使用 @WebMvcTest(Controller.class) |
| ContentController 大量 TODO | 测试覆盖不完整 | 排除 TODO 方法，仅测试已实现方法 |