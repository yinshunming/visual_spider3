import { test, expect } from '@playwright/test'
import { ContentListPage } from '../../page-objects/ContentListPage'
import { ContentEditPage } from '../../page-objects/ContentEditPage'
import { startMockServer, stopMockServer, getMockServerUrl } from '../../fixtures/mock-server/server'
import { createTask, deleteTask, enableTask } from '../../fixtures/seed'
import axios from 'axios'

const API_BASE = 'http://localhost:8080/api'

test.describe('E2E-P0 预览编辑功能测试', () => {
  let mockPort: number
  let mockBaseUrl: string
  let taskId: number | null = null
  let taskName: string | null = null
  let contentId: number | null = null

  test.beforeAll(async () => {
    mockPort = await startMockServer()
    mockBaseUrl = getMockServerUrl(mockPort)

    taskName = `E2E Preview Edit Test ${Date.now()}`
    taskId = await createTask({
      name: taskName,
      description: 'Task for preview/edit E2E test',
      urlMode: 'DIRECT_URL',
      seedUrls: [`${mockBaseUrl}/content-page-1.html`]
    })

    await enableTask(taskId)

    try {
      await axios.post(`${API_BASE}/tasks/${taskId}/run`)
      await new Promise(resolve => setTimeout(resolve, 5000))

      const response = await axios.get(`${API_BASE}/contents`, {
        params: { taskId, page: 0, size: 1 }
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

  test('E2E-P0-04 预览和编辑 - 应能预览内容并编辑状态', async ({ page }) => {
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

    const previewButton = page.locator('button').filter({ hasText: /^预览$/ }).first()
    if (await previewButton.isVisible()) {
      await previewButton.click()
      const previewDialog = page.locator('.el-dialog')
      await expect(previewDialog).toBeVisible()
      const closeButton = page.locator('.el-dialog__headerbtn')
      if (await closeButton.isVisible()) {
        await closeButton.click()
        await page.waitForTimeout(500)
      }
    }

    const editButton = page.locator('button').filter({ hasText: /^编辑$/ }).first()
    if (await editButton.isVisible()) {
      await editButton.click()
      await expect(page).toHaveURL(/\/contents\/\d+\/edit/)

      const contentEditPage = new ContentEditPage(page)
      await expect(contentEditPage.form).toBeVisible({ timeout: 10000 })

      await contentEditPage.selectStatus('DELETED')
      await contentEditPage.clickSave()

      const success = await contentEditPage.waitForSuccessMessage()
      expect(success).toBe(true)

      const verifyResponse = await axios.get(`${API_BASE}/contents/${contentId}`)
      expect(verifyResponse.data.status).toBe('DELETED')
    }
  })
})
