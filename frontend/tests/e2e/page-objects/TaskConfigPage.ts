import { Page, Locator, expect } from '@playwright/test'
import { BasePage } from './BasePage'

/**
 * 任务配置页页面对象
 */
export class TaskConfigPage extends BasePage {
  readonly formTitle: Locator
  readonly taskNameInput: Locator
  readonly taskUrlInput: Locator
  readonly addFieldButton: Locator
  readonly fieldRows: Locator
  readonly saveButton: Locator
  readonly discardButton: Locator

  constructor(page: Page) {
    super(page)
    // TaskConfig.vue uses el-card with header "编辑任务" or "新建任务"
    this.formTitle = page.locator('.el-card__header span')
    this.taskNameInput = page.getByLabel(/任务名称/i)
    this.taskUrlInput = page.getByLabel(/列表页URL|种子URL/i)
    this.addFieldButton = page.getByRole('button', { name: /\+ 添加字段/i })
    this.fieldRows = page.locator('.field-item')
    this.saveButton = page.getByRole('button', { name: /创建任务|保存修改/i })
    this.discardButton = page.getByRole('button', { name: /取消/i })
  }

  /**
   * 导航到任务配置页（编辑模式）
   */
  async navigate(taskId: number): Promise<void> {
    await this.goto(`/tasks/${taskId}`)
    await this.waitForLoadState()
  }

  /**
   * 导航到任务创建页
   */
  async navigateToCreate(): Promise<void> {
    await this.goto('/tasks/new')
    await this.waitForLoadState()
  }

  /**
   * 获取当前任务名称
   */
  async getTaskName(): Promise<string> {
    return await this.taskNameInput.inputValue()
  }

  /**
   * 填写任务名称
   */
  async fillTaskName(name: string): Promise<void> {
    await this.taskNameInput.fill(name)
  }

  /**
   * 获取当前任务 URL
   */
  async getTaskUrl(): Promise<string> {
    return await this.taskUrlInput.inputValue()
  }

  /**
   * 填写任务 URL
   */
  async fillTaskUrl(url: string): Promise<void> {
    await this.taskUrlInput.fill(url)
  }

  /**
   * 点击添加字段按钮
   */
  async addField(): Promise<void> {
    await this.addFieldButton.click()
  }

  /**
   * 获取字段行数
   */
  async getFieldRowCount(): Promise<number> {
    return await this.fieldRows.count()
  }

  /**
   * 配置指定字段
   */
  async configureField(rowIndex: number, name: string, type: string, selector: string): Promise<void> {
    const fieldNameInput = this.fieldRows.nth(rowIndex).locator('input').filter({ hasText: /字段名|Field Name/i })
    const fieldTypeSelect = this.fieldRows.nth(rowIndex).locator('select')
    const selectorInput = this.fieldRows.nth(rowIndex).locator('input').filter({ hasText: /选择器|Selector/i })

    await fieldNameInput.fill(name)
    await fieldTypeSelect.selectOption({ label: type })
    await selectorInput.fill(selector)
  }

  /**
   * 保存配置
   */
  async save(): Promise<void> {
    await this.saveButton.click()
  }

  /**
   * 放弃更改
   */
  async discard(): Promise<void> {
    await this.discardButton.click()
  }

  /**
   * 检查保存按钮是否可用
   */
  async isSaveEnabled(): Promise<boolean> {
    return !(await this.saveButton.isDisabled())
  }
}
