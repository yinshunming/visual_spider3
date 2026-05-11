import { test, expect } from '@playwright/test'
import { createTask, deleteTask, enableTask } from '../../fixtures/seed'
import axios from 'axios'

const API_BASE = 'http://localhost:8080/api'

test.describe('E2E-P1 任务操作测试', () => {
  let taskId: number | null = null

  test.afterEach(async () => {
    if (taskId) {
      await deleteTask(taskId)
      taskId = null
    }
  })

  test('E2E-P1-04 任务启用/停用 - 状态切换正常', async () => {
    taskId = await createTask({
      name: `E2E Enable Disable Test ${Date.now()}`,
      description: 'Task for enable/disable test',
      urlMode: 'DIRECT_URL',
      seedUrls: ['http://example.com/test'],
      status: 'DRAFT'
    })

    let response = await axios.get(`${API_BASE}/tasks/${taskId}`)
    expect(response.data.status).toBe('DRAFT')

    await enableTask(taskId)
    response = await axios.get(`${API_BASE}/tasks/${taskId}`)
    expect(response.data.status).toBe('ENABLED')

    await axios.post(`${API_BASE}/tasks/${taskId}/disable`)
    response = await axios.get(`${API_BASE}/tasks/${taskId}`)
    expect(response.data.status).toBe('DISABLED')
  })

  test('E2E-P1-05 任务删除 - 删除后任务不存在', async () => {
    taskId = await createTask({
      name: `E2E Delete Test ${Date.now()}`,
      description: 'Task for delete test',
      urlMode: 'DIRECT_URL',
      seedUrls: ['http://example.com/test'],
      status: 'DRAFT'
    })

    await axios.delete(`${API_BASE}/tasks/${taskId}`)

    await expect(async () => {
      await axios.get(`${API_BASE}/tasks/${taskId}`)
    }).rejects.toThrow()
  })
})
