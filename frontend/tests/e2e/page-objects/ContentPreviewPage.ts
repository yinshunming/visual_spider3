import { Page, Locator, FrameLocator } from '@playwright/test'
import { BasePage } from './BasePage'

/**
 * 内容预览页面对象
 */
export class ContentPreviewPage extends BasePage {
  readonly dialog: Locator
  readonly closeButton: Locator
  readonly previewIframe: FrameLocator

  constructor(page: Page) {
    super(page)
    this.dialog = page.locator('.el-dialog')
    this.closeButton = page.getByRole('button', { name: /关闭/i })
    this.previewIframe = page.frameLocator('.preview-iframe')
  }

  async isDialogVisible(): Promise<boolean> {
    return await this.dialog.isVisible()
  }

  async clickClose(): Promise<void> {
    await this.closeButton.click()
  }

  async getDialogTitle(): Promise<string> {
    return await this.dialog.locator('.el-dialog__header span').textContent() || ''
  }

  async getIdValue(): Promise<string> {
    const idCell = this.dialog.locator('.el-descriptions__cell').filter({ hasText: /^ID$/ }).first()
    return await idCell.locator('..').textContent() || ''
  }

  async getStatusValue(): Promise<string> {
    const statusCell = this.dialog.locator('.el-descriptions__cell').filter({ hasText: /^状态$/ }).first()
    return await statusCell.locator('..').textContent() || ''
  }

  async getSourceUrlValue(): Promise<string> {
    const urlCell = this.dialog.locator('.el-descriptions__cell').filter({ hasText: /^来源URL$/ }).first()
    return await urlCell.locator('..').textContent() || ''
  }

  async getFieldsTableRowCount(): Promise<number> {
    const table = this.dialog.locator('.el-table')
    const rows = table.locator('tbody tr')
    return await rows.count()
  }

  async hasIframe(): Promise<boolean> {
    const iframe = this.page.locator('.preview-iframe')
    return await iframe.isVisible()
  }

  async getIframeSandboxAttribute(): Promise<string | null> {
    const iframe = this.page.locator('.preview-iframe')
    return await iframe.getAttribute('sandbox')
  }
}
