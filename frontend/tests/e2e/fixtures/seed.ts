import axios from 'axios'

const API_BASE = 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

export interface SeedTask {
  id?: number
  name: string
  description?: string
  urlMode: 'LIST_PAGE' | 'DIRECT_URL'
  listPageUrl?: string
  seedUrls?: string[]
  listPageRule?: object
  contentPageRule?: object
  scheduleCron?: string
  status?: 'DRAFT' | 'ENABLED' | 'DISABLED' | 'RUNNING'
}

export interface FieldRequest {
  fieldName: string
  fieldLabel: string
  fieldType: 'text' | 'image' | 'link' | 'richText'
  selector: string
  selectorType: 'CSS' | 'XPATH'
  extractType: 'text' | 'attr' | 'html'
  attrName?: string
  required?: boolean
  defaultValue?: string
  displayOrder?: number
}

export async function createTask(task: SeedTask): Promise<number> {
  const fields: FieldRequest[] = [
    { fieldName: 'title', fieldLabel: '标题', fieldType: 'text', selector: 'h1.title', selectorType: 'CSS', extractType: 'text' },
    { fieldName: 'content', fieldLabel: '正文', fieldType: 'richText', selector: 'div.content', selectorType: 'CSS', extractType: 'html' }
  ]

  const requestData: Record<string, any> = {
    name: task.name,
    description: task.description,
    urlMode: task.urlMode,
    listPageUrl: task.listPageUrl,
    seedUrls: task.seedUrls,
    listPageRule: task.listPageRule ? JSON.stringify(task.listPageRule) : undefined,
    contentPageRule: task.contentPageRule ? JSON.stringify(task.contentPageRule) : undefined,
    scheduleCron: task.scheduleCron,
    fields: fields,
    status: task.status
  }

  const response = await api.post('/tasks', requestData)
  return response.data.id
}

export async function updateTask(taskId: number, task: Partial<SeedTask>): Promise<void> {
  const requestData: Record<string, any> = {}
  if (task.name) requestData.name = task.name
  if (task.urlMode) requestData.urlMode = task.urlMode
  if (task.listPageUrl) requestData.listPageUrl = task.listPageUrl
  if (task.seedUrls) requestData.seedUrls = task.seedUrls
  if (task.listPageRule) requestData.listPageRule = JSON.stringify(task.listPageRule)
  if (task.contentPageRule) requestData.contentPageRule = JSON.stringify(task.contentPageRule)
  if (task.scheduleCron) requestData.scheduleCron = task.scheduleCron

  await api.put(`/tasks/${taskId}`, requestData)
}

export async function deleteTask(taskId: number): Promise<void> {
  try {
    await api.delete(`/tasks/${taskId}`)
  } catch (error) {
    console.warn(`Failed to delete task ${taskId}:`, error)
  }
}

export async function waitForBackend(timeout: number = 30000): Promise<void> {
  const start = Date.now()
  while (Date.now() - start < timeout) {
    try {
      await api.get('/tasks', { params: { page: 0, size: 1 } })
      return
    } catch {
      await new Promise(resolve => setTimeout(resolve, 1000))
    }
  }
  throw new Error('Backend not available after timeout')
}

export async function enableTask(taskId: number): Promise<void> {
  await api.post(`/tasks/${taskId}/enable`)
}

export async function runTask(taskId: number): Promise<void> {
  await api.post(`/tasks/${taskId}/run`)
}
