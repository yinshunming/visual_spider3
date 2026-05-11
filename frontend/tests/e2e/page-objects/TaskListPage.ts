import { Page, Locator, expect } from '@playwright/test'
import { BasePage } from './BasePage'

/**
 * 任务列表页页面对象
 */
export class TaskListPage extends BasePage {
  readonly url = '/tasks'
  readonly pageTitle: Locator
  readonly createButton: Locator
  readonly taskTable: Locator
  readonly emptyState: Locator

  constructor(page: Page) {
    super(page)
    // TaskList.vue 没有 h1/h2，使用 .task-list 或 el-table 作为主要标识
    this.pageTitle = page.locator('.task-list')
    this.createButton = page.getByRole('button', { name: /新建任务|创建任务/i })
    this.taskTable = page.locator('.el-table')
    this.emptyState = page.locator('text=/暂无数据/i')
  }

  /**
   * 导航到任务列表页
   */
  async navigate(): Promise<void> {
    await this.goto(this.url)
    await this.waitForLoadState()
  }

  /**
   * 检查页面标题是否显示
   */
  async hasPageTitle(): Promise<boolean> {
    return await this.pageTitle.isVisible()
  }

  /**
   * 检查创建按钮是否存在
   */
  async hasCreateButton(): Promise<boolean> {
    return await this.createButton.isVisible()
  }

  /**
   * 检查任务表格是否存在
   */
  async hasTaskTable(): Promise<boolean> {
    return await this.taskTable.isVisible()
  }

  /**
   * 检查是否有任务数据
   */
  async hasTasks(): Promise<boolean> {
    return !(await this.emptyState.isVisible())
  }

  /**
   * 点击创建任务按钮
   */
  async clickCreateButton(): Promise<void> {
    await this.createButton.click()
  }

  /**
   * 获取任务行数 - Element Plus el-table body tr
   */
  async getTaskRowCount(): Promise<number> {
    const rows = this.page.locator('.el-table__body tr')
    return await rows.count()
  }

  /**
   * 点击指定任务的配置按钮
   */
  async clickEditTask(taskId: number): Promise<void> {
    // 根据 taskId 找到对应行的配置按钮
    const row = this.page.locator('.el-table__body tr').filter({ hasText: String(taskId) })
    const editButton = row.locator('button').filter({ hasText: /^配置$/ })
    await editButton.click()
  }

  /**
   * 点击指定任务的删除按钮
   */
  async clickDeleteTask(taskId: number): Promise<void> {
    const row = this.page.locator('.el-table__body tr').filter({ hasText: String(taskId) })
    const deleteButton = row.locator('button').filter({ hasText: /^删除$/ })
    await deleteButton.first().click()
  }

  /**
   * 确认删除对话框
   */
  async confirmDelete(): Promise<void> {
    // Element Plus 确认框
    const confirmButton = this.page.locator('.el-message-box__wrapper button').filter({ hasText: /确定/i })
    await confirmButton.click()
  }

  /**
   * 点击启用按钮
   */
  async clickEnableTask(taskId: number): Promise<void> {
    const row = this.page.locator('.el-table__body tr').filter({ hasText: String(taskId) })
    const enableButton = row.locator('button').filter({ hasText: /^启用$/ })
    await enableButton.first().click()
  }

  /**
   * 点击停用按钮
   */
  async clickDisableTask(taskId: number): Promise<void> {
    const row = this.page.locator('.el-table__body tr').filter({ hasText: String(taskId) })
    const disableButton = row.locator('button').filter({ hasText: /^停用$/ })
    await disableButton.first().click()
  }
}
