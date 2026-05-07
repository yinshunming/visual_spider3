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

export default api
