import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 任务列表（分页）
export function getTasks(params) {
  return api.get('/tasks', { params })
}

// 获取单个任务
export function getTask(id) {
  return api.get(`/tasks/${id}`)
}

// 创建任务
export function createTask(data) {
  return api.post('/tasks', data)
}

// 更新任务
export function updateTask(id, data) {
  return api.put(`/tasks/${id}`, data)
}

// 删除任务
export function deleteTask(id) {
  return api.delete(`/tasks/${id}`)
}

// 启用任务
export function enableTask(id) {
  return api.post(`/tasks/${id}/enable`)
}

// 停用任务
export function disableTask(id) {
  return api.post(`/tasks/${id}/disable`)
}

// 运行任务
export function runTask(id) {
  return api.post(`/tasks/${id}/run`)
}

// 内容列表（分页）
export function getContents(params) {
  return api.get('/contents', { params })
}

// 获取单个内容
export function getContent(id) {
  return api.get(`/contents/${id}`)
}

// 更新内容
export function updateContent(id, data) {
  return api.put(`/contents/${id}`, data)
}

// 删除内容
export function deleteContent(id) {
  return api.delete(`/contents/${id}`)
}

// 导出内容
export function exportContent(params) {
  return api.get('/contents/export', { params, responseType: 'blob' })
}

export default api
