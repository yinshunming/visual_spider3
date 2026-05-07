import { Page, Locator, expect } from '@playwright/test'
import { BasePage } from './BasePage'

/**
 * 任务创建页页面对象 - 使用 TaskConfigPage（TaskConfig.vue 处理创建和编辑）
 */
export class TaskCreatePage extends BasePage {
  readonly url = '/tasks/new' // 实际路由是 /tasks/new，不是 /tasks/create
  readonly formTitle: Locator
  readonly taskNameInput: Locator
  readonly taskUrlInput: Locator
  readonly taskTypeSelect: Locator
  readonly submitButton: Locator
  readonly cancelButton: Locator

  constructor(page: Page) {
    super(page)
    // TaskConfig.vue uses .el-card__header span with text "新建任务"
    this.formTitle = page.locator('.el-card__header span')
    this.taskNameInput = page.getByLabel(/任务名称/i)
    this.taskUrlInput = page.getByLabel(/列表页URL|种子URL/i)
    // 使用更具体的选择器 - Element Plus radiogroup
    this.taskTypeSelect = page.locator('[id^="el-id"].el-radio-group')
    this.submitButton = page.getByRole('button', { name: /创建任务/i })
    this.cancelButton = page.getByRole('button', { name: /^取消$/i })
  }

  /**
   * 导航到任务创建页
   */
  async navigate(): Promise<void> {
    await this.goto(this.url)
    await this.waitForLoadState()
  }

  /**
   * 填写任务名称
   */
  async fillTaskName(name: string): Promise<void> {
    await this.taskNameInput.fill(name)
  }

  /**
   * 填写任务 URL
   */
  async fillTaskUrl(url: string): Promise<void> {
    await this.taskUrlInput.fill(url)
  }

  /**
   * 选择任务类型
   */
  async selectTaskType(type: 'LIST_PAGE' | 'DIRECT_URL'): Promise<void> {
    // 使用 el-radio-group 选择器直接点击对应 radio
    const radioLabel = type === 'LIST_PAGE' ? '列表页模式' : '直接URL模式'
    await this.page.locator('.el-radio').filter({ hasText: radioLabel }).click()
  }

  /**
   * 提交表单
   */
  async submit(): Promise<void> {
    await this.submitButton.click()
  }

  /**
   * 取消创建
   */
  async cancel(): Promise<void> {
    await this.cancelButton.click()
  }

  /**
   * 获取验证错误信息
   */
  async getValidationError(): Promise<string> {
    // Element Plus 验证错误可能同时显示多个
    const errorElements = this.page.locator('.el-form-item__error')
    const count = await errorElements.count()
    if (count === 0) return ''
    if (count === 1) return await errorElements.first().textContent() || ''
    // 返回所有错误，用分号分隔
    const texts = await errorElements.allTextContents()
    return texts.join('; ')
  }

  /**
   * 检查表单是否有效
   */
  async isFormValid(): Promise<boolean> {
    const submitButton = this.submitButton
    const isDisabled = await submitButton.isDisabled()
    return !isDisabled
  }
}
