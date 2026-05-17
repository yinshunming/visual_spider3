import { Page, Locator, expect } from '@playwright/test'
import { BasePage } from './BasePage'

export class EmbeddedBrowserPage extends BasePage {
  readonly urlInput: Locator
  readonly loadButton: Locator
  readonly refreshButton: Locator
  readonly closeButton: Locator
  readonly cssSelectorInput: Locator
  readonly xpathSelectorInput: Locator
  readonly copyCssButton: Locator
  readonly copyXpathButton: Locator
  readonly testCssButton: Locator
  readonly testXpathButton: Locator
  readonly screenshotImage: Locator
  readonly emptyState: Locator
  readonly loadingMask: Locator

  private sessionId: string | null = null

  constructor(page: Page) {
    super(page)
    this.urlInput = page.locator('input[placeholder*="输入URL"]')
    this.loadButton = page.locator('button:has-text("加载页面")')
    this.refreshButton = page.locator('button:has-text("刷新")')
    this.closeButton = page.locator('button:has-text("关闭浏览器")')
    this.cssSelectorInput = page.locator('textarea[readonly]').first()
    this.xpathSelectorInput = page.locator('textarea[readonly]').nth(1)
    this.copyCssButton = page.locator('button:has-text("复制 CSS")')
    this.copyXpathButton = page.locator('button:has-text("复制 XPath")')
    this.testCssButton = page.locator('button:has-text("测试")').first()
    this.testXpathButton = page.locator('button:has-text("测试")').nth(1)
    this.screenshotImage = page.locator('.screenshot-image')
    this.emptyState = page.locator('.empty-state')
    this.loadingMask = page.locator('.loading-mask')
  }

  setSessionId(sessionId: string): void {
    this.sessionId = sessionId
  }

  async cleanup(): Promise<void> {
    // 尝试通过 API 关闭 session（需要 sessionId）
    if (this.sessionId) {
      try {
        await fetch(`/api/playwright/sessions/${this.sessionId}`, { method: 'DELETE' })
      } catch (e) {
        // ignore errors during cleanup
      }
      this.sessionId = null
    }

    // 尝试通过 UI 关闭 session
    try {
      if (await this.closeButton.isVisible({ timeout: 500 })) {
        await this.closeButton.click()
        await this.page.waitForTimeout(1000)
      }
    } catch (e) {
      // button not visible or timeout, ignore
    }

    // 尝试等待空状态出现
    try {
      await this.emptyState.waitFor({ state: 'visible', timeout: 2000 })
    } catch (e) {
      // empty state not visible, ignore
    }
  }

  async navigate(taskId?: number): Promise<void> {
    if (taskId) {
      await this.goto(`/tasks/${taskId}`)
    } else {
      await this.goto('/tasks/new')
    }
    await this.waitForLoadState()
  }

  async loadUrl(url: string): Promise<void> {
    await this.urlInput.fill(url)
    await this.loadButton.click()
    await this.waitForScreenshot()
  }

  async waitForScreenshot(options?: { timeout?: number }): Promise<void> {
    await this.page.waitForSelector('.screenshot-image', { state: 'visible', timeout: options?.timeout || 30000 })
  }

  async waitForLoading(): Promise<void> {
    await this.loadingMask.waitFor({ state: 'visible' })
    await this.loadingMask.waitFor({ state: 'hidden' })
  }

  async clickOnScreenshot(x: number, y: number): Promise<void> {
    await this.screenshotImage.click({ position: { x, y } })
  }

  async getCssSelector(): Promise<string> {
    return await this.cssSelectorInput.inputValue()
  }

  async getXpathSelector(): Promise<string> {
    return await this.xpathSelectorInput.inputValue()
  }

  async isScreenshotVisible(): Promise<boolean> {
    return await this.screenshotImage.isVisible()
  }

  async isEmptyStateVisible(): Promise<boolean> {
    return await this.emptyState.isVisible()
  }
}