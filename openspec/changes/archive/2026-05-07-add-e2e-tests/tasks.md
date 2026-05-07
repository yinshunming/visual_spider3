## 1. Playwright 基础配置

- [x] 1.1 安装 `@playwright/test` 依赖到 `frontend/package.json`
- [x] 1.2 创建 `frontend/playwright.config.ts` 配置文件
- [x] 1.3 配置 `webServer` 指定 Vite dev server 启动参数
- [x] 1.4 添加 `npm run test:e2e` 脚本到 `frontend/package.json`
- [ ] 1.5 安装 Chromium 浏览器 `npx playwright install chromium`

## 2. 测试目录结构

- [x] 2.1 创建 `frontend/tests/e2e/` 目录
- [x] 2.2 创建 `frontend/tests/e2e/page-objects/` 目录（Page Object 模式）

## 3. Page Object 基础类

- [x] 3.1 创建 `BasePage.ts` - 基础页面对象类
- [x] 3.2 创建 `TaskListPage.ts` - 任务列表页对象
- [x] 3.3 创建 `TaskCreatePage.ts` - 任务创建页对象
- [x] 3.4 创建 `TaskConfigPage.ts` - 任务配置页对象

## 4. E2E 测试用例 - 任务列表

- [x] 4.1 创建 `task-list.spec.ts` 测试文件
- [x] 4.2 实现任务列表页加载测试
- [x] 4.3 实现空列表状态测试
- [x] 4.4 实现任务行操作按钮测试（Edit/Delete）
- [x] 4.5 实现启用/停用切换测试

## 5. E2E 测试用例 - 任务创建

- [x] 5.1 创建 `task-create.spec.ts` 测试文件
- [x] 5.2 实现导航到创建页测试
- [x] 5.3 实现表单字段验证测试
- [x] 5.4 实现成功创建任务测试
- [x] 5.5 实现取消创建测试

## 6. E2E 测试用例 - 任务配置

- [x] 6.1 创建 `task-config.spec.ts` 测试文件
- [x] 6.2 实现加载配置页测试
- [x] 6.3 实现添加字段测试
- [x] 6.4 实现保存配置测试
- [x] 6.5 实现放弃更改测试

## 7. 验证测试

- [ ] 7.1 运行 `npm run test:e2e` 验证所有测试通过
- [ ] 7.2 确认测试报告生成正常
