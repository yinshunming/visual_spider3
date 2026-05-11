import { test, expect } from '@playwright/test'
import { TaskListPage } from '../../page-objects/TaskListPage'
import { startMockServer, stopMockServer, getMockServerUrl } from '../../fixtures/mock-server/server'
import { createTask, deleteTask, enableTask } from '../../fixtures/seed'
import axios from 'axios'

const API_BASE = 'http://localhost:8080/api'

test.describe('E2E-P1 执行日志测试', () => {
  let mockPort: number
  let mockBaseUrl: string
  let taskId: number | null = null

  test.beforeAll(async () => {
    mockPort = await startMockServer()
    mockBaseUrl = getMockServerUrl(mockPort)

    taskId = await createTask({
      name: `E2E Execution Log Test ${Date.now()}`,
      description: 'Task for execution log E2E test',
      urlMode: 'DIRECT_URL',
      seedUrls: [`${mockBaseUrl}/content-page-1.html`]
    })

    await enableTask(taskId)

    try {
      await axios.post(`${API_BASE}/tasks/${taskId}/run`)
      await new Promise(resolve => setTimeout(resolve, 5000))
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

  test('E2E-P1-03 执行日志查看 - 应能查看执行记录', async ({ page }) => {
    const response = await axios.get(`${API_BASE}/executions`, { params: { taskId } })
    expect(response.status).toBe(200)
  })
})
