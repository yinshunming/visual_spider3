import { Page, Locator, expect } from '@playwright/test'
import { BasePage } from './BasePage'

/**
 * 内容列表页页面对象
 */
export class ContentListPage extends BasePage {
  readonly url = '/contents'
  readonly pageTitle: Locator
  readonly table: Locator
  readonly pagination: Locator
  readonly taskFilter: Locator
  readonly exportButton: Locator
  readonly emptyState: Locator

  constructor(page: Page) {
    super(page)
    this.pageTitle = page.locator('.content-list')
    this.table = page.locator('.el-table')
    this.pagination = page.locator('.el-pagination')
    this.taskFilter = page.locator('.el-select').first()
    this.exportButton = page.getByRole('button', { name: /导出/i })
    this.emptyState = page.locator('text=/暂无数据/i')
  }

  async navigate(): Promise<void> {
    await this.goto(this.url)
    await this.waitForLoadState()
  }

  async hasTable(): Promise<boolean> {
    return await this.table.isVisible()
  }

  async hasPagination(): Promise<boolean> {
    return await this.pagination.isVisible()
  }

  async hasExportButton(): Promise<boolean> {
    return await this.exportButton.isVisible()
  }

  async hasContent(): Promise<boolean> {
    return !(await this.emptyState.isVisible())
  }

  async getRowCount(): Promise<number> {
    const rows = this.page.locator('.el-table__body tr')
    return await rows.count()
  }

  async clickPreview(contentId: number): Promise<void> {
    const row = this.page.locator('.el-table__body tr').filter({ hasText: String(contentId) })
    const previewButton = row.locator('button').filter({ hasText: /^预览$/ })
    await previewButton.click()
  }

  async clickEdit(contentId: number): Promise<void> {
    const row = this.page.locator('.el-table__body tr').filter({ hasText: String(contentId) })
    const editButton = row.locator('button').filter({ hasText: /^编辑$/ })
    await editButton.click()
  }

  async clickDelete(contentId: number): Promise<void> {
    const row = this.page.locator('.el-table__body tr').filter({ hasText: String(contentId) })
    const deleteButton = row.locator('button').filter({ hasText: /^删除$/ })
    await deleteButton.click()
  }

  async confirmDelete(): Promise<void> {
    const confirmButton = this.page.getByRole('button', { name: /确定/i })
    await confirmButton.click()
  }

  async cancelDelete(): Promise<void> {
    const cancelButton = this.page.getByRole('button', { name: /取消/i })
    await cancelButton.click()
  }

  async selectTaskFilter(taskId: number): Promise<void> {
    await this.taskFilter.click()
    const option = this.page.locator('.el-select-dropdown__item').filter({ hasText: String(taskId) })
    await option.click()
  }

  async clearTaskFilter(): Promise<void> {
    const clearButton = this.taskFilter.locator('.el-input__clear')
    if (await clearButton.isVisible()) {
      await clearButton.click()
    }
  }

  async changePageSize(size: number): Promise<void> {
    const sizeSelect = this.page.locator('.el-pagination__sizes .el-select')
    await sizeSelect.click()
    const option = this.page.locator('.el-select-dropdown__item').filter({ hasText: String(size) })
    await option.click()
  }
}
