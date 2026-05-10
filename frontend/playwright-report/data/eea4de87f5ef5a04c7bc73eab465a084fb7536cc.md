# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: content-edit.spec.ts >> 内容编辑页 E2E 测试 >> E2E-033 修改状态 - 应能选择新状态
- Location: tests\e2e\content-edit.spec.ts:56:3

# Error details

```
Test timeout of 30000ms exceeded.
```

```
Error: locator.click: Test timeout of 30000ms exceeded.
Call log:
  - waiting for locator('.el-form').locator('.el-select')

```

# Page snapshot

```yaml
- generic [ref=e4]:
  - heading "Visual Spider - 可视化爬虫配置" [level=2] [ref=e6]
  - main [ref=e7]:
    - generic [ref=e8]:
      - generic [ref=e9]:
        - generic [ref=e11] [cursor=pointer]:
          - generic:
            - combobox [ref=e13]
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
```

# Test source

```ts
  1  | import { Page, Locator } from '@playwright/test'
  2  | import { BasePage } from './BasePage'
  3  | 
  4  | /**
  5  |  * 内容编辑页面对象
  6  |  */
  7  | export class ContentEditPage extends BasePage {
  8  |   readonly urlPattern: RegExp = /\/contents\/\d+\/edit/
  9  |   readonly form: Locator
  10 |   readonly saveButton: Locator
  11 |   readonly backButton: Locator
  12 |   readonly statusSelect: Locator
  13 |   readonly sourceUrlInput: Locator
  14 | 
  15 |   constructor(page: Page) {
  16 |     super(page)
  17 |     this.form = page.locator('.el-form')
  18 |     this.saveButton = page.getByRole('button', { name: /保存/i })
  19 |     this.backButton = page.getByRole('button', { name: /返回/i })
  20 |     this.statusSelect = page.locator('.el-select')
  21 |     this.sourceUrlInput = page.locator('input[readonly]').first()
  22 |   }
  23 | 
  24 |   async isFormVisible(): Promise<boolean> {
  25 |     return await this.form.isVisible()
  26 |   }
  27 | 
  28 |   async isSaveButtonVisible(): Promise<boolean> {
  29 |     return await this.saveButton.isVisible()
  30 |   }
  31 | 
  32 |   async isBackButtonVisible(): Promise<boolean> {
  33 |     return await this.backButton.isVisible()
  34 |   }
  35 | 
  36 |   async getSourceUrlValue(): Promise<string> {
  37 |     return await this.sourceUrlInput.inputValue()
  38 |   }
  39 | 
  40 |   async selectStatus(status: 'PENDING' | 'PUBLISHED' | 'DELETED'): Promise<void> {
  41 |     const statusDropdown = this.page.locator('.el-form').locator('.el-select')
> 42 |     await statusDropdown.click()
     |                          ^ Error: locator.click: Test timeout of 30000ms exceeded.
  43 |     const option = this.page.locator('.el-select-dropdown__item').filter({ hasText: status })
  44 |     await option.click()
  45 |   }
  46 | 
  47 |   async getFieldInputCount(): Promise<number> {
  48 |     const inputs = this.form.locator('textarea')
  49 |     return await inputs.count()
  50 |   }
  51 | 
  52 |   async updateField(fieldName: string, newValue: string): Promise<void> {
  53 |     const fieldLabel = this.form.locator('.el-form-item__label').filter({ hasText: fieldName })
  54 |     const textarea = fieldLabel.locator('~div textarea')
  55 |     await textarea.fill(newValue)
  56 |   }
  57 | 
  58 |   async clickSave(): Promise<void> {
  59 |     await this.saveButton.click()
  60 |   }
  61 | 
  62 |   async clickBack(): Promise<void> {
  63 |     await this.backButton.click()
  64 |   }
  65 | 
  66 |   async waitForSuccessMessage(): Promise<boolean> {
  67 |     try {
  68 |       const message = this.page.locator('.el-message--success')
  69 |       await message.waitFor({ timeout: 5000 })
  70 |       return true
  71 |     } catch {
  72 |       return false
  73 |     }
  74 |   }
  75 | }
  76 | 
```