# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: task-list.spec.ts >> 任务列表页 E2E 测试 >> 4.4 任务行操作按钮 - 应能点击删除按钮
- Location: tests\e2e\task-list.spec.ts:44:3

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: locator('.el-message-box, [role="dialog"]')
Expected: visible
Error: strict mode violation: locator('.el-message-box, [role="dialog"]') resolved to 2 elements:
    1) <div role="dialog" aria-label="提示" aria-modal="true" class="el-overlay-message-box" aria-describedby="el-id-5134-6">…</div> aka getByRole('dialog', { name: '提示' })
    2) <div tabindex="-1" class="el-message-box">…</div> aka getByText('提示确定要删除该任务吗？CancelOK')

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for locator('.el-message-box, [role="dialog"]')

```

# Page snapshot

```yaml
- generic [ref=e1]:
  - generic [ref=e4]:
    - heading "Visual Spider - 可视化爬虫配置" [level=2] [ref=e6]
    - main [ref=e7]:
      - generic [ref=e8]:
        - button "新建任务" [ref=e10] [cursor=pointer]:
          - generic [ref=e11]: 新建任务
        - generic [ref=e13]:
          - table [ref=e15]:
            - rowgroup [ref=e23]:
              - row "ID 任务名称 模式 状态 创建时间 操作" [ref=e24]:
                - columnheader "ID" [ref=e25]:
                  - generic [ref=e26]: ID
                - columnheader "任务名称" [ref=e27]:
                  - generic [ref=e28]: 任务名称
                - columnheader "模式" [ref=e29]:
                  - generic [ref=e30]: 模式
                - columnheader "状态" [ref=e31]:
                  - generic [ref=e32]: 状态
                - columnheader "创建时间" [ref=e33]:
                  - generic [ref=e34]: 创建时间
                - columnheader "操作" [ref=e35]:
                  - generic [ref=e36]: 操作
          - table [ref=e41]:
            - rowgroup [ref=e49]:
              - row "1 E2E 测试任务 列表页 草稿 2026/5/10 11:09:36 配置 启用 停用 删除" [ref=e50]:
                - cell "1" [ref=e51]:
                  - generic [ref=e52]: "1"
                - cell "E2E 测试任务" [ref=e53]:
                  - generic [ref=e54]: E2E 测试任务
                - cell "列表页" [ref=e55]:
                  - generic [ref=e56]: 列表页
                - cell "草稿" [ref=e57]:
                  - generic [ref=e60]: 草稿
                - cell "2026/5/10 11:09:36" [ref=e61]:
                  - generic [ref=e62]: 2026/5/10 11:09:36
                - cell "配置 启用 停用 删除" [ref=e63]:
                  - generic [ref=e64]:
                    - button "配置" [ref=e65] [cursor=pointer]:
                      - generic [ref=e66]: 配置
                    - button "启用" [ref=e67] [cursor=pointer]:
                      - generic [ref=e68]: 启用
                    - button "停用" [ref=e69] [cursor=pointer]:
                      - generic [ref=e70]: 停用
                    - button "删除" [ref=e71] [cursor=pointer]:
                      - generic [ref=e72]: 删除
        - generic [ref=e73]:
          - generic [ref=e74]: Total 1
          - generic [ref=e77] [cursor=pointer]:
            - generic:
              - combobox [ref=e79]
              - generic [ref=e80]: 10/page
            - img [ref=e83]
          - button "Go to previous page" [disabled] [ref=e85]:
            - generic:
              - img
          - list [ref=e86]:
            - listitem "page 1" [ref=e87]: "1"
          - button "Go to next page" [disabled] [ref=e88]:
            - generic:
              - img
  - dialog "提示" [ref=e90]:
    - generic [ref=e91]:
      - generic [ref=e92]:
        - generic [ref=e93]: 提示
        - button "Close this dialog" [ref=e94] [cursor=pointer]:
          - img [ref=e96]
      - generic [ref=e99]:
        - img [ref=e101]
        - paragraph [ref=e104]: 确定要删除该任务吗？
      - generic [ref=e105]:
        - button "Cancel" [ref=e106] [cursor=pointer]:
          - generic [ref=e107]: Cancel
        - button "OK" [active] [ref=e108] [cursor=pointer]:
          - generic [ref=e109]: OK
```

# Test source

```ts
  1  | import { test, expect } from '@playwright/test'
  2  | import { TaskListPage } from './page-objects/TaskListPage'
  3  | 
  4  | test.describe('任务列表页 E2E 测试', () => {
  5  |   let taskListPage: TaskListPage
  6  | 
  7  |   test.beforeEach(async ({ page }) => {
  8  |     taskListPage = new TaskListPage(page)
  9  |     await taskListPage.navigate()
  10 |   })
  11 | 
  12 |   test('4.2 任务列表页加载 - 应显示页面标题', async ({ page }) => {
  13 |     await expect(taskListPage.pageTitle).toBeVisible()
  14 |   })
  15 | 
  16 |   test('4.2 任务列表页加载 - 应显示任务表格', async ({ page }) => {
  17 |     await expect(taskListPage.taskTable).toBeVisible()
  18 |   })
  19 | 
  20 |   test('4.2 任务列表页加载 - 应显示创建按钮', async ({ page }) => {
  21 |     await expect(taskListPage.createButton).toBeVisible()
  22 |   })
  23 | 
  24 |   test('4.3 空列表状态 - 无任务时显示空状态提示', async ({ page }) => {
  25 |     // 假设后端没有数据时显示空状态
  26 |     const emptyState = page.locator('text=/暂无|空列表|no tasks/i')
  27 |     // 根据实际实现，可能为空或显示空表格
  28 |     if (await emptyState.isVisible() || await taskListPage.taskTable.isVisible()) {
  29 |       // 通过：页面正常渲染
  30 |     }
  31 |   })
  32 | 
  33 |   test('4.4 任务行操作按钮 - 应能点击编辑按钮', async ({ page }) => {
  34 |     // 查找编辑按钮（如果存在任务）
  35 |     const editButton = page.getByRole('button', { name: /编辑|Edit/i }).first()
  36 |     if (await editButton.isVisible()) {
  37 |       // 编辑按钮可见时，点击应有响应（跳转到配置页或打开弹窗）
  38 |       await editButton.click()
  39 |       // 验证页面变化（编辑弹窗或导航）
  40 |       await expect(page).not.toHaveURL(/\/tasks$/) // 应该离开列表页
  41 |     }
  42 |   })
  43 | 
  44 |   test('4.4 任务行操作按钮 - 应能点击删除按钮', async ({ page }) => {
  45 |     const deleteButton = page.getByRole('button', { name: /删除|Delete/i }).first()
  46 |     if (await deleteButton.isVisible()) {
  47 |       await deleteButton.click()
  48 |       // 确认删除对话框出现
  49 |       const confirmDialog = page.locator('.el-message-box, [role="dialog"]')
> 50 |       await expect(confirmDialog).toBeVisible()
     |                                   ^ Error: expect(locator).toBeVisible() failed
  51 |     }
  52 |   })
  53 | 
  54 |   test('4.5 启用/停用切换 - 应能切换任务状态', async ({ page }) => {
  55 |     const toggle = page.locator('input[type="checkbox"]').first()
  56 |     if (await toggle.isVisible()) {
  57 |       const initialState = await toggle.isChecked()
  58 |       await toggle.click()
  59 |       // 状态应该改变
  60 |       const newState = await toggle.isChecked()
  61 |       expect(newState).not.toBe(initialState)
  62 |     }
  63 |   })
  64 | 
  65 |   test('4.4 任务行操作按钮 - 点击创建按钮应导航到创建页', async ({ page }) => {
  66 |     await taskListPage.clickCreateButton()
  67 |     // TaskConfig.vue handles creation at /tasks/new
  68 |     await expect(page).toHaveURL(/\/tasks\/new/)
  69 |   })
  70 | })
  71 | 
```