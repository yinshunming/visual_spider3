# e2e-main-flow

**日期**: 2026-05-10
**状态**: 待实现

---

## ADDED Requirements

### Requirement: E2E 测试基础设施

E2E 测试套件 SHALL 提供完整的主链路端到端测试能力，包括 Mock Server、测试数据 Seed、Page Objects 和 Spec 文件。

### Requirement: 完整主链路测试

系统 SHALL 提供完整用户主链路的 E2E 测试，覆盖以下步骤：

1. 创建任务
2. 配置规则（列表页规则 + 内容页字段）
3. 保存任务
4. 启用任务
5. 手动执行爬虫
6. 验证内容入库
7. 查看内容列表

### Requirement: Mock Target Site

系统 SHALL 提供本地 Mock HTTP Server 和静态 HTML Fixtures，用于模拟目标网站的列表页和内容页结构，避免依赖外部真实网站。

### Requirement: 测试数据 Seed

系统 SHALL 提供测试数据准备函数，在 E2E 测试执行前准备必要的任务和内容数据。

### Requirement: Page Object 模式

系统 SHALL 使用 Page Object 模式组织 E2E 测试代码，页面元素定位集中在 Page Objects 中，Spec 文件专注于测试逻辑。

---

## ADDED Scenarios

#### Scenario: E2E-P0-01 完整主链路测试
- **WHEN** 执行完整主链路 E2E 测试
- **THEN** 创建任务→配置→保存→启用→执行→内容查看 全流程通过

#### Scenario: Mock Server 可用性
- **WHEN** E2E 测试启动时
- **THEN** Mock Server 可响应 HTTP 请求，返回静态 HTML Fixtures

#### Scenario: 测试数据就绪
- **WHEN** E2E 测试需要预置数据时
- **THEN** Seed 函数可创建任务和内容数据

#### Scenario: Page Object 可用
- **WHEN** E2E 测试需要操作页面时
- **THEN** Page Objects 提供稳定的元素定位和方法
