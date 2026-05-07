<template>
  <div class="task-list">
    <div class="toolbar">
      <el-button type="primary" @click="$router.push('/tasks/new')">新建任务</el-button>
    </div>
    <el-table :data="tasks" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="任务名称" min-width="200" />
      <el-table-column prop="urlMode" label="模式" width="120">
        <template #default="{ row }">
          {{ row.urlMode === 'LIST_PAGE' ? '列表页' : '直接URL' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push(`/tasks/${row.id}`)">配置</el-button>
          <el-button size="small" type="success" @click="handleEnable(row)" :disabled="row.status === 'ENABLED' || row.status === 'RUNNING'">启用</el-button>
          <el-button size="small" type="warning" @click="handleDisable(row)" :disabled="row.status === 'DISABLED' || row.status === 'RUNNING'">停用</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)" :disabled="row.status === 'RUNNING'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadTasks" @current-change="loadTasks" style="margin-top: 20px; justify-content: center;" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTasks, enableTask, disableTask, deleteTask } from '../api'

const tasks = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const statusType = (status) => ({ DRAFT: 'info', ENABLED: 'success', DISABLED: 'warning', RUNNING: 'primary' })[status] || 'info'
const statusLabel = (status) => ({ DRAFT: '草稿', ENABLED: '已启用', DISABLED: '已停用', RUNNING: '运行中' })[status] || status
const formatDate = (dateStr) => dateStr ? new Date(dateStr).toLocaleString('zh-CN') : '-'

const loadTasks = async () => {
  loading.value = true
  try {
    const res = await getTasks({ page: page.value - 1, size: pageSize.value })
    tasks.value = res.data.content || res.data
    total.value = res.data.totalElements || tasks.value.length
  } catch { ElMessage.error('加载任务失败') } finally { loading.value = false }
}
const handleEnable = async (task) => { await enableTask(task.id); ElMessage.success('任务已启用'); loadTasks() }
const handleDisable = async (task) => { await disableTask(task.id); ElMessage.success('任务已停用'); loadTasks() }
const handleDelete = async (task) => {
  await ElMessageBox.confirm('确定要删除该任务吗？', '提示', { type: 'warning' })
  await deleteTask(task.id); ElMessage.success('任务已删除'); loadTasks()
}
onMounted(loadTasks)
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
</style>
