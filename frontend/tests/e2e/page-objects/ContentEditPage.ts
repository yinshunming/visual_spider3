import { Page, Locator } from '@playwright/test'
import { BasePage } from './BasePage'

/**
 * 内容编辑页面对象
 */
export class ContentEditPage extends BasePage {
  readonly urlPattern: RegExp = /\/contents\/\d+\/edit/
  readonly form: Locator
  readonly saveButton: Locator
  readonly backButton: Locator
  readonly statusSelect: Locator
  readonly sourceUrlInput: Locator

  constructor(page: Page) {
    super(page)
    this.form = page.locator('.el-form')
    this.saveButton = page.getByRole('button', { name: /保存/i })
    this.backButton = page.getByRole('button', { name: /返回/i })
    this.statusSelect = page.locator('.el-select')
    this.sourceUrlInput = page.locator('input[readonly]').first()
  }

  async isFormVisible(): Promise<boolean> {
    return await this.form.isVisible()
  }

  async isSaveButtonVisible(): Promise<boolean> {
    return await this.saveButton.isVisible()
  }

  async isBackButtonVisible(): Promise<boolean> {
    return await this.backButton.isVisible()
  }

  async getSourceUrlValue(): Promise<string> {
    return await this.sourceUrlInput.inputValue()
  }

  async selectStatus(status: 'PENDING' | 'PUBLISHED' | 'DELETED'): Promise<void> {
    const statusDropdown = this.page.locator('.el-form').locator('.el-select')
    await statusDropdown.click()
    const option = this.page.locator('.el-select-dropdown__item').filter({ hasText: status })
    await option.click()
  }

  async getFieldInputCount(): Promise<number> {
    const inputs = this.form.locator('textarea')
    return await inputs.count()
  }

  async updateField(fieldName: string, newValue: string): Promise<void> {
    const fieldLabel = this.form.locator('.el-form-item__label').filter({ hasText: fieldName })
    const textarea = fieldLabel.locator('~div textarea')
    await textarea.fill(newValue)
  }

  async clickSave(): Promise<void> {
    await this.saveButton.click()
  }

  async clickBack(): Promise<void> {
    await this.backButton.click()
  }

  async waitForSuccessMessage(): Promise<boolean> {
    try {
      const message = this.page.locator('.el-message--success')
      await message.waitFor({ timeout: 5000 })
      return true
    } catch {
      return false
    }
  }
}
