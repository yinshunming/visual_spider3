import { test, expect } from '@playwright/test'
import { TaskListPage } from '../../page-objects/TaskListPage'
import { ContentListPage } from '../../page-objects/ContentListPage'
import { startMockServer, stopMockServer, getMockServerUrl } from '../../fixtures/mock-server/server'
import { createTask, deleteTask } from '../../fixtures/seed'
import axios from 'axios'

const API_BASE = 'http://localhost:8080/api'

test.describe('E2E-P0 核心链路测试', () => {
  let mockPort: number
  let mockBaseUrl: string
  let taskId: number | null = null
  let taskName: string | null = null

  test.beforeAll(async () => {
    mockPort = await startMockServer()
    mockBaseUrl = getMockServerUrl(mockPort)
  })

  test.afterAll(async () => {
    await stopMockServer()
    if (taskId) {
      await deleteTask(taskId)
    }
  })

  async function pollTaskStatus(maxAttempts = 10, interval = 3000): Promise<string> {
    for (let i = 0; i < maxAttempts; i++) {
      try {
        const response = await axios.get(`${API_BASE}/tasks/${taskId}`)
        const status = response.data.status
        if (status === 'ENABLED' || status === 'DISABLED' || status === 'DRAFT') {
          return status
        }
      } catch (e) {
        // Task might not exist yet
      }
      await new Promise(resolve => setTimeout(resolve, interval))
    }
    throw new Error('Task status polling timeout')
  }

  test('E2E-P0-01 完整主链路测试', async ({ page }) => {
    const taskListPage = new TaskListPage(page)
    const contentListPage = new ContentListPage(page)

    taskName = `E2E Main Flow Test ${Date.now()}`
    taskId = await createTask({
      name: taskName,
      urlMode: 'LIST_PAGE',
      listPageUrl: `${mockBaseUrl}/list.html`,
      listPageRule: {
        containerSelector: 'div.article-list > div.item',
        itemUrlSelector: 'a.title'
      },
      contentPageRule: {
        fields: [
          { fieldName: 'title', fieldLabel: '标题', selector: 'h1.title', selectorType: 'CSS', extractType: 'text' },
          { fieldName: 'content', fieldLabel: '正文', selector: 'div.article-content', selectorType: 'CSS', extractType: 'html' }
        ]
      },
      status: 'DRAFT'
    })

    const response = await axios.get(`${API_BASE}/tasks/${taskId}`)
    expect(response.data.status).toBe('DRAFT')

    await axios.put(`${API_BASE}/tasks/${taskId}`, {
      name: response.data.name,
      urlMode: 'LIST_PAGE',
      listPageUrl: `${mockBaseUrl}/list.html`,
      listPageRule: response.data.listPageRule,
      contentPageRule: response.data.contentPageRule,
      status: 'DRAFT'
    })

    await axios.post(`${API_BASE}/tasks/${taskId}/enable`)

    let currentStatus = await pollTaskStatus()
    expect(currentStatus).toBe('ENABLED')

    await axios.post(`${API_BASE}/tasks/${taskId}/run`)

    await page.waitForTimeout(3000)

    currentStatus = await pollTaskStatus()
    expect(['ENABLED', 'RUNNING', 'DISABLED']).toContain(currentStatus)

    await contentListPage.navigate()
    await page.waitForTimeout(2000)

    if (taskName) {
      await contentListPage.selectTaskFilter(taskName)
      await page.waitForTimeout(1000)
    }

    const hasContent = await contentListPage.hasContent()
    console.log('Has content:', hasContent)
  })
})
