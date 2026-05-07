## Context

Visual Spider 项目已完成 M4 可视化配置前端开发。前端技术栈为 Vue3 + Vite + Element Plus，后端为 Spring Boot。

当前测试状态：
- 后端：15 个单元测试通过（JUnit 5 + MockMvc）
- 前端：17 个 API 模块单元测试通过（Vitest）
- E2E：空白

项目包含嵌入式浏览器组件（Chrome DevTools Protocol），需要 E2E 测试框架具备 CDP 支持能力。

## Goals / Non-Goals

**Goals:**
- 引入 Playwright E2E 测试框架
- 创建任务管理核心流程的 E2E 测试用例
- 配置 Vite dev server 与 Playwright 集成
- 实现任务列表、创建、配置页的自动化测试覆盖

**Non-Goals:**
- 不包含 CDP 嵌入式浏览器的专项测试（需要单独的计划）
- 不包含 API 层面的集成测试（已有单元测试覆盖）
- 不包含性能测试和压力测试

## Decisions

### 1. 选择 Playwright 而非 Cypress 或 Selenium

**选择 Playwright**

**理由：**
- 原生支持 Chrome DevTools Protocol（CDP），能连接嵌入式浏览器
- Vue3 + Vite 官方推荐组合
- 内置并行测试支持
- 测试隔离性好（每个测试用例独立浏览器上下文）

**替代方案：**
- Cypress：Vue 生态流行，但不支持 CDP，付费版本才有并行测试
- Selenium：传统方案，Setup 复杂，对 Vue3 支持不如 Playwright

### 2. 测试文件位置

**选择 `frontend/tests/e2e/`**

**理由：**
- 与 `src/` 分离，清晰区分测试代码和业务代码
- Playwright 官方推荐 `tests/` 目录结构
- 便于后续扩展到更多 E2E 测试场景

### 3. 使用 Vite dev server 进行测试

**选择 `webServer` 配置 Playwright**

确保测试运行前 Vite dev server 自动启动，测试完成后自动关闭。

## Risks / Trade-offs

| 风险 | 影响 |  Mitigation |
|------|------|-------------|
| Playwright 安装浏览器耗时 | 首次 setup 时间较长 | 使用 `npx playwright install --with-deps chromium` |
| Windows 环境路径问题 | Windows 上文件路径格式不同 | 使用 path.resolve() 处理跨平台路径 |
| 前端组件依赖外部 API | 测试可能因后端问题失败 | 使用 API mock 或 test database |

## Migration Plan

1. 安装 `@playwright/test` 依赖
2. 创建 `playwright.config.ts` 配置文件
3. 创建 `tests/e2e/` 目录结构
4. 实现任务列表页 E2E 测试
5. 实现任务创建页 E2E 测试
6. 实现任务配置页 E2E 测试
7. 验证测试可运行

## Open Questions

- 是否需要为 CDP 嵌入式浏览器单独创建 E2E 测试计划？
- 是否需要配置 CI/CD 自动化运行 E2E 测试？
