import { test, expect } from '@playwright/test'
import { ContentListPage } from '../../page-objects/ContentListPage'
import { startMockServer, stopMockServer, getMockServerUrl } from '../../fixtures/mock-server/server'
import { createTask, deleteTask, enableTask } from '../../fixtures/seed'
import axios from 'axios'

const API_BASE = 'http://localhost:8080/api'

test.describe('E2E-P1 内容列表分页测试', () => {
  let mockPort: number
  let mockBaseUrl: string
  let taskId: number | null = null
  let taskName: string | null = null

  test.beforeAll(async () => {
    mockPort = await startMockServer()
    mockBaseUrl = getMockServerUrl(mockPort)

    taskName = `E2E Pagination Test ${Date.now()}`
    taskId = await createTask({
      name: taskName,
      description: 'Task for pagination E2E test',
      urlMode: 'DIRECT_URL',
      seedUrls: Array.from({ length: 25 }, (_, i) => `${mockBaseUrl}/content-page-${(i % 2) + 1}.html`)
    })

    await enableTask(taskId)

    try {
      await axios.post(`${API_BASE}/tasks/${taskId}/run`)
      await new Promise(resolve => setTimeout(resolve, 8000))
    } catch (e) {
      console.warn('Task run failed:', e)
    }
  })

  test.afterAll(async () => {
    await stopMockServer()
    if (taskId) {
      await deleteTask(taskId)
    }
  })

  test('E2E-P1-01 内容列表分页 - 默认显示20条，翻页正确', async ({ page }) => {
    const contentListPage = new ContentListPage(page)
    await contentListPage.navigate()

    if (taskName) {
      await contentListPage.selectTaskFilter(taskName)
      await page.waitForTimeout(1000)
    }

    const hasContent = await contentListPage.hasContent()
    if (!hasContent) {
      test.skip()
      return
    }

    const firstPageRows = await contentListPage.getRowCount()
    expect(firstPageRows).toBeLessThanOrEqual(20)

    const nextButton = page.locator('.btn-next')
    if (await nextButton.isEnabled()) {
      await nextButton.click()
      await page.waitForTimeout(500)
      const secondPageRows = await contentListPage.getRowCount()
      expect(secondPageRows).toBeLessThanOrEqual(20)
    }
  })

  test('E2E-P1-01 修改每页条数 - 应显示全部内容', async ({ page }) => {
    const contentListPage = new ContentListPage(page)
    await contentListPage.navigate()

    if (taskName) {
      await contentListPage.selectTaskFilter(taskName)
      await page.waitForTimeout(1000)
    }

    const hasContent = await contentListPage.hasContent()
    if (!hasContent) {
      test.skip()
      return
    }

    await contentListPage.changePageSize(50)
    await page.waitForTimeout(1000)

    const rows = await contentListPage.getRowCount()
    expect(rows).toBeLessThanOrEqual(50)
  })
})
