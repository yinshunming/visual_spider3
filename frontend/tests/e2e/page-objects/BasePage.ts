import { Page, Locator, expect } from '@playwright/test'

/**
 * 基础页面对象类
 * 提供所有页面对象公用的方法和属性
 */
export class BasePage {
  protected page: Page
  protected baseURL: string = 'http://localhost:3000'

  constructor(page: Page) {
    this.page = page
  }

  /**
   * 导航到指定路径
   */
  async goto(path: string): Promise<void> {
    await this.page.goto(`${this.baseURL}${path}`)
  }

  /**
   * 等待页面加载完成
   */
  async waitForLoadState(state: 'load' | 'domcontentloaded' | 'networkidle' = 'load'): Promise<void> {
    await this.page.waitForLoadState(state)
  }

  /**
   * 获取页面标题
   */
  async getTitle(): Promise<string> {
    return this.page.title()
  }

  /**
   * 点击按钮
   */
  async clickButton(text: string): Promise<void> {
    const button = this.page.getByRole('button', { name: text })
    await button.click()
  }

  /**
   * 填写输入框
   */
  async fillInput(label: string, value: string): Promise<void> {
    const input = this.page.getByLabel(label)
    await input.fill(value)
  }

  /**
   * 检查元素是否存在
   */
  async elementExists(selector: string): Promise<boolean> {
    const element = this.page.locator(selector)
    return await element.count() > 0
  }

  /**
   * 等待元素可见
   */
  async waitForSelector(selector: string, timeout?: number): Promise<void> {
    await this.page.waitForSelector(selector, { timeout })
  }

  /**
   * 获取文本内容
   */
  async getText(selector: string): Promise<string> {
    return await this.page.locator(selector).textContent() || ''
  }
}
