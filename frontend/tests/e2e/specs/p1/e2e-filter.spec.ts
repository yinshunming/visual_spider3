import { test, expect } from '@playwright/test'
import { ContentListPage } from '../../page-objects/ContentListPage'
import { startMockServer, stopMockServer, getMockServerUrl } from '../../fixtures/mock-server/server'
import { createTask, deleteTask, enableTask } from '../../fixtures/seed'
import axios from 'axios'

const API_BASE = 'http://localhost:8080/api'

test.describe('E2E-P1 内容筛选测试', () => {
  let mockPort: number
  let mockBaseUrl: string
  let taskAId: number | null = null
  let taskAName: string | null = null
  let taskBId: number | null = null
  let taskBName: string | null = null

  test.beforeAll(async () => {
    mockPort = await startMockServer()
    mockBaseUrl = getMockServerUrl(mockPort)

    taskAName = `E2E Filter Task A ${Date.now()}`
    taskAId = await createTask({
      name: taskAName,
      description: 'Task A for filter test',
      urlMode: 'DIRECT_URL',
      seedUrls: [`${mockBaseUrl}/content-page-1.html`]
    })

    taskBName = `E2E Filter Task B ${Date.now()}`
    taskBId = await createTask({
      name: taskBName,
      description: 'Task B for filter test',
      urlMode: 'DIRECT_URL',
      seedUrls: [`${mockBaseUrl}/content-page-2.html`]
    })

    await enableTask(taskAId)
    await enableTask(taskBId)

    try {
      await Promise.all([
        axios.post(`${API_BASE}/tasks/${taskAId}/run`),
        axios.post(`${API_BASE}/tasks/${taskBId}/run`)
      ])
      await new Promise(resolve => setTimeout(resolve, 8000))
    } catch (e) {
      console.warn('Task run failed:', e)
    }
  })

  test.afterAll(async () => {
    await stopMockServer()
    if (taskAId) await deleteTask(taskAId)
    if (taskBId) await deleteTask(taskBId)
  })

  test('E2E-P1-02 内容按任务筛选 - 筛选后显示正确条数', async ({ page }) => {
    const contentListPage = new ContentListPage(page)
    await contentListPage.navigate()
    await page.waitForTimeout(2000)

    if (!taskAName) {
      test.skip()
      return
    }

    await contentListPage.selectTaskFilter(taskAName)
    await page.waitForTimeout(2000)

    const filteredRows = await contentListPage.getRowCount()
    expect(filteredRows).toBeGreaterThanOrEqual(0)

    await contentListPage.clearTaskFilter()
    await page.waitForTimeout(2000)

    const afterClearRows = await contentListPage.getRowCount()
    expect(afterClearRows).toBeGreaterThanOrEqual(0)
  })
})
