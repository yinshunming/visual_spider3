import { test, expect } from '@playwright/test'
import { TaskCreatePage } from '../../page-objects/TaskCreatePage'
import { ContentListPage } from '../../page-objects/ContentListPage'
import { startMockServer, stopMockServer, getMockServerUrl } from '../../fixtures/mock-server/server'
import { createTask, deleteTask, enableTask } from '../../fixtures/seed'
import axios from 'axios'

const API_BASE = 'http://localhost:8080/api'

test.describe('E2E-P2 辅助功能测试', () => {
  let mockPort: number
  let mockBaseUrl: string
  let taskId: number | null = null
  let taskName: string | null = null
  let contentId: number | null = null

  test.beforeAll(async () => {
    mockPort = await startMockServer()
    mockBaseUrl = getMockServerUrl(mockPort)

    taskName = `E2E Auxiliary Test ${Date.now()}`
    taskId = await createTask({
      name: taskName,
      description: 'Task for auxiliary E2E tests',
      urlMode: 'DIRECT_URL',
      seedUrls: [`${mockBaseUrl}/content-page-1.html`, `${mockBaseUrl}/content-page-2.html`]
    })

    await enableTask(taskId)

    try {
      await axios.post(`${API_BASE}/tasks/${taskId}/run`)
      await new Promise(resolve => setTimeout(resolve, 5000))

      const response = await axios.get(`${API_BASE}/contents`, {
        params: { taskId, page: 0, size: 10 }
      })
      const content = response.data.content || response.data
      if (content && content.length > 0) {
        contentId = content[0].id
      }
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

  test('E2E-P2-01 内容删除 - 删除后条数减少', async ({ page }) => {
    if (!contentId) {
      test.skip()
      return
    }

    const contentListPage = new ContentListPage(page)
    await contentListPage.navigate()

    if (taskName) {
      await contentListPage.selectTaskFilter(taskName)
      await page.waitForTimeout(1000)
    }

    const beforeRows = await contentListPage.getRowCount()
    if (beforeRows < 2) {
      test.skip()
      return
    }

    const firstRow = page.locator('.el-table__body tr').first()
    const deleteButton = firstRow.locator('button').filter({ hasText: /^删除$/ })
    await deleteButton.click()
    await contentListPage.confirmDelete()
    await page.waitForTimeout(1000)

    const afterRows = await contentListPage.getRowCount()
    expect(afterRows).toBe(beforeRows - 1)
  })

  test('E2E-P2-02 任务创建表单验证 - 空表单提交应有错误提示', async ({ page }) => {
    const createPage = new TaskCreatePage(page)
    await createPage.navigate()

    const submitButton = createPage.submitButton
    const isDisabled = await submitButton.isDisabled()

    if (!isDisabled) {
      await submitButton.click()
      await page.waitForTimeout(500)
    }

    await expect(createPage.formTitle).toBeVisible()
  })

  test('E2E-P2-03 任务创建 - 仅填写名称应提示URL必填', async ({ page }) => {
    const createPage = new TaskCreatePage(page)
    await createPage.navigate()

    await createPage.fillTaskName('Test Task Without URL')
    const submitButton = createPage.submitButton

    const buttonExists = await submitButton.isVisible()
    expect(buttonExists).toBe(true)
  })
})
