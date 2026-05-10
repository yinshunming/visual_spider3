<template>
  <el-dialog v-model="visible" title="内容预览" width="900px" @close="handleClose">
    <div v-if="content" class="preview-content">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ content.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusLabel(content.status) }}</el-descriptions-item>
        <el-descriptions-item label="来源URL" :span="2">{{ content.sourceUrl }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(content.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ content.publishedAt ? formatDate(content.publishedAt) : '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">字段内容</el-divider>

      <el-table :data="fieldTableData" stripe size="small">
        <el-table-column prop="key" label="字段名" width="150" />
        <el-table-column prop="value" label="值">
          <template #default="{ row }">
            <span v-if="row.isHtml" v-html="row.value"></span>
            <span v-else>{{ row.value }}</span>
          </template>
        </el-table-column>
      </el-table>

      <template v-if="content.rawHtml">
        <el-divider content-position="left">HTML 渲染预览</el-divider>
        <div class="html-preview">
          <iframe :srcdoc="content.rawHtml" sandbox="allow-same-origin" class="preview-iframe"></iframe>
        </div>
      </template>
    </div>
    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  content: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const statusLabel = (status) => ({ PENDING: '待发布', PUBLISHED: '已发布', DELETED: '已删除' })[status] || status
const formatDate = (dateStr) => dateStr ? new Date(dateStr).toLocaleString('zh-CN') : '-'

const fieldTableData = computed(() => {
  if (!props.content || !props.content.fields) return []
  return Object.entries(props.content.fields).map(([key, value]) => ({
    key,
    value: typeof value === 'object' ? JSON.stringify(value) : String(value),
    isHtml: key.toLowerCase().includes('html') || key.toLowerCase().includes('content')
  }))
})

const handleClose = () => {
  visible.value = false
}
</script>

<style scoped>
.preview-content {
  max-height: 70vh;
  overflow-y: auto;
}

.html-preview {
  margin-top: 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.preview-iframe {
  width: 100%;
  height: 400px;
  border: none;
  display: block;
}
</style>