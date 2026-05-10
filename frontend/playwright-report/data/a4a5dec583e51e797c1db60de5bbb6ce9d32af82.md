# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: content-list.spec.ts >> 内容列表页 E2E 测试 >> E2E-004 按任务筛选 - 应能选择任务筛选
- Location: tests\e2e\content-list.spec.ts:30:3

# Error details

```
Test timeout of 30000ms exceeded.
```

```
Error: locator.click: Test timeout of 30000ms exceeded.
Call log:
  - waiting for locator('.el-select-dropdown__item').first()
    - locator resolved to <li role="option" id="el-id-1689-4" aria-selected="false" class="el-select-dropdown__item">…</li>
  - attempting click action
    2 × waiting for element to be visible, enabled and stable
      - element is not visible
    - retrying click action
    - waiting 20ms
    2 × waiting for element to be visible, enabled and stable
      - element is not visible
    - retrying click action
      - waiting 100ms
    55 × waiting for element to be visible, enabled and stable
       - element is not visible
     - retrying click action
       - waiting 500ms

```

# Page snapshot

```yaml
- generic [ref=e1]:
  - generic [ref=e4]:
    - heading "Visual Spider - 可视化爬虫配置" [level=2] [ref=e6]
    - main [ref=e7]:
      - generic [ref=e8]:
        - generic [ref=e9]:
          - generic [ref=e11] [cursor=pointer]:
            - generic:
              - combobox [expanded] [active] [ref=e13]
              - generic [ref=e14]: 按任务筛选
            - img [ref=e17]
          - button "导出" [ref=e19] [cursor=pointer]:
            - generic [ref=e20]: 导出
        - generic [ref=e22]:
          - table [ref=e24]:
            - rowgroup [ref=e31]:
              - row "ID 来源URL 状态 创建时间 操作" [ref=e32]:
                - columnheader "ID" [ref=e33]:
                  - generic [ref=e34]: ID
                - columnheader "来源URL" [ref=e35]:
                  - generic [ref=e36]: 来源URL
                - columnheader "状态" [ref=e37]:
                  - generic [ref=e38]: 状态
                - columnheader "创建时间" [ref=e39]:
                  - generic [ref=e40]: 创建时间
                - columnheader "操作" [ref=e41]:
                  - generic [ref=e42]: 操作
          - generic [ref=e46]:
            - table:
              - rowgroup
            - generic [ref=e48]: No Data
        - generic [ref=e49]:
          - generic [ref=e50]: Total 0
          - generic [ref=e53] [cursor=pointer]:
            - generic:
              - combobox [ref=e55]
              - generic [ref=e56]: 20/page
            - img [ref=e59]
          - button "Go to previous page" [disabled] [ref=e61]:
            - generic:
              - img
          - list [ref=e62]:
            - listitem "page 1" [ref=e63]: "1"
          - button "Go to next page" [disabled] [ref=e64]:
            - generic:
              - img
  - tooltip "No data" [ref=e65]:
    - generic [ref=e67]: No data
```

# Test source

```ts
  1   | import { test, expect, Page } from '@playwright/test'
  2   | import { ContentListPage } from './page-objects/ContentListPage'
  3   | 
  4   | test.describe('内容列表页 E2E 测试', () => {
  5   |   let contentListPage: ContentListPage
  6   | 
  7   |   test.beforeEach(async ({ page }) => {
  8   |     contentListPage = new ContentListPage(page)
  9   |     await contentListPage.navigate()
  10  |   })
  11  | 
  12  |   test('E2E-001 页面加载 - 应显示内容表格', async () => {
  13  |     await expect(contentListPage.table).toBeVisible()
  14  |   })
  15  | 
  16  |   test('E2E-002 显示内容列表 - 应显示 sourceUrl、status、createdAt 列', async () => {
  17  |     if (await contentListPage.hasContent()) {
  18  |       const rows = await contentListPage.getRowCount()
  19  |       expect(rows).toBeGreaterThan(0)
  20  |     }
  21  |   })
  22  | 
  23  |   test('E2E-003 分页切换 - 应能切换每页条数', async ({ page }) => {
  24  |     if (await contentListPage.hasContent()) {
  25  |       await contentListPage.changePageSize(50)
  26  |       await expect(contentListPage.table).toBeVisible()
  27  |     }
  28  |   })
  29  | 
  30  |   test('E2E-004 按任务筛选 - 应能选择任务筛选', async ({ page }) => {
  31  |     const filterSelect = contentListPage.taskFilter
  32  |     if (await filterSelect.isVisible()) {
  33  |       await filterSelect.click()
  34  |       const options = page.locator('.el-select-dropdown__item')
  35  |       const count = await options.count()
  36  |       if (count > 0) {
> 37  |         await options.first().click()
      |                               ^ Error: locator.click: Test timeout of 30000ms exceeded.
  38  |         await expect(contentListPage.table).toBeVisible()
  39  |       }
  40  |     }
  41  |   })
  42  | 
  43  |   test('E2E-005 清除筛选 - 应能清除任务筛选', async () => {
  44  |     await contentListPage.clearTaskFilter()
  45  |     await expect(contentListPage.table).toBeVisible()
  46  |   })
  47  | 
  48  |   test('E2E-006 点击预览按钮 - 应能点击预览', async ({ page }) => {
  49  |     if (await contentListPage.hasContent()) {
  50  |       const firstRow = page.locator('.el-table__body tr').first()
  51  |       const previewButton = firstRow.locator('button').filter({ hasText: /^预览$/ })
  52  |       if (await previewButton.isVisible()) {
  53  |         await previewButton.click()
  54  |         await expect(page.locator('.el-dialog')).toBeVisible()
  55  |       }
  56  |     }
  57  |   })
  58  | 
  59  |   test('E2E-007 点击编辑按钮 - 应能点击编辑', async ({ page }) => {
  60  |     if (await contentListPage.hasContent()) {
  61  |       const firstRow = page.locator('.el-table__body tr').first()
  62  |       const editButton = firstRow.locator('button').filter({ hasText: /^编辑$/ })
  63  |       if (await editButton.isVisible()) {
  64  |         await editButton.click()
  65  |         await expect(page).toHaveURL(/\/contents\/\d+\/edit/)
  66  |       }
  67  |     }
  68  |   })
  69  | 
  70  |   test('E2E-008 点击删除按钮 - 应显示确认对话框', async ({ page }) => {
  71  |     if (await contentListPage.hasContent()) {
  72  |       const firstRow = page.locator('.el-table__body tr').first()
  73  |       const deleteButton = firstRow.locator('button').filter({ hasText: /^删除$/ })
  74  |       if (await deleteButton.isVisible()) {
  75  |         await deleteButton.click()
  76  |         await expect(page.locator('.el-message-box')).toBeVisible()
  77  |       }
  78  |     }
  79  |   })
  80  | 
  81  |   test('E2E-009 确认删除 - 点击确定应关闭对话框', async ({ page }) => {
  82  |     if (await contentListPage.hasContent()) {
  83  |       const firstRow = page.locator('.el-table__body tr').first()
  84  |       const deleteButton = firstRow.locator('button').filter({ hasText: /^删除$/ })
  85  |       if (await deleteButton.isVisible()) {
  86  |         await deleteButton.click()
  87  |         await contentListPage.confirmDelete()
  88  |         await expect(page.locator('.el-message-box')).not.toBeVisible()
  89  |       }
  90  |     }
  91  |   })
  92  | 
  93  |   test('E2E-010 取消删除 - 点击取消应关闭对话框', async ({ page }) => {
  94  |     if (await contentListPage.hasContent()) {
  95  |       const firstRow = page.locator('.el-table__body tr').first()
  96  |       const deleteButton = firstRow.locator('button').filter({ hasText: /^删除$/ })
  97  |       if (await deleteButton.isVisible()) {
  98  |         await deleteButton.click()
  99  |         await contentListPage.cancelDelete()
  100 |         await expect(page.locator('.el-message-box')).not.toBeVisible()
  101 |       }
  102 |     }
  103 |   })
  104 | })
  105 | 
```