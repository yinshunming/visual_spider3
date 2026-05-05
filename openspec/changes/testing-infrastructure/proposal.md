## Why

Visual Spider 项目目前没有任何自动化测试，核心业务逻辑（爬虫任务管理、内容提取、调度执行）缺乏回归保护。添加完整测试金字塔（单元测试 → 集成测试 → Controller测试）可确保代码质量、降低重构风险、支撑持续交付。

## What Changes

- 新增 H2 内存数据库依赖，用于集成测试
- 创建测试目录结构 `src/test/java` 和 `src/test/resources`
- 新增 `application-test.yml` 和 `schema.sql` 测试配置
- 新增 Fixtures 测试工具类（`TaskFixtures`、`FieldFixtures`）
- 新增 3 个 Repository 层集成测试类
- 新增 3 个 Service 层单元测试类
- 新增 2 个 Controller 层测试类
- 配置 Maven Surefire 插件支持测试执行

## Capabilities

### New Capabilities

- `test-infrastructure`: 测试基础设施，包含 H2 配置、Fixtures、目录结构
- `repository-tests`: Repository 层集成测试，覆盖 SpiderTaskRepository、SpiderFieldRepository、ContentItemRepository
- `service-tests`: Service 层单元测试，覆盖 SpiderTaskService、CrawlerEngine、ContentService
- `controller-tests`: Controller 层测试，覆盖 SpiderTaskController、ContentController

### Modified Capabilities

- （无）

## Impact

- **新增依赖**: `com.h2database:h2` (test scope)
- **影响范围**: backend 模块，src/test 目录
- **测试文件**: 11 个新 Java 文件 + 2 个配置文件
- **构建变化**: `./mvnw test` 可执行全部测试