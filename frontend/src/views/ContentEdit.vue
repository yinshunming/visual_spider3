<template>
  <div class="content-edit">
    <el-card v-if="content">
      <template #header>
        <span>编辑内容</span>
      </template>

      <el-form ref="formRef" :model="form" label-width="120px">
        <el-divider content-position="left">基本信息</el-divider>

        <el-form-item label="来源URL">
          <el-input v-model="form.sourceUrl" readonly />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option value="PENDING" label="待发布" />
            <el-option value="PUBLISHED" label="已发布" />
            <el-option value="DELETED" label="已删除" />
          </el-select>
        </el-form-item>

        <el-divider content-position="left">字段内容</el-divider>

        <el-form-item v-for="(value, key) in form.fields" :key="key" :label="key">
          <el-input v-model="form.fields[key]" type="textarea" :rows="3" />
        </el-form-item>

        <el-divider content-position="left">操作</el-divider>

        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
          <el-button @click="handleBack">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <div v-else v-loading="loading" style="min-height: 400px;"></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getContent, updateContent } from '../api'

const props = defineProps({
  id: {
    type: [String, Number],
    required: true
  }
})

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const content = ref(null)
const formRef = ref(null)
const form = ref({
  sourceUrl: '',
  status: 'PENDING',
  fields: {}
})

const loadContent = async () => {
  loading.value = true
  try {
    const res = await getContent(props.id)
    content.value = res.data
    form.value = {
      sourceUrl: content.value.sourceUrl || '',
      status: content.value.status || 'PENDING',
      fields: { ...content.value.fields }
    }
  } catch (err) {
    ElMessage.error('加载内容失败')
    console.error(err)
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    await updateContent(props.id, {
      fields: form.value.fields,
      status: form.value.status
    })
    ElMessage.success('保存成功')
    router.push('/contents')
  } catch (err) {
    ElMessage.error('保存失败')
    console.error(err)
  } finally {
    saving.value = false
  }
}

const handleBack = () => {
  router.push('/contents')
}

onMounted(() => {
  loadContent()
})
</script>

<style scoped>
.content-edit {
  padding: 20px;
}
</style>