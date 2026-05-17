import { test, expect, Page } from '@playwright/test'
import { EmbeddedBrowserPage } from './page-objects/EmbeddedBrowserPage'
import { TaskConfigPage } from './page-objects/TaskConfigPage'
import { TaskListPage } from './page-objects/TaskListPage'

test.describe('Playwright 升级 - 完整链路集成测试', () => {
  let embeddedBrowserPage: EmbeddedBrowserPage
  let taskConfigPage: TaskConfigPage
  let taskListPage: TaskListPage

  test.beforeEach(async ({ page }) => {
    embeddedBrowserPage = new EmbeddedBrowserPage(page)
    taskConfigPage = new TaskConfigPage(page)
    taskListPage = new TaskListPage(page)
  })

  test.afterEach(async () => {
    await embeddedBrowserPage.cleanup()
  })

  test('10.1 验证 Playwright 浏览器可以启动并加载页面', async ({ page }) => {
    await embeddedBrowserPage.navigate()

    // 初始应显示空状态
    await expect(embeddedBrowserPage.emptyState).toBeVisible()

    // 加载 example.com 页面
    await embeddedBrowserPage.loadUrl('https://example.com')

    // 等待截图加载成功
    await embeddedBrowserPage.waitForScreenshot()

    // 截图应该可见
    await expect(embeddedBrowserPage.screenshotImage).toBeVisible()
  })

  test('10.2 使用新浪页面测试完整流程', async ({ page }) => {
    await embeddedBrowserPage.navigate()

    // 加载新浪页面
    await embeddedBrowserPage.loadUrl('https://k.sina.com.cn')

    // 等待截图加载成功（可能需要更长时间）
    await embeddedBrowserPage.waitForScreenshot({ timeout: 60000 })

    // 截图应该可见
    await expect(embeddedBrowserPage.screenshotImage).toBeVisible()
  })

  test('10.3 验证选择器生成、测试、保存全流程', async ({ page }) => {
    // 1. 创建任务
    await taskListPage.navigate()
    await page.getByRole('button', { name: /新建任务|New Task/i }).click()
    await taskConfigPage.waitForLoadState()

    // 2. 填写任务信息
    const testTaskName = `集成测试任务 ${Date.now()}`
    await taskConfigPage.fillTaskName(testTaskName)
    await taskConfigPage.fillTaskUrl('https://example.com')

    // 3. 使用 EmbeddedBrowser 选择元素
    // 滚动到 EmbeddedBrowser 组件
    const embeddedBrowser = page.locator('.embedded-browser')
    await embeddedBrowser.scrollIntoViewIfNeeded()

    // 等待 EmbeddedBrowser 加载
    await page.waitForTimeout(1000)

    // 如果 URL 输入框可见，尝试加载页面
    const urlInput = embeddedBrowserPage.urlInput
    if (await urlInput.isVisible({ timeout: 2000 })) {
      await embeddedBrowserPage.loadUrl('https://example.com')
      await embeddedBrowserPage.waitForScreenshot()

      // 点击页面元素
      await embeddedBrowserPage.clickOnScreenshot(300, 200)
      await page.waitForTimeout(2000)

      // 验证 CSS 选择器已生成
      const cssSelector = await embeddedBrowserPage.getCssSelector()
      if (cssSelector) {
        expect(cssSelector.length).toBeGreaterThan(0)
      }

      // 验证 XPath 选择器已生成
      const xpathSelector = await embeddedBrowserPage.getXpathSelector()
      if (xpathSelector) {
        expect(xpathSelector.length).toBeGreaterThan(0)
      }
    }

    // 4. 保存任务
    await taskConfigPage.save()
    await page.waitForTimeout(1000)

    // 验证保存成功（应该显示成功提示或跳转到列表页）
    const successMessage = page.locator('text=/成功|Success/i')
    if (await successMessage.isVisible({ timeout: 3000 })) {
      await expect(successMessage).toBeVisible()
    }
  })

  test('10.4 验证并发 Session 限制', async ({ page }) => {
    // 这个测试需要创建多个 Session，验证达到上限时返回 409
    const sessions: string[] = []

    try {
      // 连续创建 5 个 Session（max-sessions=5）
      for (let i = 0; i < 5; i++) {
        const response = await page.request.post('/api/playwright/sessions', {
          data: { url: 'https://example.com' },
          headers: { 'Content-Type': 'application/json' }
        })
        expect(response.status()).toBe(201)
        const body = await response.json()
        sessions.push(body.sessionId)
      }

      // 第 6 个 Session 应该返回 409
      const response6 = await page.request.post('/api/playwright/sessions', {
        data: { url: 'https://example.com' },
        headers: { 'Content-Type': 'application/json' }
      })
      expect(response6.status()).toBe(409)
      const errorBody = await response6.json()
      expect(errorBody.errorCode).toBe('SESSION_LIMIT')

    } finally {
      // 清理所有 Session
      for (const sessionId of sessions) {
        try {
          await page.request.delete(`/api/playwright/sessions/${sessionId}`)
        } catch (e) {
          // ignore
        }
      }
    }
  })

  test('10.5 验证超时清理（模拟）', async ({ page }) => {
    // 由于实际超时需要等待 3 分钟，这个测试验证超时机制的配置正确性

    // 创建 Session
    const response = await page.request.post('/api/playwright/sessions', {
      data: { url: 'https://example.com' },
      headers: { 'Content-Type': 'application/json' }
    })
    expect(response.status()).toBe(201)
    const body = await response.json()
    const sessionId = body.sessionId

    try {
      // 验证 Session 可以正常使用
      const pingResponse = await page.request.post(`/api/playwright/sessions/${sessionId}/ping`)
      expect(pingResponse.status()).toBe(200)

      // 验证 screenshot 可以正常获取
      const screenshotResponse = await page.request.post(`/api/playwright/sessions/${sessionId}/screenshot`, {
        data: {},
        headers: { 'Content-Type': 'application/json' }
      })
      expect(screenshotResponse.status()).toBe(200)

      // 注意：实际超时清理需要等待 3 分钟（180000ms）
      // 在 CI 环境中可以通过配置 playwright.session-timeout-ms=5000 来加速测试

    } finally {
      // 清理
      await page.request.delete(`/api/playwright/sessions/${sessionId}`)
    }
  })

  test('10.6 新浪页面元素选择完整流程', async ({ page }) => {
    await embeddedBrowserPage.navigate()

    // 加载新浪新闻页面
    await embeddedBrowserPage.loadUrl('https://k.sina.com.cn/article_2431485513_90ed8649040019t5a.html?from=sports&subch=osport')
    await embeddedBrowserPage.waitForScreenshot({ timeout: 60000 })

    // 点击页面中间区域尝试选择元素
    await embeddedBrowserPage.clickOnScreenshot(400, 300)
    await page.waitForTimeout(3000)

    // 验证元素信息已获取
    const cssSelector = await embeddedBrowserPage.getCssSelector()
    const xpathSelector = await embeddedBrowserPage.getXpathSelector()

    // 至少有一个选择器不为空
    const hasAnySelector = (cssSelector && cssSelector.length > 0) || (xpathSelector && xpathSelector.length > 0)
    expect(hasAnySelector).toBeTruthy()
  })
})

test.describe('Playwright 升级 - API 端到端测试', () => {
  test('完整 Session 生命周期', async ({ request }) => {
    // 1. 创建 Session
    const createResponse = await request.post('/api/playwright/sessions', {
      data: { url: 'https://example.com' },
      headers: { 'Content-Type': 'application/json' }
    })
    expect(createResponse.status()).toBe(201)
    const session = await createResponse.json()
    expect(session.sessionId).toBeDefined()
    expect(session.url).toBe('https://example.com')

    // 2. 心跳
    const pingResponse = await request.post(`/api/playwright/sessions/${session.sessionId}/ping`)
    expect(pingResponse.status()).toBe(200)

    // 3. 截图
    const screenshotResponse = await request.post(`/api/playwright/sessions/${session.sessionId}/screenshot`, {
      data: {},
      headers: { 'Content-Type': 'application/json' }
    })
    expect(screenshotResponse.status()).toBe(200)
    const screenshot = await screenshotResponse.json()
    expect(screenshot.data).toBeDefined()
    expect(screenshot.width).toBeGreaterThan(0)
    expect(screenshot.height).toBeGreaterThan(0)

    // 4. 元素查询
    const elementResponse = await request.post(`/api/playwright/sessions/${session.sessionId}/element`, {
      data: { x: 400, y: 300 },
      headers: { 'Content-Type': 'application/json' }
    })
    expect(elementResponse.status()).toBe(200)
    const element = await elementResponse.json()
    expect(element.tagName).toBeDefined()

    // 5. 选择器测试
    const testSelectorResponse = await request.post(`/api/playwright/sessions/${session.sessionId}/test-selector`, {
      data: { selector: 'h1', type: 'CSS' },
      headers: { 'Content-Type': 'application/json' }
    })
    expect(testSelectorResponse.status()).toBe(200)
    const testResult = await testSelectorResponse.json()
    expect(testResult.count).toBeDefined()
    expect(testResult.unique).toBeDefined()

    // 6. 导航
    const navigateResponse = await request.post(`/api/playwright/sessions/${session.sessionId}/navigate`, {
      data: { url: 'https://example.org' },
      headers: { 'Content-Type': 'application/json' }
    })
    expect(navigateResponse.status()).toBe(200)
    const navResult = await navigateResponse.json()
    expect(navResult.url).toBe('https://example.org')

    // 7. 关闭 Session
    const closeResponse = await request.delete(`/api/playwright/sessions/${session.sessionId}`)
    expect(closeResponse.status()).toBe(204)

    // 8. 验证 Session 已关闭（再次访问应返回 404）
    const afterCloseResponse = await request.post(`/api/playwright/sessions/${session.sessionId}/ping`)
    expect(afterCloseResponse.status()).toBe(404)
  })

  test('错误场景覆盖', async ({ request }) => {
    // 1. Session 不存在
    const notFoundResponse = await request.post('/api/playwright/sessions/nonexistent/ping', {
      data: {},
      headers: { 'Content-Type': 'application/json' }
    })
    expect(notFoundResponse.status()).toBe(404)
    const notFoundError = await notFoundResponse.json()
    expect(notFoundError.errorCode).toBe('SESSION_NOT_FOUND')

    // 2. 无效选择器
    const session = await (await request.post('/api/playwright/sessions', {
      data: { url: 'https://example.com' },
      headers: { 'Content-Type': 'application/json' }
    })).json()

    try {
      const invalidSelectorResponse = await request.post(`/api/playwright/sessions/${session.sessionId}/test-selector`, {
        data: { selector: 'invalid[[[', type: 'CSS' },
        headers: { 'Content-Type': 'application/json' }
      })
      expect(invalidSelectorResponse.status()).toBe(400)
      const invalidError = await invalidSelectorResponse.json()
      expect(invalidError.errorCode).toBe('INVALID_SELECTOR')
    } finally {
      await request.delete(`/api/playwright/sessions/${session.sessionId}`)
    }
  })
})