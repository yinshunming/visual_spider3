<template>
  <div class="content-list">
    <div class="toolbar">
      <el-select v-model="filterTaskId" placeholder="按任务筛选" clearable @change="handleTaskFilterChange" style="width: 300px; margin-right: 16px;">
        <el-option v-for="task in tasks" :key="task.id" :label="task.name" :value="task.id" />
      </el-select>
      <el-button type="primary" @click="handleExport">导出</el-button>
    </div>
    <el-table :data="contents" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="sourceUrl" label="来源URL" min-width="300" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handlePreview(row)">预览</el-button>
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="loadContents" @current-change="loadContents" style="margin-top: 20px; justify-content: center;" />

    <!-- 预览对话框 -->
    <ContentPreview v-model="previewVisible" :content="currentContent" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getContents, deleteContent, exportContent, getTasks } from '../api'
import ContentPreview from './ContentPreview.vue'

const contents = ref([])
const tasks = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const filterTaskId = ref(null)
const previewVisible = ref(false)
const currentContent = ref(null)

const statusType = (status) => ({ PENDING: 'info', PUBLISHED: 'success', DELETED: 'danger' })[status] || 'info'
const statusLabel = (status) => ({ PENDING: '待发布', PUBLISHED: '已发布', DELETED: '已删除' })[status] || status
const formatDate = (dateStr) => dateStr ? new Date(dateStr).toLocaleString('zh-CN') : '-'

const loadTasks = async () => {
  try {
    const res = await getTasks({ page: 0, size: 1000 })
    tasks.value = res.data.content || res.data || []
  } catch (err) {
    console.error('Failed to load tasks:', err)
  }
}

const loadContents = async () => {
  loading.value = true
  try {
    const params = { page: page.value - 1, size: pageSize.value }
    if (filterTaskId.value) {
      params.taskId = filterTaskId.value
    }
    const res = await getContents(params)
    contents.value = res.data.content || res.data || []
    total.value = res.data.totalElements || contents.value.length
  } catch (err) {
    ElMessage.error('加载内容列表失败')
    console.error(err)
  } finally {
    loading.value = false
  }
}

const handleTaskFilterChange = () => {
  page.value = 1
  loadContents()
}

const handlePreview = (row) => {
  currentContent.value = row
  previewVisible.value = true
}

const handleEdit = (row) => {
  window.location.href = `/contents/${row.id}/edit`
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这条内容吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteContent(row.id)
      ElMessage.success('删除成功')
      loadContents()
    } catch (err) {
      ElMessage.error('删除失败')
      console.error(err)
    }
  }).catch(() => {})
}

const handleExport = async () => {
  try {
    const params = {}
    if (filterTaskId.value) {
      params.taskId = filterTaskId.value
    }
    const res = await exportContent(params)
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `content-export-${Date.now()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (err) {
    ElMessage.error('导出失败')
    console.error(err)
  }
}

onMounted(() => {
  loadTasks()
  loadContents()
})
</script>

<style scoped>
.content-list {
  padding: 20px;
}

.toolbar {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}
</style>