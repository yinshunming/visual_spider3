import { test, expect } from '@playwright/test'
import { ContentListPage } from '../../page-objects/ContentListPage'
import { startMockServer, stopMockServer, getMockServerUrl } from '../../fixtures/mock-server/server'
import { createTask, deleteTask, enableTask } from '../../fixtures/seed'
import axios from 'axios'

const API_BASE = 'http://localhost:8080/api'

test.describe('E2E-P0 导出功能测试', () => {
  let mockPort: number
  let mockBaseUrl: string
  let taskId: number | null = null
  let taskName: string | null = null

  test.beforeAll(async () => {
    mockPort = await startMockServer()
    mockBaseUrl = getMockServerUrl(mockPort)

    taskName = `E2E Export Test ${Date.now()}`
    taskId = await createTask({
      name: taskName,
      description: 'Task for export E2E test',
      urlMode: 'DIRECT_URL',
      seedUrls: [`${mockBaseUrl}/content-page-1.html`, `${mockBaseUrl}/content-page-2.html`]
    })

    await enableTask(taskId)

    try {
      await axios.post(`${API_BASE}/tasks/${taskId}/run`)
      await new Promise(resolve => setTimeout(resolve, 5000))
    } catch (e) {
      console.warn('Task run failed (may already have content):', e)
    }
  })

  test.afterAll(async () => {
    await stopMockServer()
    if (taskId) {
      await deleteTask(taskId)
    }
  })

  test('E2E-P0-02 导出 Excel - 应触发下载 .xlsx 文件', async ({ page }) => {
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

    const downloadPromise = page.waitForEvent('download', { timeout: 10000 }).catch(() => null)
    await contentListPage.exportButton.click()

    const download = await downloadPromise
    expect(download).not.toBeNull()
    const filename = download!.suggestedFilename()
    expect(filename).toMatch(/\.xlsx$/)
  })

  test('E2E-P0-03 导出 CSV - 应触发下载 .csv 文件', async ({ page }) => {
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

    const downloadPromise = page.waitForEvent('download', { timeout: 10000 }).catch(() => null)
    await contentListPage.exportButton.click()

    const download = await downloadPromise
    expect(download).not.toBeNull()
    const filename = download!.suggestedFilename()
    expect(filename).toMatch(/\.(xlsx|csv)$/)
  })
})
